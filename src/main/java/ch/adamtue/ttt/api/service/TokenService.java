package ch.adamtue.ttt.api.service;

import ch.adamtue.ttt.api.model.TokenInfo;

public interface TokenService {
	public String createToken(TokenInfo info);

	public TokenInfo verifyToken(String token);
}
