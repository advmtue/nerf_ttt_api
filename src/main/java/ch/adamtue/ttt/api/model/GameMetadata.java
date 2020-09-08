package ch.adamtue.ttt.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import javax.management.Attribute;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GameMetadata {
    private String gameId;
    private String name;
    private String dateCreated;
    private String dateLaunched;
    private String dateStarted;
    private String dateEnded;
    private String winningTeam;
    private long playerCount;
    private String status;
    private String ownerName;
    private String ownerId;

    /**
     * Create a DynamoDB PK for a GameMetadata object
     *
     * @param gameId Game UUID
     * @return GameMetaData PK
     */
    public static Map<String, AttributeValue> createPK(String gameId) {
        return new HashMap<String, AttributeValue>(Map.of(
                "pk", createHashKey(gameId),
                "sk", createRangeKey()
        ));
    }

    /**
     * Create DynamoDB hash key for Game Metadata
     *
     * @param gameId Game UUID
     * @return DynamoDB hash key
     */
    public static AttributeValue createHashKey(String gameId) {
        return AttributeValue.builder().s(String.format("GAME#%s", gameId)).build();
    }

    /**
     * Create DynamoDB range key for Game Metadata
     *
     * @return DynamoDB range key
     */
    public static AttributeValue createRangeKey() {
        return AttributeValue.builder().s("metadata").build();
    }

    /**
     * Marshall a Query/GetItem result
     *
     * @param item Query/GetItem request
     * @return {GameMetadata} Game metadata
     */
    public static GameMetadata createFromQuery(Map<String, AttributeValue> item) {
        // Always exist
        GameMetadata gm = new GameMetadata();
        gm.setOwnerName(item.get("ownerName").s());
        gm.setOwnerId(item.get("ownerId").s());
        gm.setGameId(item.get("pk").s().split("#")[1]); // TODO : Cleanup
        gm.setDateCreated(item.get("GSI1-SK").s());
        gm.setStatus(item.get("GSI1-PK").s());
        gm.setName(item.get("lobbyName").s());
        gm.setPlayerCount(Long.parseLong(item.get("playerCount").n()));

        if (item.containsKey("dateLaunched")) {
            gm.setDateLaunched(item.get("dateLaunched").s());
        }

        if (item.containsKey("dateStarted")) {
            gm.setDateStarted(item.get("dateStarted").s());
        }

        if (item.containsKey("dateEnded")) {
            gm.setDateEnded(item.get("dateEnded").s());
        }

        if (item.containsKey("winningTeam")) {
            gm.setWinningTeam(item.get("winningTeam").s());
        }

        return gm;
    }

    // DynamoDB
    @JsonIgnore
    public String getPk() { return String.format("GAME#%s", this.gameId); }

    @JsonIgnore
    public String getSk() { return "metadata"; }

    public String getGameId() { return this.gameId; }
    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getName() { return name; }
    public void setName(String name) {
        this.name = name;
    }

    public String getDateCreated() { return dateCreated; }
    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getDateLaunched() { return dateLaunched; }
    public void setDateLaunched(String dateLaunched) {
        this.dateLaunched = dateLaunched;
    }

    public String getDateStarted() { return dateStarted; }
    public void setDateStarted(String dateStarted) {
        this.dateStarted = dateStarted;
    }

    public String getDateEnded() { return dateEnded; }
    public void setDateEnded(String dateEnded) {
        this.dateEnded = dateEnded;
    }

    public String getWinningTeam() { return winningTeam; }
    public void setWinningTeam(String winningTeam) {
        this.winningTeam = winningTeam;
    }

    public long getPlayerCount() { return playerCount; }
    public void setPlayerCount(long playerCount) {
        this.playerCount = playerCount;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
    
}
