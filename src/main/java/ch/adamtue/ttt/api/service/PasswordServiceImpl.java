package ch.adamtue.ttt.api.service;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.springframework.stereotype.Repository;

import ch.adamtue.ttt.api.exception.FailedPasswordHashException;

@Repository("PasswordServiceDefault")
public class PasswordServiceImpl implements PasswordService {

	public byte[] createPasswordHash(String password, byte[] salt)
		throws FailedPasswordHashException
	{
		// Build a key spec
		PBEKeySpec key = new PBEKeySpec(password.toCharArray(), salt, 65535, 64);

		// Create the key factory
		String algorithm = "PBKDF2WithHmacSHA512";
		SecretKeyFactory keyFactory;
		try {
			keyFactory = SecretKeyFactory.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new FailedPasswordHashException();
		}

		// Create the password hash
		try {
			return keyFactory.generateSecret(key).getEncoded();
		} catch (InvalidKeySpecException e) {
			throw new FailedPasswordHashException();
		}
	}

	public byte[] generateSalt() {
		SecureRandom random = new SecureRandom();
		byte[] randomSalt = new byte[16];
		random.nextBytes(randomSalt);

		return randomSalt;
	}

	public boolean isPasswordMatch(String password, byte[] salt, byte[] hash) {
		byte[] newPasswordHash = this.createPasswordHash(password, salt);
		return Arrays.equals(newPasswordHash, hash);
	}
}
