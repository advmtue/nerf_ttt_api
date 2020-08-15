package ch.adamtue.ttt.api.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import ch.adamtue.ttt.api.dto.request.ChangePasswordRequest;
import ch.adamtue.ttt.api.dto.request.CreateUserRequest;
import ch.adamtue.ttt.api.dto.request.LoginRequest;
import ch.adamtue.ttt.api.dto.response.ChangePasswordResponse;
import ch.adamtue.ttt.api.dto.response.CreateUserResponse;
import ch.adamtue.ttt.api.dto.response.LoginResponse;
import ch.adamtue.ttt.api.exception.InvalidCredentialsException;
import ch.adamtue.ttt.api.exception.UserAlreadyExistsException;
import ch.adamtue.ttt.api.service.UserService;

@RestController
public class AuthController {
	@Autowired @Qualifier("UserService")
	private UserService userService;

	@PostMapping("/auth/login")
	public LoginResponse postLogin(@RequestBody @Valid LoginRequest loginRequest)
		throws InvalidCredentialsException
	{
		return this.userService.getLoginMeta(loginRequest);
	}

	// DEBUG
	@PutMapping("/auth/user")
	public CreateUserResponse postCreateUser(@RequestBody @Valid CreateUserRequest userInfo)
		throws UserAlreadyExistsException
	{
		return this.userService.createNewUser(userInfo);
	}

	@PostMapping("/auth/changepassword")
	public ChangePasswordResponse postChangePassword(@RequestBody @Valid ChangePasswordRequest userInfo)
		throws InvalidCredentialsException
	{
		return this.userService.changeUserPassword(userInfo);
	}
}
