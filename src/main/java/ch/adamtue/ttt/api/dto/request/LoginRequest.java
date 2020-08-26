package ch.adamtue.ttt.api.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class LoginRequest {
	@NotBlank
	@NotNull
	private String username;

	@NotBlank
	@NotNull
	private String password;

	public LoginRequest() {}

	// GET
	public String getUsername() {
		return this.username;
	}

	public String getPassword() {
		return this.password;
	}

	// SET
	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
