package at.aau.serg.websocketserver.messaging.dtos;

import java.util.List;
import java.util.ArrayList;

/**
 * Repräsentiert eine Bewegungsnachricht für die Übertragung über den WebSocket.
 * Sendet nur die Field-ID, da die Koordinaten im Frontend bekannt sind.
 * Beinhaltet auch Information über erlaubte nächste Felder.
 */
public class MoveMessage {
    private final String playerName;
    private final int index;
    private final String type;
    private final String timestamp;
    private final List<Integer> nextPossibleFields;

    public MoveMessage(String playerName, int index, String type, String timestamp) {
        this.playerName = playerName;
        this.index = index;
        this.type = type;
        this.timestamp = timestamp;
        this.nextPossibleFields = new ArrayList<>();
    }
    
    public MoveMessage(String playerName, int index, String type, String timestamp, List<Integer> nextPossibleFields) {
        this.playerName = playerName;
        this.index = index;
        this.type = type;
        this.timestamp = timestamp;
        this.nextPossibleFields = new ArrayList<>(nextPossibleFields);
    }

    public String getPlayerName() {
        return playerName;
    }    
    
    public int getIndex() {
        return index;
    }

    public String getType() {
        return type;
    }

    public String getTimestamp() {
        return timestamp;
    }
    
    public List<Integer> getNextPossibleFields() {
        return new ArrayList<>(nextPossibleFields);
    }
}

