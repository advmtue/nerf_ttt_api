package ch.adamtue.ttt.api.service;

import ch.adamtue.ttt.api.dao.DatabaseService;
import ch.adamtue.ttt.api.model.GamePlayer;
import ch.adamtue.ttt.api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class GameService {

    @Autowired
    UserService userService;

    @Autowired
    DatabaseService dbService;

    // TODO: (thought) Do we ever need to invalidate the cache?
    Map<String, List<GamePlayer>> gameCache;

    /**
     * Instantiate a new gamePlayerCache
     */
    public GameService() {
        this.gameCache = new HashMap<String, List<GamePlayer>>();
    }

    public List<GamePlayer> getGamePlayersCached(@PathVariable String lobbyId) {
        // Try to hit the cache
        List<GamePlayer> players = this.gameCache.get(lobbyId);

        // Cache miss
        if (players == null) {
            players = this.dbService.getLobbyPlayers(lobbyId);

            // Populate cache
            this.gameCache.put(lobbyId, players);
        }

        // TODO Set all players to alive and filter roles
        // Maybe add this functionality to the object itself

        return players;
    }
}
