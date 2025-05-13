package at.aau.serg.websocketserver.messaging.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LobbyResponseMessage {
    private String lobbyID;
    private String playerName;
    private boolean isSuccessful;
    private String message;
}
