package at.aau.serg.websocketserver.messaging.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LobbyUpdateMessage {
    private String player1;
    private String player2;
    private String player3;
    private String player4;
    private boolean isStarted;
}
