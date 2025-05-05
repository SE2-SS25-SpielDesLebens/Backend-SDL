package at.aau.serg.websocketserver.messaging.dtos;

import lombok.Getter;
import lombok.Setter;

/**
 * Nachricht für Job-Anfragen und -Akzeptanz: kennzeichnet Lobby, Spieler,
 * (optionalen) gewählten Job und Hochschulreife.
 */
@Getter
@Setter
public class JobRequestMessage {
    private String playerName;
    private String gameId;
    private boolean hasDegree;
    private Integer jobId; // null für Anfrage, gesetzt für Akzeptanz

    public JobRequestMessage() {}

    public JobRequestMessage(String playerName, String gameId, boolean hasDegree, Integer jobId) {
        this.playerName = playerName;
        this.gameId = gameId;
        this.hasDegree = hasDegree;
        this.jobId = jobId;
    }
    public boolean hasDegree() {
        return hasDegree;
    }

}


