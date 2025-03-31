package at.aau.serg.websocketdemoserver.session;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JobRepository {
    private final List<Job> jobs = new ArrayList<>();

    public void loadJobs() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("jobs.json");
        if (inputStream == null) {
            throw new IllegalStateException("jobs.json nicht gefunden!");
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(inputStream);
        JsonNode jobsNode = rootNode.get("jobs");

        if (jobsNode != null && jobsNode.isArray()) {
            for (JsonNode jobNode : jobsNode) {
                Job job = new Job(
                        jobNode.get("bezeichnung").asText(),
                        jobNode.get("gehalt").asInt(),
                        jobNode.get("bonusgehalt").asInt(),
                        jobNode.get("benoetigtHochschulreife").asBoolean()
                );
                jobs.add(job);
            }
        }
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public Optional<Job> getAvailableJob(String bezeichnung) {
        return jobs.stream()
                .filter(job -> job.getBezeichnung().equalsIgnoreCase(bezeichnung) && !job.isTaken())
                .findFirst();
    }

    public boolean assignJob(String bezeichnung) {
        Optional<Job> job = getAvailableJob(bezeichnung);
        job.ifPresent(Job::assignJob);
        return job.isPresent();
    }

    public void releaseJob(String bezeichnung) {
        jobs.stream()
                .filter(job -> job.getBezeichnung().equalsIgnoreCase(bezeichnung))
                .findFirst()
                .ifPresent(Job::releaseJob);
    }
}
