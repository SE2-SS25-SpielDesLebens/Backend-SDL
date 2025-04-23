package at.aau.serg.websocketdemoserver.session;

import lombok.Data;

@Data
public class Job {
    private int jobId;
    private String title;
    private int salary;
    private int bonusSalary;
    private boolean requiresDegree;
    private boolean isTaken;
    private String assignedToPlayerName;

    public Job() {
    }

    public Job(int jobId, String title, int salary, int bonusSalary, boolean requiresDegree) {
        this.jobId = jobId;
        this.title = title;
        this.salary = salary;
        this.bonusSalary = bonusSalary;
        this.requiresDegree = requiresDegree;
        this.isTaken = false;
        this.assignedToPlayerName = null;
    }

    public boolean assignJobTo(String playerName) {
        if (!isTaken) {
            isTaken = true;
            assignedToPlayerName = playerName;
            return true;
        }
        return false;
    }

    public void releaseJob() {
        isTaken = false;
        assignedToPlayerName = null;
    }

    @Override
    public String toString() {
        return "Job{" +
                "Bezeichnung='" + title + '\'' +
                ", Gehalt=" + salary +
                ", Bonusgehalt=" + bonusSalary +
                ", Hochschulreife benötigt=" + requiresDegree +
                ", Vergeben=" + isTaken +
                ", Zugewiesen an='" + assignedToPlayerName + '\'' +
                '}';
    }
}
