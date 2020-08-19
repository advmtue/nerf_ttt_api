package ch.adamtue.ttt.api.controller;

import java.security.Principal;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import ch.adamtue.ttt.api.dto.request.ChangePasswordRequest;
import ch.adamtue.ttt.api.dto.request.LoginRequest;
import ch.adamtue.ttt.api.dto.response.LoginResponse;
import ch.adamtue.ttt.api.exception.InvalidCredentialsException;
import ch.adamtue.ttt.api.service.UserService;

@RestController
public class AuthController {

	@Autowired @Qualifier("UserService")
	private UserService userService;

	/**
	 * Anonymous user requests a login token
	 **/
	@PostMapping("/auth/login")
	public LoginResponse postLogin(@AuthenticationPrincipal Principal principal, @RequestBody @Valid LoginRequest loginRequest)
		throws InvalidCredentialsException
	{
		return this.userService.getLoginMeta(loginRequest);
	}

	/**
	 * User requests to change their password.
	 **/
	@PostMapping("/auth/changepassword")
	public LoginResponse postChangePassword(@RequestBody @Valid ChangePasswordRequest userInfo)
		throws InvalidCredentialsException
	{
		return this.userService.changeUserPassword(userInfo);
	}
}
