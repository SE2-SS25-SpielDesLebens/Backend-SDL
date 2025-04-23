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
     * Lädt die Jobs aus der JSON-Datei "jobs.json" und speichert sie in der internen Liste.
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
                        jobNode.get("bezeichnung").asText(),               // mapped to title
                        jobNode.get("gehalt").asInt(),                     // mapped to salary
                        jobNode.get("bonusgehalt").asInt(),                // mapped to bonusSalary
                        jobNode.get("benoetigtHochschulreife").asBoolean() // mapped to requiresDegree
                );
                jobs.add(job);
            }
        }
    }

    public Job[] getJobArray() {
        return jobs.toArray(new Job[0]);
    }

    public List<Job> getTwoAvailableJobs() {
        List<Job> availableJobs = jobs.stream()
                .filter(job -> !job.isTaken())
                .collect(Collectors.toList());
        Collections.shuffle(availableJobs);
        return availableJobs.stream().limit(2).collect(Collectors.toList());
    }

    public boolean assignJobToPlayer(String playerName, Job newJob) {
        // Alten Job freigeben, falls vorhanden
        jobs.stream()
                .filter(job -> playerName.equals(job.getAssignedToPlayerName()))
                .forEach(Job::releaseJob);

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
}
