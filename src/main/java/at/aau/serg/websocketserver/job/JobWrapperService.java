package at.aau.serg.websocketserver.job;

import at.aau.serg.websocketserver.messaging.dtos.JobMessage;
import at.aau.serg.websocketserver.messaging.dtos.JobRequestMessage;
import at.aau.serg.websocketserver.session.Job;
import at.aau.serg.websocketserver.session.JobRepository;
import at.aau.serg.websocketserver.session.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service zur Verwaltung von Jobs und Job-Anfragen.
 * Enthält Logik, die zuvor direkt im WebSocketBrokerController war.
 */
@Service
public class JobWrapperService {

    private final JobService jobService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public JobWrapperService(JobService jobService, SimpMessagingTemplate messagingTemplate) {
        this.jobService = jobService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Verarbeitet eine Job-Anfrage von einem Spieler.
     *
     * @param gameId ID des Spiels
     * @param playerName Name des Spielers
     * @param hasDegree Gibt an, ob der Spieler einen Abschluss hat
     */
    public void handleJobRequest(int gameId, String playerName, boolean hasDegree) {
        var repo = jobService.getOrCreateRepository(gameId);
        List<Job> jobsToSend = new ArrayList<>();

        Optional<Job> current = repo.getCurrentJobForPlayer(playerName);
        if (current.isPresent()) {
            jobsToSend.add(current.get());
            List<Job> random = repo.getRandomAvailableJobs(hasDegree, 1);
            if (random.contains(current.get())) {
                random.remove(current.get());
            }
            jobsToSend.addAll(random);
        } else {
            jobsToSend = repo.getRandomAvailableJobs(hasDegree, 2);
        }

        // Debug-Ausgabe: welche beiden Jobs gleich verschickt werden
        System.out.println("[INFO] Jobs an Spieler " + playerName + " für Spiel " + gameId + ": " +
                jobsToSend.stream()
                        .map(j -> j.getJobId() + "=\"" + j.getTitle() + "\"")
                        .collect(Collectors.joining(", "))
        );

        List<JobMessage> dtos = jobsToSend.stream()
                .map(j -> new JobMessage(
                        j.getJobId(),
                        j.getTitle(),
                        j.getSalary(),
                        j.getBonusSalary(),
                        j.isRequiresDegree(),
                        j.isTaken(),
                        gameId
                ))
                .collect(Collectors.toList());

        String dest = String.format("/topic/%d/jobs/%s", gameId, playerName);
        messagingTemplate.convertAndSend(dest, dtos);
    }

    /**
     * Verarbeitet die Job-Auswahl eines Spielers.
     *
     * @param gameId ID des Spiels
     * @param playerName Name des Spielers
     * @param jobId ID des ausgewählten Jobs
     */
    public void handleJobSelection(int gameId, String playerName, int jobId) {
        var repo = jobService.getOrCreateRepository(gameId);
        Optional<Job> currentOpt = repo.getCurrentJobForPlayer(playerName);

        // Wenn der Spieler bereits diesen Job hat, nichts tun
        if (currentOpt.isPresent() && currentOpt.get().getJobId() == jobId) {
            return;
        }

        // Job dem Spieler zuweisen, wenn er existiert
        repo.findJobById(jobId)
                .ifPresent(job -> repo.assignJobToPlayer(playerName, job));
    }
    
    /**
     * Erstellt ein Job-Repository für ein Spiel, falls es noch nicht existiert.
     * 
     * @param gameId ID des Spiels
     * @return Das Job-Repository
     */
    public JobRepository createJobRepository(int gameId) {
        return jobService.getOrCreateRepository(gameId);
    }
}
