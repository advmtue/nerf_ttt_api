package ch.adamtue.ttt.api.controller;

import java.security.Principal;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import ch.adamtue.ttt.api.dto.request.ChangePasswordRequest;
import ch.adamtue.ttt.api.dto.request.LoginRequest;
import ch.adamtue.ttt.api.dto.response.LoginResponse;
import ch.adamtue.ttt.api.exception.InvalidCredentialsException;
import ch.adamtue.ttt.api.service.UserService;

@RestController
public class AuthController {

	@Autowired
	private UserService userService;

	/**
	 * Anonymous user requests a login token
	 **/
	@CrossOrigin
	@PostMapping("/auth/login")
	public LoginResponse postLogin(@RequestBody @Valid LoginRequest loginRequest)
		throws InvalidCredentialsException
	{
		return this.userService.getLoginMeta(loginRequest);
	}

	/**
	 * User requests to change their password.
	 **/
	@PostMapping("/auth/changepassword")
	public LoginResponse postChangePassword(@AuthenticationPrincipal Principal user, @RequestBody @Valid ChangePasswordRequest userInfo)
		throws InvalidCredentialsException
	{
		return this.userService.changeUserPassword(userInfo);
	}
}
