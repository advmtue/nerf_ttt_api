package ch.adamtue.ttt.api.dao;

import ch.adamtue.ttt.api.dto.request.ChangePasswordRequest;
import ch.adamtue.ttt.api.dto.request.CreateUserRequest;
import ch.adamtue.ttt.api.dto.response.CreateUserResponse;
import ch.adamtue.ttt.api.model.TokenInfo;
import ch.adamtue.ttt.api.model.UserLogin;

public interface DatabaseService {
	/**
	 * Pull login information for a given username.
	 * Assumes only the username is indexed.
	 * @param username Unique username
	 **/
	public UserLogin getUserLogin(String username);

	/**
	 * Create a new user
	 * @param userInfo Username and default password
	 **/
	public CreateUserResponse createNewUser(CreateUserRequest userInfo);

	public boolean changeUserPassword(ChangePasswordRequest userInfo);

	public TokenInfo getTokenInfo(String userId);
}
