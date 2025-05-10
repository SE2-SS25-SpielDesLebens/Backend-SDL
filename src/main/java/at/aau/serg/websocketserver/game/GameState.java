package at.aau.serg.websocketserver.game;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameState {
    private String gameId;
    private List<Field> fields;
    private Map<String, Integer> playerPositions = new HashMap<>();
    
    public GameState(String gameId) {
        this.gameId = gameId;
        this.fields = BoardData.getInitialFields(); // Vordefinierte Spielfelder laden
    }
    
    public void addPlayer(String playerId, int startFieldIndex) {
        playerPositions.put(playerId, startFieldIndex);
    }
    
    public MoveResult movePlayer(String playerId, int steps) {
        Integer currentFieldIndex = playerPositions.getOrDefault(playerId, 0);
        
        for (int i = 0; i < steps; i++) {
            Field currentField = fields.get(currentFieldIndex);
            
            // Am Ziel oder Sackgasse
            if (currentField.getNextFields().isEmpty()) {
                return new MoveResult(currentFieldIndex, false, null);
            }
            
            // Bei mehreren Möglichkeiten stoppen (Player muss dann auswählen)
            if (currentField.getNextFields().size() > 1) {
                return new MoveResult(currentFieldIndex, true, currentField.getNextFields());
            }
            
            currentFieldIndex = currentField.getNextFields().get(0);
        }
        
        playerPositions.put(playerId, currentFieldIndex);
        return new MoveResult(currentFieldIndex, false, null);
    }
    
    public MoveResult manualMoveTo(String playerId, int fieldIndex) {
        int currentFieldIndex = playerPositions.getOrDefault(playerId, 0);
        Field currentField = fields.get(currentFieldIndex);
        
        if (currentField.getNextFields().contains(fieldIndex)) {
            playerPositions.put(playerId, fieldIndex);
            return new MoveResult(fieldIndex, false, null);
        }
        
        return new MoveResult(currentFieldIndex, false, null);
    }
    
    public Field getPlayerField(String playerId) {
        int fieldIndex = playerPositions.getOrDefault(playerId, 0);
        return fields.get(fieldIndex);
    }
    
    public String getGameId() {
        return gameId;
    }
    
    public List<Field> getFields() {
        return fields;
    }
    
    public Map<String, Integer> getPlayerPositions() {
        return playerPositions;
    }
}