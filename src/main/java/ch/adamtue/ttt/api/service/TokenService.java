package ch.adamtue.ttt.api.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import ch.adamtue.ttt.api.model.TokenInfo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

// TODO : Research JWT and how I should be using these
@Service
public class TokenService {

	final Algorithm algorithm;
	final JWTVerifier verifier;

	public TokenService() {
		this.algorithm = Algorithm.HMAC256("secretChangeMeLater");
		this.verifier = JWT.require(this.algorithm).build();
	}

	/**
	 * Create a new token from a TokenInfo
	 * @param info Token information (claims)
	 * @return JWT String
	 */
	public String createToken(TokenInfo info) {
		return JWT.create()
			.withClaim("userId", info.getUserId())
			.withClaim("accessRole", info.getAccessRole())
			.withClaim("name", info.getName())
			.sign(this.algorithm);
	}

	/**
	 * Perform verification of a token, and extract claims into a TokenInfo object.
	 * @param token String JWT
	 * @return Token information representation
	 */
	public TokenInfo verifyToken(String token) {
		DecodedJWT jwt = this.verifier.verify(token);

		TokenInfo tokenInfo = new TokenInfo();
		tokenInfo.setUserId(jwt.getClaim("userId").asString());
		tokenInfo.setAccessRole(jwt.getClaim("accessRole").asString());
		tokenInfo.setName(jwt.getClaim("name").asString());

		return tokenInfo;
	}

	/**
	 * Create a user principal from token information
	 * @param tokenInfo TokenInfo object of a user token
	 * @return Authentication Token
	 */
	public UsernamePasswordAuthenticationToken createPrincipal(TokenInfo tokenInfo) {
		GrantedAuthority authority = new SimpleGrantedAuthority(tokenInfo.getAccessRole());
		ArrayList<GrantedAuthority> authorities = new ArrayList<>( List.of(authority) );

		// Create some authentication
		return new UsernamePasswordAuthenticationToken(tokenInfo, null, authorities);
	}
}
