package ch.adamtue.ttt.api.dao;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import ch.adamtue.ttt.api.dto.request.ChangePasswordRequest;
import ch.adamtue.ttt.api.dto.request.CreateUserRequest;
import ch.adamtue.ttt.api.dto.response.CreateUserResponse;
import ch.adamtue.ttt.api.exception.DefaultInternalError;
import ch.adamtue.ttt.api.exception.FailedPasswordHashException;
import ch.adamtue.ttt.api.exception.PasswordNotChangeableException;
import ch.adamtue.ttt.api.exception.UserAlreadyExistsException;
import ch.adamtue.ttt.api.exception.UserNotExistsException;
import ch.adamtue.ttt.api.model.TokenInfo;
import ch.adamtue.ttt.api.model.UserLogin;
import ch.adamtue.ttt.api.service.PasswordService;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

@Repository("DatabaseServiceDynamo")
public class DatabaseServiceDynamo implements DatabaseService {

	@Autowired @Qualifier("PasswordServiceDefault")
	PasswordService pwService;

	DynamoDbClient dbClient;
	String tableName;
	Logger logger;

	public DatabaseServiceDynamo() {
		this.tableName = "ttt_testing";

		this.logger = LoggerFactory.getLogger(DatabaseServiceDynamo.class);

		// Create DDB connection to Sydney
		Region region = Region.AP_SOUTHEAST_2;
		this.dbClient = DynamoDbClient.builder()
			.region(region).build();
	}

	private Map<String, AttributeValue> getItem(String partKey, String sortKey)
		throws DynamoDbException
	{
		HashMap<String, AttributeValue> keyToGet = new HashMap<String, AttributeValue>(
			Map.of(
				"pk", AttributeValue.builder().s(partKey).build(),
				"sk", AttributeValue.builder().s(sortKey).build()));

		// Create the request object
		GetItemRequest itemRequest = GetItemRequest.builder()
			.key(keyToGet)
			.tableName(this.tableName)
			.build();

		return this.dbClient.getItem(itemRequest).item();
	}

	public TokenInfo getTokenInfo(String userId)
		throws UserNotExistsException
	{
		String PK = String.format("USER#%s", userId);
		String SK = "profile";

		Map<String, AttributeValue> tokenInfo = this.getItem(PK, SK);

		if (tokenInfo.isEmpty()) throw new UserNotExistsException();

		TokenInfo userTokenInfo = new TokenInfo();
		try {
			userTokenInfo.setUserId(userId);
			userTokenInfo.setAccessRole(tokenInfo.get("accessRole").s());
		} catch (DynamoDbException e) {
			throw new DefaultInternalError();
		}

		return userTokenInfo;
	}

	public UserLogin getUserLogin(String username) {
		Map<String, AttributeValue> login;

		try {
			login = this.getItem(String.format("LOGIN#%s", username), "login");
		} catch (ResourceNotFoundException e) {
			return null;
		} catch (DynamoDbException e) {
			this.logger.error("Error retrieving UserLogin");
			this.logger.error(e.toString());
			return null;
		}

		if (login.isEmpty()) return null;

		UserLogin ul = new UserLogin();
		ul.setPK(login.get("pk").s());
		ul.setSK(login.get("sk").s());
		ul.setUserId(login.get("userId").s());
		ul.setPasswordChangeOnLogin(login.get("passwordChangeOnLogin").bool());

		if (ul.getPasswordChangeOnLogin()) {
			ul.setPasswordResetValue(login.get("passwordResetValue").s());
		} else {
			ul.setPasswordHash(login.get("passwordHash").b().asByteArray());
			ul.setPasswordSalt(login.get("passwordSalt").b().asByteArray());
		}

		return ul;
	}

