package ch.adamtue.ttt.api.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "ttt_testing")
public class UserLogin {

	@DynamoDBHashKey(attributeName = "pk")
	private String PK;

	@DynamoDBIndexRangeKey(attributeName = "GSI1-SK")
	@DynamoDBRangeKey(attributeName = "sk")
	private String SK;

	@DynamoDBIndexHashKey(attributeName = "GSI1-PK")
	private String userId;

	@DynamoDBAttribute(attributeName = "passwordHash")
	private byte[] passwordHash;

	@DynamoDBAttribute(attributeName = "passwordSalt")
	private byte[] passwordSalt;

	@DynamoDBAttribute(attributeName = "passwordChangeOnLogin")
	private boolean passwordChangeOnLogin;

	@DynamoDBAttribute(attributeName = "passwordResetValue")
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

