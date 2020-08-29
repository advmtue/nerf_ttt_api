package ch.adamtue.ttt.api.dao;

import java.util.*;

import ch.adamtue.ttt.api.dto.request.CreateLobbyRequest;
import ch.adamtue.ttt.api.model.GameMetadata;
import ch.adamtue.ttt.api.model.LobbyPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import ch.adamtue.ttt.api.dto.request.ChangePasswordRequest;
import ch.adamtue.ttt.api.dto.request.ChangeUserRoleRequest;
import ch.adamtue.ttt.api.dto.request.CreateUserRequest;
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
import software.amazon.awssdk.services.dynamodb.model.*;

import javax.validation.groups.Default;

@Repository("DatabaseServiceDynamo")
public class DatabaseServiceDynamo implements DatabaseService {

	@Autowired
	PasswordService pwService;

	public static String createProfilePK(String userId) {
		return String.format("USER#%s", userId);
	}

	final private DynamoDbClient dbClient;
	final private String tableName;
	final private Logger logger;

	public DatabaseServiceDynamo() {
		this.tableName = "ttt_testing";

		this.logger = LoggerFactory.getLogger(DatabaseServiceDynamo.class);

		// Create DDB connection to Sydney
		Region region = Region.AP_SOUTHEAST_2;
		this.dbClient = DynamoDbClient.builder()
			.region(region).build();
	}

	/**
	 * Perform a basic get request on a PK/SK pair.
	 * @param partKey Partition key
	 * @param sortKey Sort key
	 * @return GetItem response object
	 * @throws DynamoDbException Generic DynamoDB issue
	 */
	private Map<String, AttributeValue> getItem(String partKey, String sortKey)
		throws DynamoDbException
	{
		HashMap<String, AttributeValue> keyToGet = buildPK(partKey, sortKey);
		
		GetItemRequest itemRequest = GetItemRequest.builder()
			.key(keyToGet)
			.tableName(this.tableName)
			.build();

		return this.dbClient.getItem(itemRequest).item();
	}

	/**
	 * Helper function for building pk/sk pairs
	 * @param pk Primary Hash Key
	 * @param sk Sort Key
	 * @return PK/SK HashMap
	 */
	private HashMap<String, AttributeValue> buildPK(String pk, String sk) {
	    return new HashMap<>(
	    		Map.of(
	    				"pk", AttributeValue.builder().s(pk).build(),
						"sk", AttributeValue.builder().s(sk).build()));
	}

