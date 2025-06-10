package at.aau.serg.websocketserver.messaging.dtos;

import java.util.Map;
import java.util.HashMap;

/**
 * Nachricht mit den Positionen aller Spieler auf dem Spielbrett.
 * Diese wird verwendet, um die Positionen aller Spieler zwischen den Clients zu synchronisieren.
 */
public class PlayerPositionsMessage {
    private final Map<String, Integer> playerPositions;
    private final String timestamp;
    private final String type = "playerPositions";

    /**
     * Erstellt eine neue Nachricht mit Spielerpositionen.
     * 
     * @param playerPositions Map der Spieler-IDs zu Feldindizes
     * @param timestamp Zeitstempel der Nachricht
     */
    public PlayerPositionsMessage(Map<String, Integer> playerPositions, String timestamp) {
        this.playerPositions = playerPositions != null ? new HashMap<>(playerPositions) : new HashMap<>();
        this.timestamp = timestamp;
    }

    /**
     * Gibt die Map der Spieler-IDs zu Feldindizes zurück.
     * 
     * @return Map mit Spieler-IDs als Schlüssel und Feldindizes als Werte
     */
    public Map<String, Integer> getPlayerPositions() {
        return new HashMap<>(playerPositions);
    }

    /**
     * Gibt den Zeitstempel der Nachricht zurück.
     * 
     * @return Zeitstempel als String
     */
    public String getTimestamp() {
        return timestamp;
    }
    
    /**
     * Gibt den Typ der Nachricht zurück.
     * 
     * @return Typ der Nachricht (immer "playerPositions")
     */
    public String getType() {
        return type;
    }
}
