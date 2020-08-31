package ch.adamtue.ttt.api.dao;

import java.util.*;

import ch.adamtue.ttt.api.dto.request.CreateLobbyRequest;
import ch.adamtue.ttt.api.exception.*;
import ch.adamtue.ttt.api.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Repository;

import ch.adamtue.ttt.api.dto.request.ChangePasswordRequest;
import ch.adamtue.ttt.api.dto.request.ChangeUserRoleRequest;
import ch.adamtue.ttt.api.dto.request.CreateUserRequest;
import ch.adamtue.ttt.api.service.PasswordService;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

@Repository("DatabaseServiceDynamo")
public class DatabaseServiceDynamo implements DatabaseService {

	@Autowired
	PasswordService pwService;

	@Autowired
	SimpMessagingTemplate messageTemplate;

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
     * 
	 * @param key Primary key (Hash + SortKey)
	 * @return GetItem response object
	 * @throws DynamoDbException Generic DynamoDB issue
	 */
	private Map<String, AttributeValue> getItem(final Map<String, AttributeValue> key)
		throws DynamoDbException
	{
		GetItemRequest itemRequest = GetItemRequest.builder()
			.key(key)
			.tableName(this.tableName)
			.build();

		return this.dbClient.getItem(itemRequest).item();
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
		Map<String, AttributeValue> tokenInfo = this.getItem(UserProfile.createPK(userId));

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
			login = this.getItem(UserLogin.createPK(username));
		} catch (DynamoDbException e) {
			this.logger.error("Error retrieving UserLogin");
			this.logger.error(e.toString());
			throw new DefaultInternalError();
		}

		if (login.isEmpty()) return null;

		UserLogin ul = new UserLogin();
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

	/**
	 * Create a new user, setting their default password and passwordChangeOnLogin = true.
	 * @param userInfo Username and default password
	 * @throws UserAlreadyExistsException User already exists
	 */
	@Override
	public void createNewUser(CreateUserRequest userInfo)
		throws UserAlreadyExistsException
	{
		String userId = UUID.randomUUID().toString();

		AttributeValue hashKey = UserLogin.createHashKey(userInfo.getUsername());
		AttributeValue rangeKey = UserLogin.createRangeKey();
		
		/*
		 * Login : PutItemRequest
		 */
		HashMap<String, AttributeValue> loginItems = new HashMap<>(
			Map.of(
				"pk", hashKey,
				"sk", rangeKey,
				"passwordChangeOnLogin", AttributeValue.builder().bool(true).build(),
				"passwordResetValue", AttributeValue.builder().s(userInfo.getDefaultPassword()).build(),
				"GSI1-SK", hashKey,
				"GSI1-PK", AttributeValue.builder().s(userId).build()));

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
				"pk", UserProfile.createHashKey(userId),
				"sk", UserProfile.createRangeKey(),
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
		Map<String, AttributeValue> key = UserLogin.createPK(userInfo.getUsername());

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
		// Expression Attributes
		HashMap<String, AttributeValue> attrs = new HashMap<>(
			Map.of(
				":newRole", AttributeValue.builder().s(request.getNewRole()).build()));

		// Key
		Map<String, AttributeValue> key = UserProfile.createPK(request.getUserId());

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

		// Generate "new" game UUID
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
						"pk", GameMetadata.createHashKey(gameId),
						"sk", GameMetadata.createRangeKey(),
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

	@Override
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

		// Iterate the response items and marshall them into a GameMetadata object, appending to the list
		ArrayList<GameMetadata> activeLobbies = new ArrayList<>();
		
		for (Map<String, AttributeValue> item : response.items()) {
		    activeLobbies.add(GameMetadata.createFromQuery(item));
		}

		// Debug
        return activeLobbies;
	}

