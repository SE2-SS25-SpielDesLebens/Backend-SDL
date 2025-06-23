package at.aau.serg.websocketserver.session.house;

import lombok.Data;

@Data
public class House {
    private int houseId;
    private String bezeichnung;
    private int kaufpreis;
    private int verkaufspreisRot;
    private int verkaufspreisSchwarz;
    private boolean isTaken;
    private String assignedToPlayerName;

    public House() {
    }

    public House(int houseId, String bezeichnung, int kaufpreis, int verkaufspreisRot, int verkaufspreisSchwarz) {
        this.houseId = houseId;
        this.bezeichnung = bezeichnung;
        this.kaufpreis = kaufpreis;
        this.verkaufspreisRot = verkaufspreisRot;
        this.verkaufspreisSchwarz = verkaufspreisSchwarz;
        this.isTaken = false;
        this.assignedToPlayerName = null;
    }

    public void assignHouseTo(String playerName) {
        if (!isTaken) {
            isTaken = true;
            assignedToPlayerName = playerName;
        }
    }

    public void releaseHouse() {
        isTaken = false;
        assignedToPlayerName = null;
    }

    @Override
    public String toString() {
        return "House{" +
                "Bezeichnung='" + bezeichnung + '\'' +
                ", Kaufpreis=" + kaufpreis +
                ", VerkaufspreisRot=" + verkaufspreisRot +
                ", VerkaufspreisSchwarz=" + verkaufspreisSchwarz +
                ", Vergeben=" + isTaken +
                ", Zugewiesen an='" + assignedToPlayerName + '\'' +
                '}';
    }
}
