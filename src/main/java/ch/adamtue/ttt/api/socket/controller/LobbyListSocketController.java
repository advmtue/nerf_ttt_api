package ch.adamtue.ttt.api.socket.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class LobbyListSocketController {

	@MessageMapping("/test")
	@SendTo("/topic/test")
	public String testMapping(String message) {
		System.out.println("Something hit this");
		return "Hello world!";
	}
}
