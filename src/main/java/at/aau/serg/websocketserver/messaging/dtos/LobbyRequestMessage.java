package at.aau.serg.websocketserver.messaging.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LobbyRequestMessage {
    private String playerName;
}
