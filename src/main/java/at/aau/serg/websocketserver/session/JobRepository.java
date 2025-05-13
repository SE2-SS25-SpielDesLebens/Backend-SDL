package at.aau.serg.websocketserver.session;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Lädt Jobs aus jobs.json und verwaltet Zuordnungen pro Spieler.
 */
@Repository
public class JobRepository {

    private final List<Job> jobs = new ArrayList<>();
    // Einmalig erzeugter SecureRandom-Generator
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Lädt alle Jobs aus jobs.json.
     */
    public void loadJobs() throws Exception {
        System.out.println("[JobRepository] loadJobs() aufgerufen");

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

        System.out.println("[JobRepository] jobs.json geladen: " + jobs.size() + " Jobs");
    }

    /**
     * Gibt das aktuelle Job-Objekt eines Spielers zurück (falls vorhanden).
     */
    public Optional<Job> getCurrentJobForPlayer(String playerName) {
        Optional<Job> existing = jobs.stream()
                .filter(job -> playerName.equals(job.getAssignedToPlayerName()))
                .findFirst();
        existing.ifPresent(j -> System.out.println(
                "[JobRepository] Spieler \"" + playerName +
                        "\" hat bereits Job: \"" + j.getTitle() +
                        "\" (ID " + j.getJobId() + ")"
        ));
        return existing;
    }

    /**
     * Gibt eine bestimmte Anzahl zufälliger verfügbarer Jobs zurück,
     * die dem Bildungsstatus des Spielers entsprechen.
     */
    public List<Job> getRandomAvailableJobs(boolean hasDegree, int count) {
        List<Job> availableJobs = jobs.stream()
                .filter(job -> !job.isTaken())
                .filter(job -> job.isRequiresDegree() == hasDegree)
                .collect(Collectors.toList());

        // Kryptographisch starker, thread‐sicherer Zufalls­generator
        Collections.shuffle(availableJobs, SECURE_RANDOM);

        return availableJobs.stream()
                .limit(count)
                .collect(Collectors.toList());
    }

    /**
     * Findet einen Job anhand der ID.
     */
    public Optional<Job> findJobById(int jobId) {
        return jobs.stream()
                .filter(job -> job.getJobId() == jobId)
                .findFirst();
    }

    /**
     * Weist einem Spieler einen neuen Job zu und gibt ggf. den alten frei.
     */
    public void assignJobToPlayer(String playerName, Job newJob) {
        // Alten Job freigeben (mit Log)
        getCurrentJobForPlayer(playerName).ifPresent(old -> {
            System.out.println(
                    "[JobRepository] Spieler \"" + playerName +
                            "\" gibt alten Job frei: \"" + old.getTitle() +
                            "\" (ID " + old.getJobId() + ")"
            );
            old.releaseJob();
        });

        // Neuen Job zuweisen
        boolean success = newJob.assignJobTo(playerName);
        if (success) {
            System.out.println(
                    "[JobRepository] Spieler \"" + playerName +
                            "\" erhält neuen Job: \"" + newJob.getTitle() +
                            "\" (ID " + newJob.getJobId() + ")"
            );
        } else {
            System.out.println(
                    "[JobRepository] Zuweisung fehlgeschlagen für Job: \"" +
                            newJob.getTitle() + "\" (ID " + newJob.getJobId() + ")"
            );
        }
    }

    /**
     * Gibt einen Job explizit frei.
     */
    public void releaseJob(Job job) {
        System.out.println(
                "[JobRepository] Job freigegeben: \"" +
                        job.getTitle() + "\" (ID " + job.getJobId() + ")"
        );
        job.releaseJob();
    }
}