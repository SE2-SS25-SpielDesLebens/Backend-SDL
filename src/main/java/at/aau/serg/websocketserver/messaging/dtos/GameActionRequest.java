package at.aau.serg.websocketserver.messaging.dtos;

public class GameActionRequest {
    private String gameId;
    private String playerName;
    private int steps;
    private int fieldIndex;
    
    // Getter und Setter
    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }
    
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    
    public int getSteps() { return steps; }
    public void setSteps(int steps) { this.steps = steps; }
    
    public int getFieldIndex() { return fieldIndex; }
    public void setFieldIndex(int fieldIndex) { this.fieldIndex = fieldIndex; }
}