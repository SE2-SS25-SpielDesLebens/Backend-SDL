package at.aau.serg.websocketserver.board;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class BoardService {
    private final List<Field> board;
    private final Map<String, Integer> playerPositions = new ConcurrentHashMap<>();  // playerId → fieldIndex

    public BoardService() {
        this.board = BoardData.getBoard();
    }
    
    // Fügt einen Spieler zum Board hinzu
    public void addPlayer(String playerId, int startFieldIndex) {
        playerPositions.put(playerId, Integer.valueOf(startFieldIndex));
    }
      // Bewegt einen Spieler um die angegebene Anzahl an Schritten vorwärts
    public void movePlayer(String playerId, int steps) {
        int currentFieldIndex = playerPositions.getOrDefault(playerId, Integer.valueOf(1));
        
        for (int i = 0; i < steps; i++) {
            Field currentField = getFieldByIndex(currentFieldIndex);
            if (currentField != null && !currentField.getNextFields().isEmpty()) {
                // Einfache Implementierung: Nimm immer das erste nächste Feld

                currentFieldIndex = currentField.getNextFields().get(0);
            }
        }
        
        playerPositions.put(playerId, Integer.valueOf(currentFieldIndex));
    }
    
    // Bewegt einen Spieler um die angegebene Anzahl an Schritten mit Entscheidungslogik für Verzweigungen
    public List<Integer> getMoveOptions(String playerId, int steps) {
        int currentFieldIndex = playerPositions.getOrDefault(playerId, Integer.valueOf(1));
        List<Integer> endOptions = new ArrayList<>();
        
        // Startposition hinzufügen
        List<Integer> positions = new ArrayList<>();
        positions.add(Integer.valueOf(currentFieldIndex));
        
        // Für jeden Schritt alle möglichen Pfade verfolgen
        for (int i = 0; i < steps; i++) {
            List<Integer> newPositions = new ArrayList<>();
            for (Integer pos : positions) {
                Field field = getFieldByIndex(pos);
                if (field != null) {
                    newPositions.addAll(field.getNextFields());
                }
            }
            positions = newPositions;
        }
        
        // Duplikate entfernen
        return positions.stream().distinct().collect(Collectors.toList());
    }
    
    // Bewegt einen Spieler direkt zu einem bestimmten Feld
    public boolean movePlayerToField(String playerId, int targetFieldIndex) {
        int currentFieldIndex = playerPositions.getOrDefault(playerId, Integer.valueOf(1));
        Field currentField = getFieldByIndex(currentFieldIndex);
        
        // Prüfe, ob das Zielfeld ein erlaubtes nächstes Feld ist
        if (currentField != null && currentField.getNextFields().contains(Integer.valueOf(targetFieldIndex))) {
            playerPositions.put(playerId, Integer.valueOf(targetFieldIndex));
            return true;
        }
        
        return false;
    }
    
    // Gibt das Feld zurück, auf dem sich der Spieler gerade befindet
    public Field getPlayerField(String playerId) {
        int fieldIndex = playerPositions.getOrDefault(playerId, Integer.valueOf(1));
        return getFieldByIndex(fieldIndex);
    }




    // Gibt ein Feld anhand seines Indexes zurück
    public Field getFieldByIndex(int index) {
        return BoardData.getFieldByIndex(index);
    }
    
    // Gibt alle gültigen nächsten Felder für einen Spieler zurück
    public List<Field> getValidNextFields(String playerId) {
        Field currentField = getPlayerField(playerId);
        if (currentField == null) {
            return Collections.emptyList();
        }
        
        List<Field> validFields = new ArrayList<>();
        for (Integer nextIndex : currentField.getNextFields()) {
            Field nextField = getFieldByIndex(nextIndex);
            if (nextField != null) {
                validFields.add(nextField);
            }
        }
        
        return validFields;
    }
    
    // Setzt die Position eines Spielers direkt
    public void setPlayerPosition(String playerId, int fieldIndex) {
        if (fieldIndex >= 1 && fieldIndex <= board.size()) {
            playerPositions.put(playerId, Integer.valueOf(fieldIndex));
        }
    }
    
    // Gibt die Größe des Boards zurück
    public int getBoardSize() {
        return board.size();
    }
    
    // Gibt das gesamte Board zurück
    public List<Field> getBoard() {
        return Collections.unmodifiableList(board);
    }
    
    // Prüft, ob ein Spieler auf einem bestimmten Feld steht
    public boolean isPlayerOnField(String playerId, int fieldIndex) {
        Integer position = playerPositions.get(playerId);
        return position != null && position == fieldIndex;
    }
      // Gibt die Position eines Spielers zurück
    public int getPlayerPosition(String playerId) {
        return playerPositions.getOrDefault(playerId, Integer.valueOf(1));
    }
    
    // Gibt alle Spieler zurück, die sich auf einem bestimmten Feld befinden
    public List<String> getPlayersOnField(int fieldIndex) {
        return playerPositions.entrySet().stream()
                .filter(entry -> entry.getValue() == fieldIndex)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
    
    // Entfernt einen Spieler vom Board
    public void removePlayer(String playerId) {
        playerPositions.remove(playerId);
    }
    
    // Gibt alle Spielerpositionen zurück
    public Map<String, Integer> getAllPlayerPositions() {
        return new HashMap<>(playerPositions);
    }
    
    // Prüft, ob sich irgendein Spieler auf einem Feld befindet
    public boolean isAnyPlayerOnField(int fieldIndex) {
        return playerPositions.containsValue(Optional.of(fieldIndex));
    }
    
    // Reset aller Spielerpositionen
    public void resetAllPlayerPositions() {
        playerPositions.clear();
    }
}
