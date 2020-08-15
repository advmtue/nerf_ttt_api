package ch.adamtue.ttt.api.service;

import ch.adamtue.ttt.api.exception.FailedPasswordHashException;

public interface PasswordService {

	/**
	 * Create a password hash from password and salt.
	 *
	 * @param password Cleartext password
	 * @param salt Salt bytes
	 **/
	public byte[] createPasswordHash(String password, byte[] saltString)
		throws FailedPasswordHashException;

	/**
	 * Generate a new salt.
	 **/
	public byte[] generateSalt();

	/**
	 * Check if a password matches a hash
	 * @param password Plain text password
	 * @param salt salt
	 * @param passwordHash Pre-determined hash
	 **/
	public boolean isPasswordMatch(String password, byte[] salt, byte[] passwordHash)
		throws FailedPasswordHashException;
}

