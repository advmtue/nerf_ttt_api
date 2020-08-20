package ch.adamtue.ttt.api.dao;

import ch.adamtue.ttt.api.dto.request.ChangePasswordRequest;
import ch.adamtue.ttt.api.dto.request.ChangeUserRoleRequest;
import ch.adamtue.ttt.api.dto.request.CreateUserRequest;
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
	public void createNewUser(CreateUserRequest userInfo);

	public void changeUserPassword(ChangePasswordRequest userInfo);

	public TokenInfo getTokenInfo(String userId);

	public void changeUserRole(ChangeUserRoleRequest request);
}
