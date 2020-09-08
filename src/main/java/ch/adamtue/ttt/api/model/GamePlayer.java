package ch.adamtue.ttt.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.w3c.dom.Attr;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import javax.management.Attribute;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GamePlayer {
    private String playerId;
    private String displayName;
    private boolean ready;
    private String role;
    private boolean alive;

    /**
     * Create DynamoDB PK for Game Player
     *
     * @param gameId Game UUID
     * @param playerId Player ID
     * @return
     */
    public static Map<String, AttributeValue> createPK(String gameId, String playerId) {
        return new HashMap<String, AttributeValue>(Map.of(
                "pk", createHashKey(gameId),
               "sk", createRangeKey(playerId)
        ));
    }

    /**
     * Create DynamoDB hash key for Game
     *
     * @param gameId Game UUID
     * @return DynamoDB hash key for Game
     */
    public static AttributeValue createHashKey(String gameId) {
        return GameMetadata.createHashKey(gameId);
    }

    /**
     * Create DynamoDb range key for Game Player
     *
     * @param playerId Player UUID
     * @return DynamoDB range key for Game Player
     */
    public static AttributeValue createRangeKey(String playerId) {
        return AttributeValue.builder().s(String.format("PLAYER#%s", playerId)).build();
    }

    /**
     * Construct a range key for non-player specific query
     *
     * @return DynamoDB range key for any game player
     */
    public static AttributeValue createEmptyRangeKey() {
        return AttributeValue.builder().s("PLAYER#").build();
    }

    /**
     * Marshall a Query/GetItem response into a GamePlayer object
     *
     * @param item Query attribute map
     * @return {GamePlayer} Game player object
     */
    public static GamePlayer createFromQuery(Map<String, AttributeValue> item) {
        GamePlayer player = new GamePlayer();

        // Always available
        player.setPlayerId(item.get("sk").s().split("#")[1]);

        if (item.containsKey("displayName")) {
            player.setDisplayName(item.get("displayName").s());
        }

        if (item.containsKey("ready")) {
            player.setReady(item.get("ready").bool());
        }

        if (item.containsKey("alive")) {
            player.setAlive(item.get("alive").bool());
        }

        if (item.containsKey("role")) {
            player.setRole(item.get("role").s());
        }

        return player;
    }

    /**
     * Construct an anonymous (role/alive filtered) version of this player
     *
     * @param otherRole The other player's role
     * @return {GamePlayer} New anonymized Game Player object
     */
    @JsonIgnore
    public GamePlayer anonymize(String otherRole) {
        GamePlayer gp = new GamePlayer();

        // Always available
        gp.setDisplayName(this.getDisplayName());
        gp.setPlayerId(this.getPlayerId());
        gp.setReady(this.isReady());
        gp.setAlive(this.isAlive());

        // If the role has been set
        if (this.getRole() != null) {
           // If local role is detective or the same as requesting player
           if (this.getRole() == "DETECTIVE" || this.getRole() == otherRole) {
               // Set role to current role
               this.setRole(this.getRole());
           } else {
               // Hide role as innocent
               this.setRole("INNOCENT");
           }
        }

        return gp;
    }

    // Player ID
    public String getPlayerId() {
        return playerId;
    }
    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    // Display name
    public String getDisplayName() {
        return displayName;
    }
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    // Ready
    public boolean isReady() {
        return this.ready;
    }
    public void setReady(boolean ready) {
        this.ready = ready;
    }

    // Role
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }

    // Alive
    public boolean isAlive() {
        return alive;
    }
    public void setAlive(boolean alive) {
        this.alive = alive;
    }
}
