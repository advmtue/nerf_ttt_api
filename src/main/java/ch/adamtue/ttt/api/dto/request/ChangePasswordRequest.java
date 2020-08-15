package ch.adamtue.ttt.api.dto.request;

public class ChangePasswordRequest {
	private String username;
	private String newPassword;
	private String defaultPassword;

	public String getUsername() { return this.username; }
	public String getNewPassword() { return this.newPassword; }
	public String getDefaultPassword() { return this.defaultPassword; }

	public void setUsername(String username) {
		this.username = username;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public void setDefaultPassword(String defaultPassword) {
		this.defaultPassword = defaultPassword;
	}
}
