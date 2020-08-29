package ch.adamtue.ttt.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

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

        // Optional post-lobby phase
        gm.setDateLaunched(item.get("dateLaunched") == null ? null : item.get("dateLaunched").s());
        gm.setDateStarted(item.get("dateStarted") == null ? null : item.get("dateStarted").s());
        gm.setDateEnded(item.get("dateEnded") == null ? null : item.get("dateEnded").s());
        gm.setWinningTeam(item.get("winningTeam") == null ? null : item.get("winningTeam").s());
        return gm;
    }
}
