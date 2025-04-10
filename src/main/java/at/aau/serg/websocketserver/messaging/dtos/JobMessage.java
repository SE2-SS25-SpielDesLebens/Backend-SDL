package at.aau.serg.websocketserver.messaging.dtos;

public class JobMessage {
    private String playerName;
    private String bezeichnung;
    private int gehalt;
    private int bonusgehalt;
    private boolean benoetigtHochschulreife;
    private boolean isTaken;
    private String timestamp;

    public JobMessage(String playerName, String bezeichnung, int gehalt, int bonusgehalt, boolean benoetigtHochschulreife, boolean isTaken, String timestamp) {
        this.playerName = playerName;
        this.bezeichnung = bezeichnung;
        this.gehalt = gehalt;
        this.bonusgehalt = bonusgehalt;
        this.benoetigtHochschulreife = benoetigtHochschulreife;
        this.isTaken = isTaken;
        this.timestamp = timestamp;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getBezeichnung() {
        return bezeichnung;
    }

    public int getGehalt() {
        return gehalt;
    }

    public int getBonusgehalt() {
        return bonusgehalt;
    }

    public boolean isBenoetigtHochschulreife() {
        return benoetigtHochschulreife;
    }

    public boolean isTaken() {
        return isTaken;
    }

    public String getTimestamp() {
        return timestamp;
    }

}
