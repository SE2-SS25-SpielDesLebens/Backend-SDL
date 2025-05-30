package at.aau.serg.websocketserver.session.board;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service zur Verwaltung der Spiellogik und Spielerpositionen auf dem Spielbrett.
 */
@Service
public class BoardService {
    private final List<Field> board;
    private final Map<String, Integer> playerPositions = new ConcurrentHashMap<>();  // playerId → fieldIndex
    private final BoardDataProvider boardDataProvider;

    /**
     * Erzeugt einen neuen BoardService mit einer Spring-Injection des BoardDataProviders.
     *
     * @param boardDataProvider Provider für die Spielbrettdaten
     */
    @Autowired
    public BoardService(BoardDataProvider boardDataProvider) {
        this.boardDataProvider = boardDataProvider;
        this.board = boardDataProvider.getBoard();
    }
    
    /**
     * Konstruktor für Testzwecke, ermöglicht das Injizieren eines Mock-BoardDataProviders.
     * 
     * @param boardDataProvider Ein Mock-Provider für die Tests
     * @param playerPositions Vorgefertigte Spielerpositionen für Tests
     */
    protected BoardService(BoardDataProvider boardDataProvider, Map<String, Integer> playerPositions) {
        this.boardDataProvider = boardDataProvider;
        this.board = boardDataProvider.getBoard();
        if (playerPositions != null) {
            this.playerPositions.putAll(playerPositions);
        }
    }
      // Fügt einen Spieler zum Board hinzu
    public void addPlayer(String playerId, int startFieldIndex) {
        playerPositions.put(playerId, startFieldIndex);
    }
    public void addPlayer(int playerId, int startFieldIndex) {
        // Wandelt die Integer-ID in einen String um
        String playerIdStr = String.valueOf(playerId);
        // Ruft die ursprüngliche Methode mit der String-ID auf
        addPlayer(playerIdStr, startFieldIndex);
    }

    // Bewegt einen Spieler um die angegebene Anzahl an Schritten vorwärts
      public void movePlayer(int playerId, int steps) {
          // Wandelt die Integer-ID in einen String um
          String playerIdStr = String.valueOf(playerId);
          // Ruft die ursprüngliche Methode mit der String-ID auf
          movePlayer(playerIdStr, steps);
      }    public void movePlayer(String playerId, int steps) {
        // Berechne alle möglichen Zielfelder
        List<Integer> moveOptions = getMoveOptions(playerId, steps);
        
        // Aktualisiere die Position des Spielers
        if (!moveOptions.isEmpty()) {
            // Bei nur einer Option wird der Spieler direkt dorthin bewegt
            // Bei mehreren Optionen nehmen wir die erste Option, um Abwärtskompatibilität für Tests zu gewährleisten
            // Die eigentliche Spiellogik verwendet getMoveOptions + movePlayerToField
            int targetFieldIndex = moveOptions.get(0);
            playerPositions.put(playerId, targetFieldIndex);
        }
    }
      // Bewegt einen Spieler um die angegebene Anzahl an Schritten mit Entscheidungslogik für Verzweigungen
    public List<Integer> getMoveOptions(String playerId, int steps) {
        int currentFieldIndex = playerPositions.getOrDefault(playerId, 1);
        
        // Startposition hinzufügen
        List<Integer> positions = new ArrayList<>();
        positions.add(currentFieldIndex);
        
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
        int currentFieldIndex = playerPositions.getOrDefault(playerId, 1);
        Field currentField = getFieldByIndex(currentFieldIndex);
        
        // Prüfe, ob das Zielfeld ein erlaubtes nächstes Feld ist
        if (currentField != null && currentField.getNextFields().contains(targetFieldIndex)) {
            playerPositions.put(playerId, targetFieldIndex);
            return true;
        }
        
        return false;
    }
    
    // Gibt das Feld zurück, auf dem sich der Spieler gerade befindet
    public Field getPlayerField(Object playerId) {
        String playerIdStr;
        if (playerId instanceof Integer) {
            playerIdStr = String.valueOf(playerId);
        } else if (playerId instanceof String) {
            playerIdStr = (String) playerId;
        } else {
            throw new IllegalArgumentException("Player ID muss ein String oder Integer sein");
        }

        int fieldIndex = playerPositions.getOrDefault(playerIdStr, 1);
        return getFieldByIndex(fieldIndex);
    }



    // Gibt ein Feld anhand seines Indexes zurück
    public Field getFieldByIndex(int index) {
        return boardDataProvider.getFieldByIndex(index);
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
            playerPositions.put(playerId, fieldIndex);
        }
    }
    public List<Field> getValidNextFields(int playerId) {
        // Wandelt die Integer-ID in einen String um
        String playerIdStr = String.valueOf(playerId);
        // Ruft die ursprüngliche Methode mit der String-ID auf
        return getValidNextFields(playerIdStr);
    }
    public void setPlayerPosition(int playerId, int fieldIndex) {
        // Wandelt die Integer-ID in einen String um
        String playerIdStr = String.valueOf(playerId);
        // Ruft die ursprüngliche Methode mit der String-ID auf
        setPlayerPosition(playerIdStr, fieldIndex);
    }

    public boolean movePlayerToField(int playerId, int targetFieldIndex) {
        // Wandelt die Integer-ID in einen String um
        String playerIdStr = String.valueOf(playerId);
        // Ruft die ursprüngliche Methode mit der String-ID auf
        return movePlayerToField(playerIdStr, targetFieldIndex);
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
    }    // Gibt die Position eines Spielers zurück
    public int getPlayerPosition(String playerId) {
        return playerPositions.getOrDefault(playerId, 1);
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
    
    // Entfernt einen Spieler vom Board (Integer-ID Version)
    public void removePlayer(int playerId) {
        String playerIdStr = String.valueOf(playerId);
        removePlayer(playerIdStr);
    }
    
    // Gibt alle Spielerpositionen zurück
    public Map<String, Integer> getAllPlayerPositions() {
        return new HashMap<>(playerPositions);
    }
      // Prüft, ob sich irgendein Spieler auf einem Feld befindet
    public boolean isAnyPlayerOnField(int fieldIndex) {
        return playerPositions.containsValue(fieldIndex);
    }
    
    // Reset aller Spielerpositionen
    public void resetAllPlayerPositions() {
        playerPositions.clear();
    }
}
