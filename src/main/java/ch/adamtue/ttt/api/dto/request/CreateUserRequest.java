package ch.adamtue.ttt.api.dto.request;

import javax.validation.constraints.NotBlank;

import org.springframework.lang.NonNull;

public class CreateUserRequest {
	@NotBlank
	@NonNull
	private String username;

	@NotBlank
	@NonNull
	private String defaultPassword;

	@NotBlank
	@NonNull
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
