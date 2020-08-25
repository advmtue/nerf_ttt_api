package ch.adamtue.ttt.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

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
}
