package at.aau.serg.websocketserver.messaging.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LobbyRequestMessage {
    private String playerName;
}
