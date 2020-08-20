package ch.adamtue.ttt.api.dto.response;

import ch.adamtue.ttt.api.dto.request.ChangeUserRoleRequest;

public class ChangeUserRoleResponse extends ChangeUserRoleRequest {
	public ChangeUserRoleResponse() {
		super();
	}

	public ChangeUserRoleResponse(ChangeUserRoleRequest request) {
		super();
		this.setUserId(request.getUserId());
		this.setNewRole(request.getNewRole());
	}
}
