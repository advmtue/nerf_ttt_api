package ch.adamtue.ttt.api.dto.response;

public class HandledErrorResponse {
	public final String message;
	public final String code;

	public HandledErrorResponse(String message, String code) {
		this.message = message;
		this.code = code;
	}
}
