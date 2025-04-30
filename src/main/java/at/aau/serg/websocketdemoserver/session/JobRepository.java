package at.aau.serg.websocketdemoserver.session;

import at.aau.serg.websocketserver.messaging.dtos.JobMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class JobRepository {

    private final List<Job> jobs = new ArrayList<>();
    private final Random random = new Random();

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

    public List<Job> getTwoAvailableJobs() {
        List<Job> availableJobs = jobs.stream()
                .filter(job -> !job.isTaken())
                .collect(Collectors.toList());
        Collections.shuffle(availableJobs);
        return availableJobs.stream().limit(2).collect(Collectors.toList());
    }

    // 🔹 Neue Methode: Wandelt die 2 zufälligen Jobs in JobMessages um
    public List<JobMessage> getTwoAvailableJobMessages(String playerName) {
        return getTwoAvailableJobs().stream()
                .map(job -> new JobMessage(
                        job.getJobId(),
                        job.getTitle(),
                        job.getSalary(),
                        job.getBonusSalary(),
                        job.isRequiresDegree(),
                        job.isTaken(),
                        job.getAssignedToPlayerName(),
                        playerName,
                        LocalDateTime.now().toString()
                ))
                .collect(Collectors.toList());
    }

    public boolean assignJobToPlayer(String playerName, Job newJob) {
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
