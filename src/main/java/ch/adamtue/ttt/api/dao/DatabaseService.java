package ch.adamtue.ttt.api.dao;

import ch.adamtue.ttt.api.dto.request.ChangePasswordRequest;
import ch.adamtue.ttt.api.dto.request.ChangeUserRoleRequest;
import ch.adamtue.ttt.api.dto.request.CreateLobbyRequest;
import ch.adamtue.ttt.api.dto.request.CreateUserRequest;
import ch.adamtue.ttt.api.model.GameMetadata;
import ch.adamtue.ttt.api.model.GamePlayer;
import ch.adamtue.ttt.api.model.TokenInfo;
import ch.adamtue.ttt.api.model.UserLogin;

import java.util.List;

public interface DatabaseService {
	/**
	 * Pull login information for a given username.
	 * Assumes only the username is indexed.
	 * @param username Unique username
	 **/
	UserLogin getUserLogin(String username);

	/**
	 * Create a new user
	 * @param userInfo Username and default password
	 **/
	void createNewUser(CreateUserRequest userInfo);

	/**
	 * Update a user password. Set passwordChangeOnLogin = false as a side effect.
	 * @param userInfo Information for use in update
	 */
	void changeUserPassword(ChangePasswordRequest userInfo);

	/**
	 * For a given userId, pull their token information
	 * @param userId User UUID
	 * @return Token information object
	 */
	TokenInfo getTokenInfo(String userId);

	/**
	 * Change the access role of a user
	 * @param request User role request information
	 */
	void changeUserRole(ChangeUserRoleRequest request);

	/**
	 * Create a new lobby
	 * @param ownerInfo Owner UUID
	 * @param lobbyInfo Name of the lobby
	 * @return Game metadata
	 */
	GameMetadata createNewLobby(TokenInfo ownerInfo, CreateLobbyRequest lobbyInfo);

	/**
	 * Get the list of active lobbies
	 * @return Public lobby listing
	 */
	List<GameMetadata> getLobbyList();

	/**
	 * Close a lobby (if it is open)
	 * @param lobbyId Lobby ID
	 */
	void closeLobby(String lobbyId);

	/**
	 * Pull information for a given lobby
	 * @param lobbyId Lobby UUID
	 * @return Lobby information (GameMetadata)
	 */
	GameMetadata getLobby(String lobbyId);

	/**
	 * Pull players who are participating in a lobby
	 * @param lobbyId Lobby UUID
	 * @return Player list
	 */
	List<GamePlayer> getLobbyPlayers(String lobbyId);

	/**
	 * Player joins a lobby
	 * @param playerInfo Player information
	 * @param lobbyId Lobby UUID
	 * @return New player count
	 */
	long playerJoinLobby(TokenInfo playerInfo, String lobbyId);
	
	/**
	 * Player leaves a lobby
	 * @param playerInfo Player information
	 * @param lobbyId Lobby UUID
	 * @return New player count   
	 */
	long playerLeaveLobby(TokenInfo playerInfo, String lobbyId);

	/**
	 * Player sets their status to ready
	 * @param playerInfo Player information
	 * @param lobbyId Lobby UUID
	 */
	void playerSetReady(TokenInfo playerInfo, String lobbyId);

	/**
	 * Player sets their status to unready
	 * @param playerInfo Player information
	 * @param lobbyId Lobby UUID
	 */
	void playerSetUnready(TokenInfo playerInfo, String lobbyId);

	/**
	 * Attempt to start the lobby
	 *
	 * @param playerInfo Calling player information
	 * @param lobbyId Lobby UUID
	 */
	void startLobby(TokenInfo playerInfo, String lobbyId);

	/**
	 * Pull player list for game
	 *
	 * @param lobbyId Lobby UUID
	 */
	List<GamePlayer> getGamePlayersPregame(String lobbyId);
}
