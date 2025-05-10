package at.aau.serg.websocketserver.messaging.dtos;

import at.aau.serg.websocketserver.game.GameState;
import at.aau.serg.websocketserver.game.MoveResult;

public class GameActionResponse {
    private String action;
    private String playerName;
    private String gameId;
    private GameState gameState;
    private MoveResult moveResult;
    private String timestamp;
    
    public GameActionResponse(String action, String playerName, String gameId, 
                             GameState gameState, String timestamp) {
        this.action = action;
        this.playerName = playerName;
        this.gameId = gameId;
        this.gameState = gameState;
        this.timestamp = timestamp;
    }
    
    public GameActionResponse(String action, String playerName, String gameId, 
                             GameState gameState, MoveResult moveResult, String timestamp) {
        this.action = action;
        this.playerName = playerName;
        this.gameId = gameId;
        this.gameState = gameState;
        this.moveResult = moveResult;
        this.timestamp = timestamp;
    }
    
    // Getter und Setter
    public String getAction() { return action; }
    public String getPlayerName() { return playerName; }
    public String getGameId() { return gameId; }
    public GameState getGameState() { return gameState; }
    public MoveResult getMoveResult() { return moveResult; }
    public String getTimestamp() { return timestamp; }
}