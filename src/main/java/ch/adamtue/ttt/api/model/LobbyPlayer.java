package ch.adamtue.ttt.api.model;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;

public class LobbyPlayer {
    private String playerId;
    private String displayName;
    private boolean isReady;

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }
    
    public static LobbyPlayer createFromQuery(Map<String, AttributeValue> item) {
        LobbyPlayer player = new LobbyPlayer();
        player.setDisplayName(item.getOrDefault("displayName", null).s());
        player.setReady(item.getOrDefault("isReady", null).bool());
        player.setPlayerId(item.get("sk").s().split("#")[2]);
        
        return player;
    }
}
