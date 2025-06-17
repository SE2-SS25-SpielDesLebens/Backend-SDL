package at.aau.serg.websocketserver.messaging.dtos;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OutputMessage {
    private String playerName;
    private String content;
    private String timestamp;

    public OutputMessage(String playerName, String content, String timestamp) {
        this.playerName = playerName;
        this.content = content;
        this.timestamp = timestamp;
    }

}