package ch.adamtue.ttt.api.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class ChangeUserRoleRequest {
	@NotNull
	@NotBlank
	private String userId;

	@NotNull
	@NotBlank
	private String newRole;

	// Empty constructor for serialization
	public ChangeUserRoleRequest() {}

	// UserId
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	// NewRole
	public String getNewRole() {
		return newRole;
	}

	public void setNewRole(String newRole) {
		this.newRole = newRole;
	}
}
