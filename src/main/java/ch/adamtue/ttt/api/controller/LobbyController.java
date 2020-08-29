package ch.adamtue.ttt.api.controller;

import ch.adamtue.ttt.api.dao.DatabaseService;
import ch.adamtue.ttt.api.dto.request.CreateLobbyRequest;
import ch.adamtue.ttt.api.model.GameMetadata;
import ch.adamtue.ttt.api.model.LobbyPlayer;
import ch.adamtue.ttt.api.model.TokenInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
    
    @GetMapping("/lobby/{lobbyId}")
    public GameMetadata getIndividualLobby(@PathVariable String lobbyId) {
        return this.databaseService.getLobby(lobbyId);
    }
    
    @GetMapping("/lobby/{lobbyId}/players")
    public List<LobbyPlayer> getLobbyPlayers(@PathVariable String lobbyId) {
        return this.databaseService.getLobbyPlayers(lobbyId);
    }
    
    @PatchMapping("/lobby/{lobbyId}/join")
    public boolean playerJoinLobby(@AuthenticationPrincipal UsernamePasswordAuthenticationToken userPrincipal, @PathVariable String lobbyId) {
        TokenInfo userInfo = (TokenInfo) userPrincipal.getPrincipal();
        this.databaseService.playerJoinLobby(userInfo, lobbyId);
        
        // Send to listening channels
        LobbyPlayer newPlayer = new LobbyPlayer();
        newPlayer.setReady(false);
        newPlayer.setPlayerId(userInfo.getUserId());
        newPlayer.setDisplayName(userInfo.getName());
        this.messageTemplate.convertAndSend(String.format("/topic/lobby/%s/playerjoin", lobbyId), newPlayer);

        GameMetadata gameData = this.databaseService.getLobby(lobbyId);
        this.messageTemplate.convertAndSend("/topic/lobbies/updated", gameData);

        return true;
    }
    
    @PatchMapping("/lobby/{lobbyId}/leave")
    public boolean playerLeaveLobby(@AuthenticationPrincipal UsernamePasswordAuthenticationToken userPrincipal, @PathVariable String lobbyId) {
        TokenInfo userInfo = (TokenInfo) userPrincipal.getPrincipal();
        this.databaseService.playerLeaveLobby(userInfo, lobbyId);

        // Send to listening channels
        LobbyPlayer newPlayer = new LobbyPlayer();
        newPlayer.setReady(false);
        newPlayer.setPlayerId(userInfo.getUserId());
        newPlayer.setDisplayName(userInfo.getName());
        this.messageTemplate.convertAndSend(String.format("/topic/lobby/%s/playerleave", lobbyId), newPlayer);

        GameMetadata gameData = this.databaseService.getLobby(lobbyId);
        this.messageTemplate.convertAndSend("/topic/lobbies/updated", gameData);

        return true;
    }

    @PatchMapping("/lobby/{lobbyId}/ready")
    public boolean playerSetReady(@AuthenticationPrincipal UsernamePasswordAuthenticationToken userPrincipal, @PathVariable String lobbyId) {
        TokenInfo userInfo = (TokenInfo) userPrincipal.getPrincipal();
        this.databaseService.playerSetReady(userInfo, lobbyId);
        
        // Send to listening channels
        // Todo new dto
        LobbyPlayer newPlayer = new LobbyPlayer();
        newPlayer.setReady(true);
        newPlayer.setPlayerId(userInfo.getUserId());
        newPlayer.setDisplayName(userInfo.getName());
        this.messageTemplate.convertAndSend(String.format("/topic/lobby/%s/playerready", lobbyId), newPlayer);
        
        return true;
    }
    
    @PatchMapping("/lobby/{lobbyId}/unready")
    public boolean playerSetUnready(@AuthenticationPrincipal UsernamePasswordAuthenticationToken userPrincipal, @PathVariable String lobbyId) {
        TokenInfo userInfo = (TokenInfo) userPrincipal.getPrincipal();
        this.databaseService.playerSetUnready(userInfo, lobbyId);

        // Send to listening channels
        // TODO new dto
        LobbyPlayer newPlayer = new LobbyPlayer();
        newPlayer.setReady(false);
        newPlayer.setPlayerId(userInfo.getUserId());
        newPlayer.setDisplayName(userInfo.getName());
        this.messageTemplate.convertAndSend(String.format("/topic/lobby/%s/playerunready", lobbyId), newPlayer);

        return true;
    }
}
