package at.aau.serg.websocketserver.messaging.dtos;

public class MoveMessage {
    private String playerName;
    private int fieldIndex;
    private float x;
    private float y;
    private String type;
    private String timestamp;

    public MoveMessage(String playerName, int fieldIndex, float x, float y, String type, String timestamp) {
        this.playerName = playerName;
        this.fieldIndex = fieldIndex;
        this.x = x;
        this.y = y;
        this.type = type;
        this.timestamp = timestamp;
    }

    // Getter
    public String getPlayerName() { return playerName; }
    public int getFieldIndex() { return fieldIndex; }
    public float getX() { return x; }
    public float getY() { return y; }
    public String getType() { return type; }
    public String getTimestamp() { return timestamp; }
}
