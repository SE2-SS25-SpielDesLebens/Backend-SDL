package at.aau.serg.websocketserver.game;

/**
 * Request zum Beitritt zu einem Spiel
 */
public class JoinGameRequest {
    private String gameId;
    private String playerName;
    private int startFieldIndex;

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getStartFieldIndex() {
        return startFieldIndex;
    }

    public void setStartFieldIndex(int startFieldIndex) {
        this.startFieldIndex = startFieldIndex;
    }
}
