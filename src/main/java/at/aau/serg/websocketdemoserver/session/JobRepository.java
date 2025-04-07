package at.aau.serg.websocketdemoserver.session;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;


public class JobRepository {

    private final List<Job> jobs = new ArrayList<>();
    private final Random random = new Random();

    /**
     * Lädt die Jobs aus der JSON-Datei "jobs.json" und speichert sie in der internen Liste.
     * Erwartet wird ein JSON-Objekt, das ein Array unter dem Schlüssel "jobs" enthält.
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

    /**
     * Gibt ein Array aller geladenen Jobs zurück.
     */
    public Job[] getJobArray() {
        return jobs.toArray(new Job[0]);
    }

    /**
     * Sucht einen zufälligen, noch nicht vergebenen Job, markiert ihn als vergeben und gibt ihn zurück.
     * Falls kein freier Job vorhanden ist, wird ein leeres Optional zurückgegeben.
     */
    public Optional<Job> getRandomJob() {
        List<Job> availableJobs = jobs.stream()
                .filter(job -> !job.isTaken())
                .collect(Collectors.toList());
        if (availableJobs.isEmpty()) {
            return Optional.empty();
        }
        Job randomJob = availableJobs.get(random.nextInt(availableJobs.size()));
        randomJob.assignJob();
        return Optional.of(randomJob);
    }

    /**
     * Gibt den übergebenen Job wieder frei.
     */
    public void releaseJob(Job job) {
        job.releaseJob();
    }
}
