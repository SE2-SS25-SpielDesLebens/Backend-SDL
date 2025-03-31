package at.aau.serg.websocketdemoserver.session;

import lombok.Data;

@Data
public class Job {
    private String bezeichnung;
    private int gehalt;
    private int bonusgehalt;
    private boolean benoetigtHochschulreife;
    private boolean isTaken; // Neu: Gibt an, ob der Job vergeben ist

    /**
     * Standardkonstruktor (z. B. für JSON-Deserialisierung)
     */
    public Job() {
    }

    /**
     * Konstruktor zum Erstellen eines Job-Objekts mit Bezeichnung, Grundgehalt,
     * Bonusgehalt und Information, ob eine Hochschulreife benötigt wird.
     *
     * @param bezeichnung              Name bzw. Titel des Jobs (z. B. "Ingenieur")
     * @param gehalt                   Festes Grundgehalt
     * @param bonusgehalt              Bonusgehalt
     * @param benoetigtHochschulreife  true, wenn Hochschulreife für den Job erforderlich ist
     */
    public Job(String bezeichnung, int gehalt, int bonusgehalt, boolean benoetigtHochschulreife) {
        this.bezeichnung = bezeichnung;
        this.gehalt = gehalt;
        this.bonusgehalt = bonusgehalt;
        this.benoetigtHochschulreife = benoetigtHochschulreife;
        this.isTaken = false; // Standardmäßig ist der Job verfügbar
    }

    public boolean assignJob() {
        if (!isTaken) {
            isTaken = true;
            return true;
        }
        return false;
    }


    public void releaseJob() {
        isTaken = false;
    }

    @Override
    public String toString() {
        return "Job{" +
                "bezeichnung='" + bezeichnung + '\'' +
                ", gehalt=" + gehalt +
                ", bonusgehalt=" + bonusgehalt +
                ", benoetigtHochschulreife=" + benoetigtHochschulreife +
                '}';
    }
}
