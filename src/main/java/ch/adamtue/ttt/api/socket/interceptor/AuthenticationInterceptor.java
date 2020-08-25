package ch.adamtue.ttt.api.socket.interceptor;

import java.util.List;

import ch.adamtue.ttt.api.model.TokenInfo;
import ch.adamtue.ttt.api.service.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

import ch.adamtue.ttt.api.dao.DatabaseService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationInterceptor implements ChannelInterceptor {

	@Autowired
	private TokenService tokenService;
	private Logger logger = LoggerFactory.getLogger(AuthenticationInterceptor.class);
	
	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

		if (StompCommand.CONNECT.equals(accessor.getCommand())) {
			List<String> authTokenHeaderList = accessor.getNativeHeader("authToken");
			
			// Fail if no auth header was set
			if (authTokenHeaderList == null || authTokenHeaderList.get(0) == null) {
				System.out.println("Connection presented invalid auth header token");
				return message;
			}
			
			String authToken = authTokenHeaderList.get(0);
			
			try {
				TokenInfo tokenInfo = this.tokenService.verifyToken(authToken);
				UsernamePasswordAuthenticationToken userPrincipal = this.tokenService.createPrincipal(tokenInfo);
				accessor.setUser(userPrincipal);
			} catch (Exception e) {
				logger.warn("Caught exception verifying user token on WebSocket connection");
				e.printStackTrace();
			}
		}

		return message;
	}
}
