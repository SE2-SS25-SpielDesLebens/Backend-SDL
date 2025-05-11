package at.aau.serg.websocketserver.lobby;

import lombok.Getter;
import lombok.Setter;

public class Player {
    private String id;
    @Setter
    @Getter
    private boolean isHost;
}
