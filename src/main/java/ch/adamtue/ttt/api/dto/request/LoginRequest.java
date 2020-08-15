package ch.adamtue.ttt.api.dto.request;

import javax.validation.constraints.NotBlank;

import org.springframework.lang.NonNull;

public class LoginRequest {
	@NotBlank
	@NonNull
	private String username;

	@NotBlank
	@NonNull
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
