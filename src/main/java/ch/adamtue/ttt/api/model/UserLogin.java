package ch.adamtue.ttt.api.model;

public class UserLogin {
	// DynamoDB
	private String PK;
	private String SK;

	private String userId;
	private byte[] passwordHash;
	private byte[] passwordSalt;
	private boolean passwordChangeOnLogin;
	private String passwordResetValue;

	public UserLogin() {}

	// GET
	public String getPK() { return this.PK; }
	public String getSK() { return this.SK; }
	public String getUserId() { return this.userId; }
	public byte[] getPasswordHash() { return this.passwordHash.clone(); }
	public boolean getPasswordChangeOnLogin() { return this.passwordChangeOnLogin; }
	public String getPasswordResetValue() { return this.passwordResetValue; }
	public byte[] getPasswordSalt() { return this.passwordSalt.clone(); }

	// SET
	public void setPK(String PK) {
		this.PK = PK;
	}

	public void setSK(String SK) {
		this.SK = SK;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public void setPasswordHash(byte[] passwordHash) {
		this.passwordHash = passwordHash.clone();
	}

	public void setPasswordChangeOnLogin(boolean passwordChangeOnLogin) {
		this.passwordChangeOnLogin = passwordChangeOnLogin;
	}

	public void setPasswordResetValue(String passwordResetValue) {
		this.passwordResetValue = passwordResetValue;
	}

	public void setPasswordSalt(byte[] passwordSalt) {
		this.passwordSalt = passwordSalt.clone();
	}

	public UserLogin clone() {
		UserLogin ul = new UserLogin();
		ul.setPK(this.PK);
		ul.setSK(this.SK);
		ul.setUserId(this.userId);
		ul.setPasswordHash(this.passwordHash.clone());
		ul.setPasswordChangeOnLogin(this.passwordChangeOnLogin);
		ul.setPasswordResetValue(this.passwordResetValue);
		ul.setPasswordSalt(this.passwordSalt.clone());

		return ul;
	}
}

