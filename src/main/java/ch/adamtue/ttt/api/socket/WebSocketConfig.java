package ch.adamtue.ttt.api.socket;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import ch.adamtue.ttt.api.socket.interceptor.AuthenticationInterceptor;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	/**
	 * Add endpoints for websocket connections
	 **/
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/socket")
			.setAllowedOrigins("http://localhost:4200", "https://api.ttt.adamtue.ch");
	}

	/**
	 * Configure endpoints for the message broker and application
	 **/
	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		config.setApplicationDestinationPrefixes("/app");
		config.enableSimpleBroker("/topic", "/queue");
	}

	/**
	 * Create new AuthenticationInterceptor as a bean so spring will manage its lifecycle.
	 * Allows for IoC
	 */
	@Bean
	public AuthenticationInterceptor createInterceptor() {
		return new AuthenticationInterceptor();
	}

	/**
	 * Configure interceptors for client inbound messages
	 */
	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(createInterceptor());
	}

}
