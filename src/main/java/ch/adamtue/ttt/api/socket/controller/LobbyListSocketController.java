package ch.adamtue.ttt.api.socket.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class LobbyListSocketController {

	@MessageMapping("/test")
	@SendTo("/topic/test")
	public String testMapping(String message, @AuthenticationPrincipal Principal testPrincipal) {
		return "Hello world!";
	}
}
