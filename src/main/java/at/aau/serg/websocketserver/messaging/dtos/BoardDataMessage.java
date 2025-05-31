package at.aau.serg.websocketserver.messaging.dtos;

import at.aau.serg.websocketserver.session.board.Field;
import java.util.List;

/**
 * Repräsentiert eine Nachricht mit den Spielbrettdaten für die Übertragung über WebSocket.
 */
public class BoardDataMessage {
    private final List<Field> fields;
    private final String timestamp;

    public BoardDataMessage(List<Field> fields, String timestamp) {
        this.fields = fields;
        this.timestamp = timestamp;
    }

    public List<Field> getFields() {
        return fields;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
