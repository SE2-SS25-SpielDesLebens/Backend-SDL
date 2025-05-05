package at.aau.serg.websocketserver.messaging.dtos;

/**
 * Nachricht für Job-Anfragen und -Akzeptanz: kennzeichnet Lobby, Spieler,
 * (optionalen) gewählten Job und Hochschulreife.
 */
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

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public boolean hasDegree() {
        return hasDegree;
    }

    public void setHasDegree(boolean hasDegree) {
        this.hasDegree = hasDegree;
    }

    public Integer getJobId() {
        return jobId;
    }

    public void setJobId(Integer jobId) {
        this.jobId = jobId;
    }
}


