package ch.adamtue.ttt.api.model;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.Map;

public class UserLogin {
    private String username;
	private String userId;
	private byte[] passwordHash;
	private byte[] passwordSalt;
	private boolean passwordChangeOnLogin;
	private String passwordResetValue;

	/**
	 * Create a PK from a given username
	 *
	 * @param username Username
	 * @return Primary key for userlogin lookups
	 */
	public static Map<String, AttributeValue> createPK(String username) {
		return new HashMap<String, AttributeValue>(Map.of(
				"pk", createHashKey(username),
				"sk", createRangeKey()
		));
	}
	
	public static AttributeValue createHashKey(String username) {
		return AttributeValue.builder().s(String.format("LOGIN#%s", username)).build();
	}
	
	public static AttributeValue createRangeKey() {
		return AttributeValue.builder().s("login").build();
	}

	public UserLogin() {}

	// UserId
	public String getUserId() { return userId; }
	public void setUserId(String userId) {
		this.userId = userId;
	}

	// Password Hash
	public byte[] getPasswordHash() { return passwordHash; }
	public void setPasswordHash(byte[] passwordHash) {
		this.passwordHash = passwordHash;
	}

	// Password Salt
	public byte[] getPasswordSalt() { return passwordSalt; }
	public void setPasswordSalt(byte[] passwordSalt) {
		this.passwordSalt = passwordSalt;
	}

	// Password change on login
	public boolean isPasswordChangeOnLogin() { return passwordChangeOnLogin; }
	public void setPasswordChangeOnLogin(boolean passwordChangeOnLogin) {
		this.passwordChangeOnLogin = passwordChangeOnLogin;
	}

	// Password reset value
	public String getPasswordResetValue() { return passwordResetValue; }
	public void setPasswordResetValue(String passwordResetValue) {
		this.passwordResetValue = passwordResetValue;
	}

	// Username
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
}

