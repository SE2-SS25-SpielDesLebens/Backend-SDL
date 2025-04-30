package at.aau.serg.websocketdemoserver.session;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class JobRepository {

    private final List<Job> jobs = new ArrayList<>();
    private final Random random = new Random();

    /**
     * Lädt alle Jobs aus src/main/resources/jobs.json.
     */
    public void loadJobs() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("jobs.json");
        if (inputStream == null) {
            throw new IllegalStateException("jobs.json nicht gefunden!");
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(inputStream);
        JsonNode jobsNode = rootNode.get("jobs");

        if (jobsNode != null && jobsNode.isArray()) {
            jobs.clear();
            for (JsonNode jobNode : jobsNode) {
                Job job = new Job(
                        jobNode.get("jobId").asInt(),
                        jobNode.get("bezeichnung").asText(),
                        jobNode.get("gehalt").asInt(),
                        jobNode.get("bonusgehalt").asInt(),
                        jobNode.get("benoetigtHochschulreife").asBoolean()
                );
                jobs.add(job);
            }
        }
    }

    public Job[] getJobArray() {
        return jobs.toArray(new Job[0]);
    }

    /**
     * Liefert zwei zufällige, unvergebene Jobs.
     */
    public List<Job> getTwoAvailableJobs() {
        List<Job> availableJobs = jobs.stream()
                .filter(job -> !job.isTaken())
                .collect(Collectors.toList());
        Collections.shuffle(availableJobs, random);
        return availableJobs.stream().limit(2).collect(Collectors.toList());
    }

    /**
     * Weist einem Spieler einen neuen Job zu und gibt ggf. den alten frei.
     */
    public boolean assignJobToPlayer(String playerName, Job newJob) {
        // alten Job frei machen
        jobs.stream()
                .filter(job -> playerName.equals(job.getAssignedToPlayerName()))
                .forEach(Job::releaseJob);

        // neuen Job zuweisen
        return newJob.assignJobTo(playerName);
    }

    public void releaseJob(Job job) {
        job.releaseJob();
    }

    public Optional<Job> findJobById(int jobId) {
        return jobs.stream()
                .filter(job -> job.getJobId() == jobId)
                .findFirst();
    }

    public Optional<Job> findJobByTitle(String title) {
        return jobs.stream()
                .filter(job -> job.getTitle().equalsIgnoreCase(title))
                .findFirst();
    }

    /**
     * Für neue Spieler: zwei zufällige, verfügbare Jobs.
     * Für Spieler mit bestehendem Job: zuerst den aktuellen Job, dann einen weiteren zufällig ausgewählten.
     */
    public List<Job> getJobsForPlayer(String playerName) {
        Optional<Job> currentJob = jobs.stream()
                .filter(job -> playerName.equals(job.getAssignedToPlayerName()))
                .findFirst();

        // Liste aller unvergebenen Jobs zufällig mischen
        List<Job> available = jobs.stream()
                .filter(job -> !job.isTaken())
                .collect(Collectors.toList());
        Collections.shuffle(available, random);

        if (currentJob.isPresent()) {
            List<Job> result = new ArrayList<>();
            // aktuellen Job immer zuerst
            result.add(currentJob.get());
            // dann den ersten aus der gemischten Liste (falls vorhanden)
            available.remove(currentJob.get());
            if (!available.isEmpty()) {
                result.add(available.get(0));
            }
            return result;
        }

        // neuer Spieler: zwei aus der gemischten Liste
        return available.stream().limit(2).collect(Collectors.toList());
    }
}
