package ch.adamtue.ttt.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import ch.adamtue.ttt.api.dto.response.HandledErrorResponse;
import ch.adamtue.ttt.api.exception.DefaultInternalError;
import ch.adamtue.ttt.api.exception.FailedPasswordHashException;
import ch.adamtue.ttt.api.exception.InvalidCredentialsException;
import ch.adamtue.ttt.api.exception.PasswordNotChangeableException;
import ch.adamtue.ttt.api.exception.UserAlreadyExistsException;

@ControllerAdvice
public class AuthControllerAdvice {

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
}
