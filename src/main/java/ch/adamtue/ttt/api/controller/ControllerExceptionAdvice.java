package ch.adamtue.ttt.api.controller;

import ch.adamtue.ttt.api.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import ch.adamtue.ttt.api.dto.response.HandledErrorResponse;

@ControllerAdvice
public class ControllerExceptionAdvice {

	@ExceptionHandler({InvalidCredentialsException.class})
	public ResponseEntity<HandledErrorResponse> handle(InvalidCredentialsException e) {
		HandledErrorResponse err = new HandledErrorResponse(
				"Invalid Credentials",
				"ERR_INVALID_CREDENTIALS");

		return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler({UserAlreadyExistsException.class})
	public ResponseEntity<HandledErrorResponse> handle(UserAlreadyExistsException e) {
		HandledErrorResponse err = new HandledErrorResponse(
				"A user with matching username already exists.",
				"ERR_USER_EXISTS");

		return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler({DefaultInternalError.class})
	public ResponseEntity<HandledErrorResponse> handle(DefaultInternalError e) {
		HandledErrorResponse err = new HandledErrorResponse(
				"An unexpected error occurred. This is unusual.",
				"ERR_INTERNAL_UNEXPECTED");

		return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler({FailedPasswordHashException.class})
	public ResponseEntity<HandledErrorResponse> handle(FailedPasswordHashException e) {
		HandledErrorResponse err = new HandledErrorResponse(
				"An error occurred when handling password hashing.",
				"ERR_PASSWORD_HASHING");

		return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler({PasswordNotChangeableException.class})
	public ResponseEntity<HandledErrorResponse> handle(PasswordNotChangeableException e) {
		HandledErrorResponse err = new HandledErrorResponse(
				"Password is not currently changeable.",
				"ERR_PASSWORD_NOT_CHANGEABLE"
				);

		return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler({GameIsNotLobbyException.class})
	public ResponseEntity<HandledErrorResponse> handle(GameIsNotLobbyException e) {
		HandledErrorResponse err = new HandledErrorResponse(
				"Requested game is not in lobby phase",
				"ERR_GAME_NOT_LOBBY"
		);

		return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler({NotEnoughPlayersException.class})
	public ResponseEntity<HandledErrorResponse> handle(NotEnoughPlayersException e) {
		HandledErrorResponse err = new HandledErrorResponse(
				"Not enough players to start game",
				"ERR_PLAYER_COUNT_NOT_MET"
		);

		return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler({PlayerNotGameOwnerException.class})
	public ResponseEntity<HandledErrorResponse> handle(PlayerNotGameOwnerException e) {
		HandledErrorResponse err = new HandledErrorResponse(
				"Only the game owner can perform this action",
				"ERR_PLAYER_NOT_OWNER"
		);

		return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler({PlayersNotReadyException.class})
	public ResponseEntity<HandledErrorResponse> handle(PlayersNotReadyException e) {
		HandledErrorResponse err = new HandledErrorResponse(
				"Not all players are ready",
				"ERR_PLAYERS_NOT_READY"
		);

		return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
