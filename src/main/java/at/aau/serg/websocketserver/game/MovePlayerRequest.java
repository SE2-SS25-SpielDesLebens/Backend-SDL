package at.aau.serg.websocketserver.game;

/**
 * Request zur Bewegung eines Spielers
 */
public class MovePlayerRequest {
    private String gameId;
    private String playerName;
    private int steps;

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

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }
}
