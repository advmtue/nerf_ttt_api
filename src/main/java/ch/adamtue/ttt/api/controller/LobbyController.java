package ch.adamtue.ttt.api.controller;

import ch.adamtue.ttt.api.dao.DatabaseService;
import ch.adamtue.ttt.api.dto.request.CreateLobbyRequest;
import ch.adamtue.ttt.api.model.GameMetadata;
import ch.adamtue.ttt.api.model.TokenInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
public class LobbyController {

    @Autowired @Qualifier("DatabaseServiceDynamo")
    private DatabaseService databaseService;
    private final Logger logger = LoggerFactory.getLogger(LobbyController.class);
    
    @Autowired
    private SimpMessagingTemplate messageTemplate;

    /**
     * Admin-only mapping for creating new lobbies
     */
    @PostMapping("/lobby")
    public GameMetadata createNewLobby(@AuthenticationPrincipal UsernamePasswordAuthenticationToken userPrincipal, @RequestBody @Valid CreateLobbyRequest lobbyInfo) {
        // Extra user information from the Authentication Principal
        TokenInfo userInfo = (TokenInfo) userPrincipal.getPrincipal();
        
        // Create the lobby
        GameMetadata gameInfo = this.databaseService.createNewLobby(userInfo, lobbyInfo);

        // Push information to sockets
        this.messageTemplate.convertAndSend("/topic/lobbies/new", gameInfo);

        // Return lobby ID to owner
        return gameInfo;
    }

    @GetMapping("/lobby")
    public List<GameMetadata> getLobbyList() {
        return this.databaseService.getLobbyList();
    }
}
