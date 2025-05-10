package at.aau.serg.websocketserver.game;

/**
 * Request zur Auswahl eines Feldes
 */
public class ChooseFieldRequest {
    private String gameId;
    private String playerName;
    private int fieldIndex;

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

    public int getFieldIndex() {
        return fieldIndex;
    }

    public void setFieldIndex(int fieldIndex) {
        this.fieldIndex = fieldIndex;
    }
}