	public CreateUserResponse createNewUser(CreateUserRequest userInfo)
		throws UserAlreadyExistsException
	{
		// Create a new UUID
		UUID userId = UUID.randomUUID();

		// Build the LOGIN#{username} | login PutItem
		HashMap<String, AttributeValue> loginItems = new HashMap<String, AttributeValue>(
			Map.of(
				"pk", AttributeValue.builder().s(String.format("LOGIN#%s", userInfo.getUsername())).build(),
				"sk", AttributeValue.builder().s("login").build(),
				"passwordChangeOnLogin", AttributeValue.builder().bool(true).build(),
				"passwordResetValue", AttributeValue.builder().s(userInfo.getDefaultPassword()).build(),
				"userId", AttributeValue.builder().s(userId.toString()).build()));

		HashMap<String, AttributeValue> profileItems = new HashMap<String, AttributeValue>(
			Map.of(
				"pk", AttributeValue.builder().s(String.format("USER#%s", userId.toString())).build(),
				"sk", AttributeValue.builder().s("profile").build(),
				"GSI1-PK", AttributeValue.builder().s("user").build(),
				"GSI1-SK", AttributeValue.builder().s(userInfo.getDisplayName()).build(),
				"statsKills", AttributeValue.builder().n("0").build(),
				"statsDeaths", AttributeValue.builder().n("0").build(),
				"statsWins", AttributeValue.builder().n("0").build(),
				"statsLosses", AttributeValue.builder().n("0").build(),
				"accessRole", AttributeValue.builder().s("user").build(),
				"joinDate", AttributeValue.builder().s(String.format("%s", System.currentTimeMillis())).build()));

		// Build a putItem request
		PutItemRequest loginPr = PutItemRequest.builder()
			.tableName(this.tableName)
			.item(loginItems)
			.conditionExpression("attribute_not_exists(pk)")
			.build();

		PutItemRequest profilePr = PutItemRequest.builder()
			.tableName(this.tableName)
			.item(profileItems)
			.conditionExpression("attribute_not_exists(pk)")
			.build();

		// Push the login item to the database
		try {
			// Do in two separate requests so we can deal with username exists
			this.dbClient.putItem(loginPr);
			this.dbClient.putItem(profilePr); // Cant have a conditionExpression
		} catch (ConditionalCheckFailedException e) {
			throw new UserAlreadyExistsException();
		} catch (DynamoDbException dbe) {
			this.logger.error("Caught default dynamo exception in createUser");
			this.logger.error(dbe.toString());

			throw new DefaultInternalError();
		}

		CreateUserResponse response = new CreateUserResponse();
		response.setUsername(userInfo.getUsername());
		return response;
	}

	public boolean changeUserPassword(ChangePasswordRequest userInfo)
		throws FailedPasswordHashException
	{
		String pw = userInfo.getNewPassword();

		// Create a random salt
		byte[] salt = this.pwService.generateSalt();
		byte[] newPwHash;

		newPwHash = this.pwService.createPasswordHash(pw, salt);

		// Update Key
		HashMap<String, AttributeValue> key = new HashMap<String, AttributeValue>(
			Map.of(
				"pk", AttributeValue.builder().s(String.format("LOGIN#%s", userInfo.getUsername())).build(),
				"sk", AttributeValue.builder().s("login").build()));

		// Expression Attribute Values
		HashMap<String, AttributeValue> attrs = new HashMap<String, AttributeValue>(
			Map.of(
				":pwHash", AttributeValue.builder().b(SdkBytes.fromByteArray(newPwHash)).build(),
				":false", AttributeValue.builder().bool(false).build(),
				":true", AttributeValue.builder().bool(true).build(),
				":salt", AttributeValue.builder().b(SdkBytes.fromByteArray(salt)).build()));

		// Build a query
		UpdateItemRequest updateReq = UpdateItemRequest.builder()
			.tableName(this.tableName)
			.updateExpression("SET passwordHash = :pwHash, passwordSalt = :salt, passwordChangeOnLogin = :false")
			.conditionExpression("passwordChangeOnLogin = :true")
			.expressionAttributeValues(attrs)
			.key(key)
			.build();

		// Perform the update request
		try {
			this.dbClient.updateItem(updateReq);
		} catch (ConditionalCheckFailedException e) {
			// passwordChangeOnLogin was false
			throw new PasswordNotChangeableException();
		} catch (DynamoDbException e) {
			this.logger.error("Caught default updating userLogin");
			this.logger.error(e.toString());
			throw new DefaultInternalError();
		}

		return true;
	}
}
