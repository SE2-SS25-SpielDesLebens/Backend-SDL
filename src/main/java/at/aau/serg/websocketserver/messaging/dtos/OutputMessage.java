package at.aau.serg.websocketserver.messaging.dtos;

import java.util.List;

public class OutputMessage {
    private String playerName;
    private String content;
    private String timestamp;
    private Integer position;
    private List<Integer> options;

    public OutputMessage(String playerName, String content, String timestamp) {
        this.playerName = playerName;
        this.content = content;
        this.timestamp = timestamp;
    }

    public OutputMessage(String playerName, String content, String timestamp, Integer position, List<Integer> options) {
        this.playerName = playerName;
        this.content = content;
        this.timestamp = timestamp;
        this.position = position;
        this.options = options;
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
    
    public Integer getPosition() {
        return position;
    }
    
    public void setPosition(Integer position) {
        this.position = position;
    }
    
    public List<Integer> getOptions() {
        return options;
    }
    
    public void setOptions(List<Integer> options) {
        this.options = options;
    }
}