	@Override
	public void closeLobby(String lobbyId) {
		// Expression Attribute Values
		HashMap<String, AttributeValue> attrV = new HashMap<>(Map.of(
				":closed", AttributeValue.builder().s("CLOSED").build(),
				":lobby", AttributeValue.builder().s("LOBBY").build()
		));

		HashMap<String, String> attrN = new HashMap<>(Map.of(
				"#status", "GSI1-PK"
		));

		Map<String, AttributeValue> key = GameMetadata.createPK(lobbyId);
		
		// Update request
		UpdateItemRequest updateRequest = UpdateItemRequest.builder()
				.key(key)
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

	@Override
	public GameMetadata getLobby(String lobbyId) {
		Map<String, AttributeValue> item = this.getItem(GameMetadata.createPK(lobbyId));
		
		// No lobby found
		if (item.size() == 0) return null;
		
		return GameMetadata.createFromQuery(item);
	}

	@Override
	public List<GamePlayer> getLobbyPlayers(String lobbyId) {
		// Attribute values
		HashMap<String, AttributeValue> attrV = new HashMap<>(Map.of(
				":game", GamePlayer.createHashKey(lobbyId),
				":sortkey", AttributeValue.builder().s("PLAYER#").build()
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
        ArrayList<GamePlayer> players = new ArrayList<>();
	    
	    for (Map<String, AttributeValue> item : response.items()) {
	    	players.add(GamePlayer.createFromQuery(item));
		}
		
	    return players;
	}

	/**
	 * @param playerInfo Player information
	 * @param lobbyId Lobby UUID
	 * @return New (updated) player count
	 */
	@Override
	public long playerJoinLobby(TokenInfo playerInfo, String lobbyId) {
		GameMetadata metadata = getLobby(lobbyId);
		if (!metadata.getStatus().equals("LOBBY")) {
			throw new DefaultInternalError(); // TODO : better exception
		}

		// PutRequest assuming player is not already in lobby
		HashMap<String, AttributeValue> attr = new HashMap<>(Map.of(
				"pk", GamePlayer.createHashKey(lobbyId),
				"sk", GamePlayer.createRangeKey(playerInfo.getUserId()),
				"displayName", AttributeValue.builder().s(playerInfo.getName()).build(),
				"ready", AttributeValue.builder().bool(false).build()
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
		Map<String, AttributeValue> lobbyPk = GameMetadata.createPK(lobbyId);
		
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
	 * @return New (updated) player count
	 */
	@Override
	public long playerLeaveLobby(TokenInfo playerInfo, String lobbyId) {
		GameMetadata metadata = getLobby(lobbyId);
		if (!metadata.getStatus().equals("LOBBY")) {
			throw new DefaultInternalError(); // TODO : better exception
		}

	    Map<String, AttributeValue> key = GamePlayer.createPK(lobbyId, playerInfo.getUserId());
	    
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
		Map<String, AttributeValue> lobbyMetaDataPK = GameMetadata.createPK(lobbyId);

		// Attribute Values
		HashMap<String, AttributeValue> lobbyPlayerAttributeValues = new HashMap<>(Map.of(
				":minusOne", AttributeValue.builder().n("1").build()
		));

		// Update lobby player count
		UpdateItemRequest lobbyPlayerUpdateRequest = UpdateItemRequest.builder()
				.tableName(this.tableName)
				.updateExpression("SET playerCount = playerCount - :minusOne")
				.expressionAttributeValues(lobbyPlayerAttributeValues)
				.key(lobbyMetaDataPK)
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

	@Override
	public void playerSetReady(TokenInfo playerInfo, String lobbyId) {
		// PK
	    Map<String, AttributeValue> key = GamePlayer.createPK(lobbyId, playerInfo.getUserId());

	    // Attribute values
	    HashMap<String, AttributeValue> attrV = new HashMap<>(Map.of(
	    		":true", AttributeValue.builder().bool(true).build()
		));
	    
		// Update request with a PK constraint existence check
		UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(this.tableName)
				.key(key)
				.updateExpression("SET ready = :true")
				.expressionAttributeValues(attrV)
				.build();
		
		try {
			this.dbClient.updateItem(request);
		} catch (Exception e) {
			throw new DefaultInternalError();
		}
	}

	@Override
	public void playerSetUnready(TokenInfo playerInfo, String lobbyId) {
		// PK
		Map<String, AttributeValue> key = GamePlayer.createPK(lobbyId, playerInfo.getUserId());

		// Attribute values
		HashMap<String, AttributeValue> attrV = new HashMap<>(Map.of(
				":false", AttributeValue.builder().bool(false).build()
		));

		// Update request with a PK constraint existence check
		UpdateItemRequest request = UpdateItemRequest.builder()
				.tableName(this.tableName)
				.key(key)
				.updateExpression("SET ready = :false")
				.expressionAttributeValues(attrV)
				.build();

		try {
			this.dbClient.updateItem(request);
		} catch (Exception e) {
			throw new DefaultInternalError();
		}
	}

	@Override
	public void startLobby(TokenInfo playerInfo, String lobbyId) {
		GameMetadata gameData = this.getLobby(lobbyId);

		// TODO Remove after debug
		// Assign roles
		this.messageTemplate.convertAndSend(
				String.format("/topic/lobby/%s/status", lobbyId),
				Collections.singletonMap("status", "LAUNCHING")
		);

		// Calling player isn't owner
		if (!gameData.getOwnerId().equals(playerInfo.getUserId())) {
			throw new PlayerNotGameOwnerException();
		}

		// Game is not in lobby phase
		if (!gameData.getStatus().equals("LOBBY")) {
			throw new GameIsNotLobbyException();
		}
		
		// Players must all be ready
		List<GamePlayer> players = this.getLobbyPlayers(lobbyId);
		
		for (GamePlayer player : players) {
			if (!player.isReady()) {
				throw new PlayersNotReadyException();
			}
		}

		// Minimum of 3 players
		if (players.size() < 3) {
			throw new NotEnoughPlayersException();
		}

	}
}
