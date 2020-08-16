package ch.adamtue.ttt.api.service;

import ch.adamtue.ttt.api.dto.request.ChangePasswordRequest;
import ch.adamtue.ttt.api.dto.request.CreateUserRequest;
import ch.adamtue.ttt.api.dto.request.LoginRequest;
import ch.adamtue.ttt.api.dto.response.CreateUserResponse;
import ch.adamtue.ttt.api.dto.response.LoginResponse;

public interface UserService {
	public LoginResponse getLoginMeta(LoginRequest loginInfo);

	public LoginResponse changeUserPassword(ChangePasswordRequest userInfo);

	public CreateUserResponse createNewUser(CreateUserRequest userInfo);
}
