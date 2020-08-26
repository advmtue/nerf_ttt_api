package ch.adamtue.ttt.api.controller;

import javax.validation.Valid;

import ch.adamtue.ttt.api.dao.DatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.adamtue.ttt.api.dto.request.ChangeUserRoleRequest;
import ch.adamtue.ttt.api.dto.request.CreateUserRequest;
import ch.adamtue.ttt.api.dto.response.ChangeUserRoleResponse;
import ch.adamtue.ttt.api.dto.response.CreateUserResponse;
import ch.adamtue.ttt.api.exception.UserAlreadyExistsException;
import ch.adamtue.ttt.api.service.UserService;

@RestController
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	private UserService userService;
	
	@Autowired @Qualifier("DatabaseServiceDynamo")
	private DatabaseService databaseService;
	
	@Autowired
	private SimpMessagingTemplate messagingTemplate;

	/**
	 * Add a new user.
	 **/
	@PostMapping("/user")
	public CreateUserResponse postCreateUser(@RequestBody @Valid CreateUserRequest userInfo)
		throws UserAlreadyExistsException
	{
		return this.userService.createNewUser(userInfo);
	}

	/**
	 * Change the role of a user
	 * Request body should be a ChangeUserRoleRequest
	 **/
	@PatchMapping("/user/role")
	public ChangeUserRoleResponse changeUserRole(@RequestBody @Valid ChangeUserRoleRequest request) {
		return this.userService.changeUserRole(request);
	}
	
	@PostMapping("/lobby/close")
	public String adminCloseLobby(@RequestBody String lobbyId) {
	    this.databaseService.closeLobby(lobbyId);
	    
	    this.messagingTemplate.convertAndSend("/topic/lobbies/closed", lobbyId);

	    return lobbyId;
	}
}
