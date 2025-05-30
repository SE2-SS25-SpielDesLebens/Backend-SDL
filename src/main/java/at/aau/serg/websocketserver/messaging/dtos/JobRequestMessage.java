package at.aau.serg.websocketserver.messaging.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobRequestMessage {
    private String playerName;
    private int gameId;
    private int jobId;
}


