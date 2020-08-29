package ch.adamtue.ttt.api.dto.response;

public class LobbyPlayerChange {
    private String lobbyId;
    private long playerCount;
    
    public LobbyPlayerChange() {}
    
    public LobbyPlayerChange(String lobbyId, long playerCount) {
        this.lobbyId = lobbyId;
        this.playerCount = playerCount;
    }

    public String getLobbyId() {
        return lobbyId;
    }

    public void setLobbyId(String lobbyId) {
        this.lobbyId = lobbyId;
    }

    public long getPlayerCount() {
        return playerCount;
    }

    public void setPlayerCount(long playerCount) {
        this.playerCount = playerCount;
    }
}
