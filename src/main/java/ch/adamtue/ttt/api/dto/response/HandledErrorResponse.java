package ch.adamtue.ttt.api.dto.response;

public class HandledErrorResponse {
	public String message;
	public String code;

	public HandledErrorResponse(String message, String code) {
		this.message = message;
		this.code = code;
	}
}
