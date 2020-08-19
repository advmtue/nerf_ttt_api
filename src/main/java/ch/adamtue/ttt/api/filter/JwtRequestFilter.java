package ch.adamtue.ttt.api.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import ch.adamtue.ttt.api.model.TokenInfo;
import ch.adamtue.ttt.api.service.TokenService;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

	@Autowired
	private TokenService tokenService;

	private Logger logger;

	public JwtRequestFilter() {
		logger = LoggerFactory.getLogger(JwtRequestFilter.class);
	}


	/**
	 * Run a filter each time a request is made.
	 * Examine the incoming request for a JWT in the header.
	 **/
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
		throws ServletException, IOException
	{
		final String authHeader = request.getHeader("Authorization");

		if (authHeader == null) {
			logger.warn("No auth header was specified for request.");
			chain.doFilter(request,response);
			return;
		}

		// No header passed or security context has been set
		if (SecurityContextHolder.getContext().getAuthentication() != null ) {
			logger.warn("Security context has already been established for request");
			chain.doFilter(request, response);
			return;
		}

		// No bearer pattern
		if (!authHeader.startsWith("Bearer ")) {
			logger.warn("Request dropped for not following Bearer pattern");
			chain.doFilter(request, response);
			return;
		}

		// Remove "Bearer "
		final String token = authHeader.substring(7);

		// Verify the token internall
		try {
			// Pull token information
			TokenInfo tokenInfo = this.tokenService.verifyToken(token);

			GrantedAuthority authority = new SimpleGrantedAuthority(tokenInfo.getAccessRole());
			ArrayList<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>( List.of(authority) );

			// Create some authentication
			UsernamePasswordAuthenticationToken userToken = new UsernamePasswordAuthenticationToken(
					tokenInfo.getUserId(), null, // Username, Credentials (unused)
					authorities);

			// Assign the security context
			logger.info("Security Context :{}   :{}", tokenInfo.getUserId(), tokenInfo.getAccessRole());
			SecurityContextHolder.getContext().setAuthentication(userToken);
		} catch (Exception e) {
			logger.error("Some error occurred when assigning security context...");
			logger.error(e.toString());
			return;
		}

		// Continue the chain
		chain.doFilter(request, response);
	}
}
