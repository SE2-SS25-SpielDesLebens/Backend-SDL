package at.aau.serg.websocketserver.messaging.dtos;


public class OutputMessage {
    private String playerName;
    private String content;
    private String timestamp;

    public OutputMessage(String playerName, String content, String timestamp) {
        this.playerName = playerName;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
