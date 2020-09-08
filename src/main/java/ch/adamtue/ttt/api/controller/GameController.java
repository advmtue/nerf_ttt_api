package ch.adamtue.ttt.api.controller;

import ch.adamtue.ttt.api.model.GamePlayer;
import ch.adamtue.ttt.api.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/game")
public class GameController {
    @Autowired
    private GameService gameService;

    @GetMapping("{gameId}/players")
    private List<GamePlayer> getGamePlayers(@PathVariable String gameId) {
        // Get the player list from service
        List<GamePlayer> players = this.gameService.getGamePlayersCached(gameId);

        // Perform filtering for the user

        return players;
    }
}
