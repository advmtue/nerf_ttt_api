package ch.adamtue.ttt.api.model;


public class TokenInfo {
	private String userId;
	private String accessRole;

	// UserId
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}

	// AccessRole
	public String getAccessRole() {
		return accessRole;
	}
	public void setAccessRole(String accessRole) {
		this.accessRole = accessRole;
	}
}
