package ch.adamtue.ttt.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class LoginResponse {
	private String token;
	private Boolean resetPassword;

	public LoginResponse() {}

	// GET
	public Boolean getResetPassword() {
		return this.resetPassword;
	}

	public String getToken() {
		return this.token;
	}

	// SET
	public void setToken(String token) {
		this.token = token;
	}

	public void setResetPassword(Boolean resetPassword) {
		this.resetPassword = resetPassword;
	}
}
