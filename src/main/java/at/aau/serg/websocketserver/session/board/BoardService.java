package at.aau.serg.websocketserver.session.board;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service für die Verwaltung des Spielbretts und der Spielerbewegungen.
 */
@Service
public class BoardService {
    
    // Mapping von Spieler-ID zu aktuellem Feldindex
    private final Map<Integer, Integer> playerPositions = new HashMap<>();
    
    // Das Spielbrett mit allen Feldern
    private final List<Field> board;
    
    public BoardService() {
        // Initialisiere das Spielbrett mit den definierten Feldern
        board = createBoard();
    }
    
    /**
     * Erstellt das Spielbrett mit den verschiedenen Feldern und deren Positionen.
     */
    private List<Field> createBoard() {
        List<Field> fields = new ArrayList<>();
        
        // Erstelle Liste für die Felder (um später nextFields zu setzen)
        List<List<Integer>> nextFieldsList = new ArrayList<>();
        
        // Definieren der Felder mit ihren Koordinaten (x, y sind Prozentangaben, 0.0-1.0)
        fields.add(new Field(0, 0.115f, 0.65f, "STARTNORMAL"));
        nextFieldsList.add(List.of(1));
        
        fields.add(new Field(1, 0.15f, 0.617f, "ZAHLTAG"));
        nextFieldsList.add(List.of(2));
        
        fields.add(new Field(2, 0.184f, 0.6f, "AKTION"));
        nextFieldsList.add(List.of(3));
        
        fields.add(new Field(3, 0.2f, 0.58f, "ANLAGE"));
        nextFieldsList.add(List.of(4));
        
        fields.add(new Field(4, 0.23f, 0.55f, "AKTION"));
        nextFieldsList.add(List.of(5));
        
        fields.add(new Field(5, 0.27f, 0.52f, "FREUND"));
        nextFieldsList.add(List.of(6));
        
        fields.add(new Field(6, 0.31f, 0.49f, "AKTION"));
        nextFieldsList.add(List.of(7));
        
        fields.add(new Field(7, 0.35f, 0.46f, "BERUF"));
        nextFieldsList.add(List.of(8));
        
        fields.add(new Field(8, 0.39f, 0.43f, "ZAHLTAG"));
        nextFieldsList.add(List.of(9));
        
        fields.add(new Field(9, 0.405f, 0.39f, "AKTION"));
        nextFieldsList.add(List.of(10));
        
        fields.add(new Field(10, 0.42f, 0.35f, "HAUS"));
        nextFieldsList.add(List.of(11));
        
        fields.add(new Field(11, 0.44f, 0.31f, "AKTION"));
        nextFieldsList.add(List.of(12));
        
        fields.add(new Field(12, 0.47f, 0.27f, "ZAHLTAG"));
        nextFieldsList.add(List.of(13));
        
        fields.add(new Field(13, 0.51f, 0.23f, "AKTION"));
        nextFieldsList.add(List.of(14));
        
        fields.add(new Field(14, 0.55f, 0.20f, "FREUND"));
        nextFieldsList.add(List.of(15));
        
        fields.add(new Field(15, 0.59f, 0.17f, "AKTION"));
        nextFieldsList.add(List.of(16));
        
        fields.add(new Field(16, 0.63f, 0.14f, "HEIRAT"));
        nextFieldsList.add(List.of(16)); // Selbstreferenz, Endfeld
        
        // Universitäts-Start und Felder
        fields.add(new Field(17, 0.25f, 0.75f, "STARTUNI"));
        nextFieldsList.add(List.of(18));
        
        fields.add(new Field(18, 0.30f, 0.72f, "ZAHLTAG"));
        nextFieldsList.add(List.of(19));
        
        fields.add(new Field(19, 0.35f, 0.70f, "EXAMEN"));
        nextFieldsList.add(List.of(5)); // Nach dem Examen geht es zum Freund-Feld (5)
        
        // Setze die nextFields für alle Felder
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            List<Integer> nextFields = nextFieldsList.get(i);
            
            for (Integer nextFieldIndex : nextFields) {
                ((Field)field).addNextField(nextFieldIndex);
            }
        }
        
