// Beispielhafte Methode, um einen Job auszuwählen
public void waehleJob(String jobName, boolean spielerHatHochschulreife) {
    Job job = jobRepository.getJobByBezeichnung(jobName);
    if (job == null) {
        System.out.println("Diesen Job gibt es nicht!");
        return;
    }

    // Prüfe, ob Hochschulreife benötigt wird
    if (job.isBenoetigtHochschulreife() && !spielerHatHochschulreife) {
        System.out.println("Du benötigst eine Hochschulreife, um diesen Job zu wählen!");
    } else {
        System.out.println("Job gewählt: " + job);
        // ... weitere Logik (z.B. Zuweisung zum Spieler)
    }
}