	/**
	 * For a given userId, pull the required TokenInfo
	 * @param userId User UUID
	 * @return Token information for specified user
	 * @throws UserNotExistsException User doesn't exist
	 */
	@Override
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
			userTokenInfo.setName(tokenInfo.get("GSI1-SK").s());
		} catch (DynamoDbException e) {
			throw new DefaultInternalError();
		}

		return userTokenInfo;
	}

	/**
	 * For a given username, pull the login information
	 * @param username Unique username
	 * @return User login information
	 * @throws DefaultInternalError Generic error for the user
	 */
	@Override
	public UserLogin getUserLogin(String username)
		throws DefaultInternalError
	{
		Map<String, AttributeValue> login;

		try {
			login = this.getItem(String.format("LOGIN#%s", username), "login");
		} catch (DynamoDbException e) {
			this.logger.error("Error retrieving UserLogin");
			this.logger.error(e.toString());
			throw new DefaultInternalError();
		}

		if (login.isEmpty()) return null;

		UserLogin ul = new UserLogin();
		ul.setPK(login.get("pk").s());
		ul.setSK(login.get("sk").s());
		ul.setUserId(login.get("GSI1-PK").s());
		ul.setPasswordChangeOnLogin(login.get("passwordChangeOnLogin").bool());

		if (ul.isPasswordChangeOnLogin()) {
			ul.setPasswordResetValue(login.get("passwordResetValue").s());
		} else {
			ul.setPasswordHash(login.get("passwordHash").b().asByteArray());
			ul.setPasswordSalt(login.get("passwordSalt").b().asByteArray());
		}

		return ul;
	}

	// TODO : Cleanup mega-functions

	/**
	 * Create a new user, setting their default password and passwordChangeOnLogin = true.
	 * @param userInfo Username and default password
	 * @throws UserAlreadyExistsException User already exists
	 */
	@Override
	public void createNewUser(CreateUserRequest userInfo)
		throws UserAlreadyExistsException
	{
		UUID userId = UUID.randomUUID(); // TODO : Review

		/*
		 * Login : PutItemRequest
		 */
		HashMap<String, AttributeValue> loginItems = new HashMap<>(
			Map.of(
				"pk", AttributeValue.builder().s(String.format("LOGIN#%s", userInfo.getUsername())).build(),
				"sk", AttributeValue.builder().s("login").build(),
				"passwordChangeOnLogin", AttributeValue.builder().bool(true).build(),
				"passwordResetValue", AttributeValue.builder().s(userInfo.getDefaultPassword()).build(),
				"GSI1-SK", AttributeValue.builder().s(String.format("LOGIN#%s", userInfo.getUsername())).build(),
				"GSI1-PK", AttributeValue.builder().s(userId.toString()).build()));

		PutItemRequest loginPr = PutItemRequest.builder()
			.tableName(this.tableName)
			.item(loginItems)
			.conditionExpression("attribute_not_exists(pk)")
			.build();

		/*
		 * Profile : PutItemRequest
		 */
		HashMap<String, AttributeValue> profileItems = new HashMap<>(
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

		PutItemRequest profilePr = PutItemRequest.builder()
			.tableName(this.tableName)
			.item(profileItems)
			.conditionExpression("attribute_not_exists(pk)")
			.build();


		// Step 1: Insert the new login
		try {
			this.dbClient.putItem(loginPr);
		} catch (ConditionalCheckFailedException e) {
			// User already exists (failed the condition)
			throw new UserAlreadyExistsException();
		} catch (DynamoDbException e) {
			// Generic failure
			this.logger.error("Error inserting new user login information for :{}", userInfo.getUsername());
			this.logger.error(e.toString());

			throw new DefaultInternalError();
		}


		// Step 2: Insert the new profile
		try {
			// Insert the new profile
			this.dbClient.putItem(profilePr);
		} catch (DynamoDbException dbe) {
			// Generic failure
			this.logger.error("Error creating new user {}", userInfo.getUsername());
			this.logger.error(dbe.toString());

			// TODO : Rollback/retry w exponential backoff
			throw new DefaultInternalError();
		}
	}

	/**
	 * Update specified user's password. Sets passwordChangeOnLogin = false
	 * @param userInfo User information
	 * @throws FailedPasswordHashException Generic hashing/crypto error
	 */
	@Override
	public void changeUserPassword(ChangePasswordRequest userInfo)
		throws FailedPasswordHashException
	{
		// Create a random salt
		byte[] salt = this.pwService.generateSalt();
		byte[] newPwHash = this.pwService.createPasswordHash(userInfo.getNewPassword(), salt);

		// Create the key mapping
		HashMap<String, AttributeValue> key = buildPK(String.format("LOGIN#%s", userInfo.getUsername()), "login");

		// Create expression attribute values
		HashMap<String, AttributeValue> attrs = new HashMap<>(
			Map.of(
				":pwHash", AttributeValue.builder().b(SdkBytes.fromByteArray(newPwHash)).build(),
				":false", AttributeValue.builder().bool(false).build(),
				":true", AttributeValue.builder().bool(true).build(),
				":salt", AttributeValue.builder().b(SdkBytes.fromByteArray(salt)).build()));

		// Build the query
		UpdateItemRequest updateReq = UpdateItemRequest.builder()
			.tableName(this.tableName)
			.updateExpression("SET passwordHash = :pwHash, passwordSalt = :salt, passwordChangeOnLogin = :false")
			.conditionExpression("passwordChangeOnLogin = :true")
			.expressionAttributeValues(attrs)
			.key(key)
			.build();

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
	}

	/**
	 * Change the role of specified user
	 * @param request User role request
	 */
	@Override
	public void changeUserRole(ChangeUserRoleRequest request) {
		String pk = createProfilePK(request.getUserId());
		String sk = "profile";

		// Expression Attributes
		HashMap<String, AttributeValue> attrs = new HashMap<>(
			Map.of(
				":newRole", AttributeValue.builder().s(request.getNewRole()).build()));

		// Key
		HashMap<String, AttributeValue> key = buildPK(pk, sk);

		UpdateItemRequest updateRequest = UpdateItemRequest.builder()
			.tableName(this.tableName)
			.updateExpression("SET accessRole = :newRole")
			.conditionExpression("attribute_exists(pk)")
			.expressionAttributeValues(attrs)
			.key(key)
			.build();

		try {
			this.dbClient.updateItem(updateRequest);
		} catch (ConditionalCheckFailedException e) {
			throw new UserNotExistsException();
		} catch (DynamoDbException e) {
			this.logger.error("Dynamo error in changeUserRole");
			this.logger.error(e.toString());
			throw new DefaultInternalError();
		}
	}

	/**
	 * Create a new lobby owned by ownerInfo.userId
	 * @param ownerInfo Owner information (UUID, name)
	 * @param lobbyInfo Create lobby request information
	 * @return Lobby UUID
	 */
	@Override
	public GameMetadata createNewLobby(TokenInfo ownerInfo, CreateLobbyRequest lobbyInfo) {
	    /*
	    	Required fields:
	    		[string] Game#GameID (pk)
	    		[string] metadata (sk)
	    		[string] Owner ID
	    		[string] Owner Name
	    		[string] Lobby Name
	    		[string] Date Created (GSI1-SK)
	    		[string] Status (GSI1-PK)
	    		[number] Player Count
	     */

		// Generate "new" game UUID -- TODO : Research
		String gameId = UUID.randomUUID().toString();

		// Build GameMetadata
		GameMetadata gameInfo = new GameMetadata();
		gameInfo.setGameId(gameId);
		gameInfo.setOwnerId(ownerInfo.getUserId());
		gameInfo.setOwnerName(ownerInfo.getName());
		gameInfo.setName(lobbyInfo.getName());
		gameInfo.setDateCreated(String.format("%s", System.currentTimeMillis()));
		gameInfo.setStatus("LOBBY");
		gameInfo.setPlayerCount(0);

		// Build PutItemRequest fields
		HashMap<String, AttributeValue> items = new HashMap<>(
				Map.of(
						"pk", AttributeValue.builder().s(gameInfo.getPk()).build(),
						"sk", AttributeValue.builder().s(gameInfo.getSk()).build(),
						"ownerId", AttributeValue.builder().s(gameInfo.getOwnerId()).build(),
						"ownerName", AttributeValue.builder().s(gameInfo.getOwnerName()).build(),
						"lobbyName", AttributeValue.builder().s(gameInfo.getName()).build(),
						"GSI1-SK", AttributeValue.builder().s(gameInfo.getDateCreated()).build(),
						"GSI1-PK", AttributeValue.builder().s(gameInfo.getStatus()).build(),
						"playerCount", AttributeValue.builder().n(String.format("%s", gameInfo.getPlayerCount())).build()
				)
		);

		// Build the put request
		PutItemRequest pr = PutItemRequest.builder()
				.tableName(this.tableName)
				.conditionExpression("attribute_not_exists(pk)")
				.item(items)
				.build();

		// Execute the request
		try {
			this.dbClient.putItem(pr);
		} catch (Exception e) {
			e.printStackTrace();
		    throw new DefaultInternalError();
		}
		
		return gameInfo;
	}
	
	public List<GameMetadata> getLobbyList() {
		HashMap<String, AttributeValue> attrV = new HashMap<>(Map.of(
				":status", AttributeValue.builder().s("LOBBY").build()
		));
		
		HashMap<String, String> attrN = new HashMap<>(Map.of(
				"#pk", "GSI1-PK"
		));
		
		QueryRequest qr = QueryRequest.builder()
				.indexName("GSI1-UserList-LobbyList")
				.tableName("ttt_testing")
				.keyConditionExpression("#pk = :status")
				.expressionAttributeValues(attrV)
                .expressionAttributeNames(attrN)
				.build();

		QueryResponse response;
		try {
			response = this.dbClient.query(qr);
		} catch (Exception e) {
			e.printStackTrace();
			throw new DefaultInternalError();
		}

		// TODO Pull information for lobby players :o

		// Iterate the response items and marshall them into a GameMetadata object, appending to the list
		ArrayList<GameMetadata> activeLobbies = new ArrayList<>();
		
		for (Map<String, AttributeValue> item : response.items()) {
		    activeLobbies.add(GameMetadata.createFromQuery(item));
		}

		// Debug
        return activeLobbies;
	}
	
	public void closeLobby(String lobbyId) {
		// Expression Attribute Values
		HashMap<String, AttributeValue> attrV = new HashMap<>(Map.of(
				":closed", AttributeValue.builder().s("CLOSED").build(),
				":lobby", AttributeValue.builder().s("LOBBY").build()
		));

		HashMap<String, String> attrN = new HashMap<>(Map.of(
				"#status", "GSI1-PK"
		));

		HashMap<String, AttributeValue> keys = new HashMap<>(Map.of(
				"pk", AttributeValue.builder().s(String.format("GAME#%s", lobbyId)).build(),
				"sk", AttributeValue.builder().s("metadata").build()
		));
		
		// Update request
		UpdateItemRequest updateRequest = UpdateItemRequest.builder()
				.key(keys)
				.tableName(this.tableName)
				.updateExpression("SET #status = :closed")
				.expressionAttributeValues(attrV)
				.expressionAttributeNames(attrN)
				.conditionExpression("#status = :lobby")
				.build();
		
		// Perform update request
		try {
			this.dbClient.updateItem(updateRequest);
		} catch (Exception e) { // TODO Throw better errors, and check ConditionCheckFailed
		    e.printStackTrace();
		    throw new DefaultInternalError();
		}
	}
	
	public GameMetadata getLobby(String lobbyId) {
		Map<String, AttributeValue> item = this.getItem(String.format("GAME#%s", lobbyId), "metadata");
		
		// No lobby found
		if (item.size() == 0) return null;
		
		return GameMetadata.createFromQuery(item);
	}
	
	public List<LobbyPlayer> getLobbyPlayers(String lobbyId) {
		// Attribute values
		HashMap<String, AttributeValue> attrV = new HashMap<>(Map.of(
				":game", AttributeValue.builder().s(String.format("GAME#%s", lobbyId)).build(),
				":sortkey", AttributeValue.builder().s("lobby#player#").build()
		));

		// Attribute names
		HashMap<String, String> attrN = new HashMap<>(Map.of(
				"#pk", "pk",
				"#sk", "sk"
		));

				
	    QueryRequest query = QueryRequest.builder()
				.tableName(this.tableName)
				.keyConditionExpression("#pk = :game AND begins_with(#sk, :sortkey)")
				.expressionAttributeNames(attrN)
				.expressionAttributeValues(attrV)
                .build();

	    QueryResponse response;
	    try {
	    	response = this.dbClient.query(query);
		} catch (Exception e) {
	    	throw new DefaultInternalError();
		}
	    
	    // Map response to lobbyplayer
        ArrayList<LobbyPlayer> players = new ArrayList<>();
	    
	    for (Map<String, AttributeValue> item : response.items()) {
	    	players.add(LobbyPlayer.createFromQuery(item));
		}
		
	    return players;
	}

	/**
	 * @param playerInfo Player information
	 * @param lobbyId Lobby UUID
	 * @return
	 */
	public long playerJoinLobby(TokenInfo playerInfo, String lobbyId) {
		// TODO check game status

		// PutRequest assuming player is not already in lobby
		HashMap<String, AttributeValue> attr = new HashMap<>(Map.of(
				"pk", AttributeValue.builder().s(String.format("GAME#%s", lobbyId)).build(),
				"sk", AttributeValue.builder().s(String.format("lobby#player#%s", playerInfo.getUserId())).build(),
				"displayName", AttributeValue.builder().s(playerInfo.getName()).build(),
				"isReady", AttributeValue.builder().bool(false).build()
		));
		
		PutItemRequest request = PutItemRequest.builder()
				.tableName(this.tableName)
                .item(attr)
				.build();

		try {
			this.dbClient.putItem(request);
		} catch (Exception e) {
			throw new DefaultInternalError();
		}
		
		// Build PK for lobby player count update
		Map<String, AttributeValue> lobbyPk = buildPK(
				String.format("GAME#%s", lobbyId),
				"metadata"
		);
		
		Map<String, AttributeValue> lobbyAttributeValues = new HashMap<>(Map.of(
				":plusOne", AttributeValue.builder().n("1").build()
		));
		
		// Update lobby player count
        UpdateItemRequest updateLobbyRequest = UpdateItemRequest.builder()
				.tableName(this.tableName)
				.key(lobbyPk)
				.updateExpression("SET playerCount = playerCount + :plusOne")
				.conditionExpression("attribute_exists(playerCount)")
				.expressionAttributeValues(lobbyAttributeValues)
                .returnValues("UPDATED_NEW")
				.build();
        
        UpdateItemResponse lobbyUpdateResponse;
        try {
        	lobbyUpdateResponse = this.dbClient.updateItem(updateLobbyRequest);
		} catch (Exception e) {
        	throw new DefaultInternalError();
		}
        
        return Long.parseLong(lobbyUpdateResponse.attributes().get("playerCount").n());
	}

	/**
	 * @param playerInfo Player information
	 * @param lobbyId Lobby UUID
	 * @return
	 */
	public long playerLeaveLobby(TokenInfo playerInfo, String lobbyId) {
		// Todo check game status 
	    Map<String, AttributeValue> key = buildPK(
	    		String.format("GAME#%s", lobbyId),
				String.format("lobby#player#%s", playerInfo.getUserId())
		);
	    
		// DeleteRequest
		DeleteItemRequest request = DeleteItemRequest.builder()
				.tableName(this.tableName)
				.key(key)
				.build();
		
		try {
			this.dbClient.deleteItem(request);
		} catch (Exception e) {
			throw new DefaultInternalError();
		}

		// PK for lobby
		Map<String, AttributeValue> lobbyMetadatapk = buildPK(
				String.format("GAME#%s", lobbyId),
				"metadata"
		);
		
		// Attribute Values
		HashMap<String, AttributeValue> lobbyPlayerAttributeValues = new HashMap<>(Map.of(
				":minusOne", AttributeValue.builder().n("1").build()
		));

		// Update lobby player count
		UpdateItemRequest lobbyPlayerUpdateRequest = UpdateItemRequest.builder()
				.tableName(this.tableName)
				.updateExpression("SET playerCount = playerCount - :minusOne")
				.expressionAttributeValues(lobbyPlayerAttributeValues)
				.key(lobbyMetadatapk)
				.returnValues("UPDATED_NEW")
				.build();

		UpdateItemResponse lobbyPlayerUpdateResponse;
		try {
			lobbyPlayerUpdateResponse = this.dbClient.updateItem(lobbyPlayerUpdateRequest);
		} catch (Exception e) {
			throw new DefaultInternalError();
		}

		return Long.parseLong(lobbyPlayerUpdateResponse.attributes().get("playerCount").n());
	}
	
	public void playerSetReady(TokenInfo playerInfo, String lobbyId) {
		// PK
	    Map<String, AttributeValue> key = buildPK(
	    		String.format("GAME#%s", lobbyId),
				String.format("lobby#player#%s", playerInfo.getUserId())
		);

	    // Attribute values
	    HashMap<String, AttributeValue> attrV = new HashMap<>(Map.of(
	    		":true", AttributeValue.builder().bool(true).build()
		));
	    
		// Update request with a PK constraint existence check
		UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(this.tableName)
				.key(key)
				.updateExpression("SET isReady = :true")
				.expressionAttributeValues(attrV)
				.build();
		
		try {
			this.dbClient.updateItem(request);
		} catch (Exception e) {
			throw new DefaultInternalError();
		}
		
		return;
	}
	
	public void playerSetUnready(TokenInfo playerInfo, String lobbyId) {
		// PK
		Map<String, AttributeValue> key = buildPK(
				String.format("GAME#%s", lobbyId),
				String.format("lobby#player#%s", playerInfo.getUserId())
		);

		// Attribute values
		HashMap<String, AttributeValue> attrV = new HashMap<>(Map.of(
				":false", AttributeValue.builder().bool(false).build()
		));

		// Update request with a PK constraint existence check
		UpdateItemRequest request = UpdateItemRequest.builder()
				.tableName(this.tableName)
				.key(key)
				.updateExpression("SET isReady = :false")
				.expressionAttributeValues(attrV)
				.build();

		try {
			this.dbClient.updateItem(request);
		} catch (Exception e) {
			throw new DefaultInternalError();
		}

		return;
	}
}
