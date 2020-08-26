package ch.adamtue.ttt.api.dao;

import ch.adamtue.ttt.api.dto.request.ChangePasswordRequest;
import ch.adamtue.ttt.api.dto.request.ChangeUserRoleRequest;
import ch.adamtue.ttt.api.dto.request.CreateLobbyRequest;
import ch.adamtue.ttt.api.dto.request.CreateUserRequest;
import ch.adamtue.ttt.api.model.GameMetadata;
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
}
