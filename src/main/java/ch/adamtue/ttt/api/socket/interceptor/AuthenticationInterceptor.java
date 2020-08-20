package ch.adamtue.ttt.api.socket.interceptor;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

import ch.adamtue.ttt.api.dao.DatabaseService;


public class AuthenticationInterceptor implements ChannelInterceptor {

	@Autowired @Qualifier("DatabaseServiceDynamo")
	private DatabaseService databaseService;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

		if (StompCommand.CONNECT.equals(accessor.getCommand())) {
			System.out.println("Got new connection");

			List<String> authTokenHeaderList = accessor.getNativeHeader("authToken");

			if (authTokenHeaderList == null) {
				System.out.println("Should fail");
				return null;
			} else {
				String authToken = authTokenHeaderList.get(0);
				System.out.println(authToken);
			}
		}

		return message;
	}
}
