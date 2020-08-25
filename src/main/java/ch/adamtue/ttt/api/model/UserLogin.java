package ch.adamtue.ttt.api.model;

public class UserLogin {
	private String PK;
	private String SK;
	private String userId;
	private byte[] passwordHash;
	private byte[] passwordSalt;
	private boolean passwordChangeOnLogin;
	private String passwordResetValue;

	public UserLogin() {}

	// PK
	public String getPK() { return PK; }
	public void setPK(String pK) {
		PK = pK;
	}

	// SK
	public String getSK() { return SK; }
	public void setSK(String sK) {
		SK = sK;
	}

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
}

