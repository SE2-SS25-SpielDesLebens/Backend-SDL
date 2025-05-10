package at.aau.serg.websocketserver.game;

import java.util.Map;

/**
 * Antwort auf eine Spielaktion
 */
public class GameActionResponse {
    private Map<String, Object> gameState;
    private MoveResult moveResult;
    private String message;
    private boolean success;

    public Map<String, Object> getGameState() {
        return gameState;
    }

    public void setGameState(Map<String, Object> gameState) {
        this.gameState = gameState;
    }

    public MoveResult getMoveResult() {
        return moveResult;
    }

    public void setMoveResult(MoveResult moveResult) {
        this.moveResult = moveResult;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
