package ch.adamtue.ttt.api.model;


public class TokenInfo {
	private String userId;
	private String name;
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

	// Username
	public String getName() {
		return this.name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
