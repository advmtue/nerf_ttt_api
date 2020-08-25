package ch.adamtue.ttt.api.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class CreateUserRequest {
	@NotBlank
	@NotNull
	private String username;

	@NotBlank
	@NotNull
	private String defaultPassword;

	@NotBlank
	@NotNull
	private String displayName;

	// Get
	public String getUsername() { return this.username; }
	public String getDefaultPassword() { return this.defaultPassword; }

	// Set
	public void setUsername(String username) {
		this.username = username;
	}

	public void setDefaultPassword(String defaultPassword) {
		this.defaultPassword = defaultPassword;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

}
