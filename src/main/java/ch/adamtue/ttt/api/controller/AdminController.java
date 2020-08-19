package ch.adamtue.ttt.api.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import ch.adamtue.ttt.api.dto.request.CreateUserRequest;
import ch.adamtue.ttt.api.dto.response.CreateUserResponse;
import ch.adamtue.ttt.api.exception.UserAlreadyExistsException;
import ch.adamtue.ttt.api.service.UserService;

@RestController
public class AdminController {

	@Autowired @Qualifier("UserService")
	private UserService userService;

	/**
	 * Add a new user.
	 **/
	@PutMapping("/admin/user")
	public CreateUserResponse postCreateUser(@RequestBody @Valid CreateUserRequest userInfo)
		throws UserAlreadyExistsException
	{
		return this.userService.createNewUser(userInfo);
	}

}
