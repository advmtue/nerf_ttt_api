package ch.adamtue.ttt.api.service;

import com.auth0.jwt.exceptions.JWTCreationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import ch.adamtue.ttt.api.dao.DatabaseService;
import ch.adamtue.ttt.api.dto.request.ChangePasswordRequest;
import ch.adamtue.ttt.api.dto.request.CreateUserRequest;
import ch.adamtue.ttt.api.dto.request.LoginRequest;
import ch.adamtue.ttt.api.dto.response.CreateUserResponse;
import ch.adamtue.ttt.api.dto.response.LoginResponse;
import ch.adamtue.ttt.api.exception.FailedPasswordHashException;
import ch.adamtue.ttt.api.exception.InvalidCredentialsException;
import ch.adamtue.ttt.api.exception.PasswordNotChangeableException;
import ch.adamtue.ttt.api.exception.UserAlreadyExistsException;
import ch.adamtue.ttt.api.model.TokenInfo;
import ch.adamtue.ttt.api.model.UserLogin;

@Repository("UserService")
public class UserServiceImpl implements UserService {

	@Autowired @Qualifier("DatabaseServiceDynamo")
	private DatabaseService dbService;

	@Autowired @Qualifier("PasswordServiceDefault")
	private PasswordService passwordService;

	@Autowired @Qualifier("TokenServiceJwt")
	private TokenService tokenService;

	public LoginResponse getLoginMeta(LoginRequest loginInfo)
		throws FailedPasswordHashException, InvalidCredentialsException
	{

		LoginResponse lr = new LoginResponse();

		UserLogin ul = this.dbService.getUserLogin(loginInfo.getUsername());

		/*
		 * Scenario #1 - No login matches
		 */
		if (ul == null) throw new InvalidCredentialsException();

		/*
		 * Scenario #2 - User login exists:
		 * 	passwordChangeOnLogin is true
		 * 	defaultPassword doesn't match
		 */
		if (ul.getPasswordChangeOnLogin() && !ul.getPasswordResetValue().equals(loginInfo.getPassword())) {
			throw new InvalidCredentialsException();
		}

		/*
		 * Scenario #3 - User login exists:
		 * 	passwordChangeOnLogin is true
		 * 	defaultPassword does match
		 */
		if (ul.getPasswordChangeOnLogin() && ul.getPasswordResetValue().equals(loginInfo.getPassword())) {
			lr.setResetPassword(true);
			return lr;
		}

		/*
		 * Scenario #4 - password is incorrect
		 */
		boolean pwMatch = passwordService.isPasswordMatch(
			loginInfo.getPassword(),
			ul.getPasswordSalt(),
			ul.getPasswordHash());

		if (!pwMatch) throw new InvalidCredentialsException();

		/*
		 * Scenario #5 - User needs to reset their password
		 */
		if (ul.getPasswordChangeOnLogin() == true) {
			lr.setResetPassword(true);
			return lr;
		}

		/*
		 * Scenario #6 - Successful login
		 */
		// Build some token information
		TokenInfo userTokenInfo = this.dbService.getTokenInfo(ul.getUserId());
		String token = this.tokenService.createToken(userTokenInfo);

		lr.setToken(token);
		lr.setResetPassword(false);

		return lr;
	}

	public LoginResponse changeUserPassword(ChangePasswordRequest userInfo)
		throws PasswordNotChangeableException, JWTCreationException
	{
		LoginResponse response = new LoginResponse();

		// Try to change the user's password
		this.dbService.changeUserPassword(userInfo);

		// Generate a token for the response to the login is seamless
		UserLogin ul = this.dbService.getUserLogin(userInfo.getUsername());
		TokenInfo tokenInfo = this.dbService.getTokenInfo(ul.getUserId());

		// Update response
		response.setToken(this.tokenService.createToken(tokenInfo));
		response.setResetPassword(false);

		return response;
	};

	public CreateUserResponse createNewUser(CreateUserRequest userInfo)
		throws UserAlreadyExistsException
	{
		CreateUserResponse response = new CreateUserResponse();
		response.setUsername(userInfo.getUsername());

		// Existence checks are performed by the DB in the single round
		// trip, so offload all responsibility to the DBService.
		this.dbService.createNewUser(userInfo);

		return response;
	};
}
