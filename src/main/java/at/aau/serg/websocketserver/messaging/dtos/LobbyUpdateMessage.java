package at.aau.serg.websocketserver.messaging.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LobbyUpdateMessage {
    private String player1;
    private String player2;
    private String player3;
    private String player4;
    private boolean isStarted;
}