        return fields;
    }
    
    /**
     * Fügt einen neuen Spieler zum Brett hinzu.
     */
    public void addPlayer(int playerId, int startFieldIndex) {
        if (startFieldIndex >= 0 && startFieldIndex < board.size()) {
            playerPositions.put(playerId, startFieldIndex);
        } else {
            // Bei ungültigem Index auf Startfeld setzen
            playerPositions.put(playerId, 0);
        }
    }
    
    /**
     * Bewegt einen Spieler um die angegebene Anzahl von Schritten.
     * Berücksichtigt die erlaubten nextFields für eine korrekte Bewegung.
     */
    public void movePlayer(int playerId, int steps) {
        // Hole aktuelle Position oder verwende 0 als Default
        int currentPosition = playerPositions.getOrDefault(playerId, 0);
        Field currentField = board.get(currentPosition);
        
        // Wenn keine Bewegung gewünscht oder möglich, behalten wir die aktuelle Position bei
        if (steps <= 0 || currentField.getNextFields().isEmpty()) {
            return;
        }
        
        // Bewege den Spieler entsprechend der nextFields-Konfiguration
        int newPosition = currentPosition;
        int remainingSteps = steps;
        
        while (remainingSteps > 0) {
            Field field = board.get(newPosition);
            List<Integer> nextFieldIndices = field.getNextFields();
            
            // Wenn keine Weiterführung möglich ist oder Endfeld erreicht (z.B. Heirat), abbrechen
            if (nextFieldIndices.isEmpty() || (nextFieldIndices.size() == 1 && nextFieldIndices.get(0) == newPosition)) {
                break;
            }
            
            // Nehme die erste mögliche Weiterführung (später könnten hier Entscheidungen eingebaut werden)
            newPosition = nextFieldIndices.get(0);
            remainingSteps--;
        }
        
        // Aktualisiere Spielerposition
        playerPositions.put(playerId, newPosition);
    }
    
    /**
     * Bestimmt die zulässigen nächsten Felder ausgehend vom aktuellen Feld eines Spielers.
     */
    public List<Field> getValidNextFields(int playerId) {
        int currentPosition = playerPositions.getOrDefault(playerId, 0);
        Field currentField = board.get(currentPosition);
        List<Field> validNextFields = new ArrayList<>();
        
        for (Integer nextFieldIndex : currentField.getNextFields()) {
            if (nextFieldIndex >= 0 && nextFieldIndex < board.size()) {
                validNextFields.add(board.get(nextFieldIndex));
            }
        }
        
        return validNextFields;
    }
    
    /**
     * Bewegt einen Spieler direkt zu einem bestimmten Feld, wenn es ein gültiger Zug ist.
     * Gibt true zurück, wenn der Zug erfolgreich war, sonst false.
     */
    public boolean movePlayerToField(int playerId, int targetFieldIndex) {
        int currentPosition = playerPositions.getOrDefault(playerId, 0);
        Field currentField = board.get(currentPosition);
        
        // Überprüfe, ob das Zielfeld in den erlaubten nextFields liegt
        if (currentField.getNextFields().contains(targetFieldIndex)) {
            playerPositions.put(playerId, targetFieldIndex);
            return true;
        }
        
        return false;
    }
    
    /**
     * Ermittelt das aktuelle Feld eines Spielers.
     */
    public Field getPlayerField(int playerId) {
        int fieldIndex = playerPositions.getOrDefault(playerId, 0);
        return board.get(fieldIndex);
    }
    
    /**
     * Liefert das Feld mit dem angegebenen Index zurück.
     */
    public Field getFieldByIndex(int index) {
        if (index >= 0 && index < board.size()) {
            return board.get(index);
        }
        return null;
    }
    
    /**
     * Liefert die Größe des Spielbretts (Anzahl der Felder).
     */
    public int getBoardSize() {
        return board.size();
    }
    
    /**
     * Setzt die Position eines Spielers auf ein bestimmtes Feld.
     */
    public void setPlayerPosition(int playerId, int fieldIndex) {
        if (fieldIndex >= 0 && fieldIndex < board.size()) {
            playerPositions.put(playerId, fieldIndex);
        }
    }
}
