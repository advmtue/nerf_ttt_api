package ch.adamtue.ttt.api.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import org.springframework.stereotype.Repository;

import ch.adamtue.ttt.api.model.TokenInfo;

@Repository("TokenServiceJwt")
public class TokenServiceImpl implements TokenService {

	Algorithm algorithm;
	JWTVerifier verifier;

	public TokenServiceImpl() {
		this.algorithm = Algorithm.HMAC256("secretChangeMeLater");

		this.verifier = JWT.require(this.algorithm).build();
	}

	public String createToken(TokenInfo info) {
		return JWT.create()
			.withClaim("userId", info.getUserId())
			.withClaim("accessRole", info.getAccessRole())
			.sign(this.algorithm);
	}

	public TokenInfo verifyToken(String token) {
		DecodedJWT jwt = this.verifier.verify(token);

		TokenInfo tokenInfo = new TokenInfo();
		tokenInfo.setUserId(jwt.getClaim("userId").asString());
		tokenInfo.setAccessRole(jwt.getClaim("accessRole").asString());

		return tokenInfo;
	}
}
