package ch.adamtue.ttt.api.service;

import ch.adamtue.ttt.api.model.GamePlayer;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutRequest;
import software.amazon.awssdk.services.dynamodb.model.WriteRequest;

import java.util.*;

public class GameStateService {
    /**
     * Assign roles to a player list
     *
     * @param players Player list
     * @return Player list with roles assigned
     */
    public static List<GamePlayer> assignRoles(List<GamePlayer> players) {
        final long playerCount = players.size();
        
        /*
            Ratio Examples
                4 players  = 1 traitor | 1 detective
                5 players  = 2 traitor | 1 detective
                8 players  = 2 traitor | 2 detective
                10 players = 3 traitor | 2 detective
         */
        
        long traitorRemaining = (playerCount / 5) + 1;
        long detectiveRemaining = (playerCount / 8) + 1;
        
        // PRNG
        Random r = new Random();
        List<GamePlayer> assignedPlayers = new ArrayList<>();
        
        while (players.size() > 0) {
            // Pick a random player
            int idx = r.nextInt(players.size());
            GamePlayer pl = players.get(idx);
            
            // Set player as alive
            pl.setAlive(true);
            
            if (traitorRemaining > 0) {
                // Assign to traitor
                pl.setRole("TRAITOR");
                traitorRemaining--;
            } else if (detectiveRemaining > 0) {
                pl.setRole("DETECTIVE");
                detectiveRemaining--;
            } else {
                pl.setRole("INNOCENT");
            }

            // Remove from list and assign to other list
            assignedPlayers.add(pl);
            players.remove(idx);
        }
        
        return assignedPlayers;
    }
    
    public static BatchWriteItemRequest createWriteRequest(List<GamePlayer> playerList, String gameId, String tableName) {
        List<WriteRequest> writeRequests = new ArrayList<>();
        
        for (GamePlayer player : playerList) {
            // Create item attributes
            Map<String, AttributeValue> item = new HashMap<>(Map.of(
                    "pk", GamePlayer.createHashKey(gameId),
                    "sk", GamePlayer.createRangeKey(player.getPlayerId()),
                    "displayName", AttributeValue.builder().s(player.getDisplayName()).build(),
                    "ready", AttributeValue.builder().bool(player.isReady()).build(),
                    "role", AttributeValue.builder().s(player.getRole()).build(),
                    "alive", AttributeValue.builder().bool(player.isAlive()).build()
            ));

            // Create a PR
            PutRequest pr = PutRequest.builder()
                    .item(item)
                    .build();

            // Create a WR
            WriteRequest wr = WriteRequest.builder()
                    .putRequest(pr)
                    .build();

            // Insert the request
            writeRequests.add(wr);
        }

        return BatchWriteItemRequest.builder()
            .requestItems(Collections.singletonMap(tableName, writeRequests))
            .build();
    }
}
