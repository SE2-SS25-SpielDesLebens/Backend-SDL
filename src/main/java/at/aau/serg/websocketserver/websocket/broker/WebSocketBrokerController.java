package at.aau.serg.websocketserver.websocket.broker;

import at.aau.serg.websocketserver.messaging.dtos.JobMessage;
import at.aau.serg.websocketserver.messaging.dtos.JobRequestMessage;
import at.aau.serg.websocketdemoserver.session.Job;
import at.aau.serg.websocketdemoserver.session.JobRepository;
import at.aau.serg.websocketserver.messaging.dtos.OutputMessage;
import at.aau.serg.websocketserver.messaging.dtos.StompMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Controller
public class WebSocketBrokerController {

    private final Map<String, JobRepository> jobRepositories = new ConcurrentHashMap<>();

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/move")
    @SendTo("/topic/game") // optional: dynamisch mit gameId (siehe Kommentar unten)
    public OutputMessage handleMove(StompMessage message) {
        System.out.println("[MOVE] [" + message.getGameId() + "] " + message.getPlayerName() + ": " + message.getAction());
        return new OutputMessage(
                message.getPlayerName(),
                message.getAction(),
                LocalDateTime.now().toString()
        );
    }

    @MessageMapping("/lobby")
    @SendTo("/topic/lobby")
    public OutputMessage handleLobby(StompMessage message) {
        String action = message.getAction();
        String content;
        String gameId = message.getGameId();

        if (action == null) {
            content = "❌ Keine Aktion angegeben.";
        } else {
            switch (action) {
                case "createLobby":
                    content = "🆕 Lobby [" + gameId + "] von " + message.getPlayerName() + " erstellt.";
                    break;
                case "joinLobby":
                    content = "✅ " + message.getPlayerName() + " ist Lobby [" + gameId + "] beigetreten.";
                    break;
                default:
                    content = "Unbekannte Lobby-Aktion.";
                    break;
            }
        }

        System.out.println("[LOBBY] [" + gameId + "] " + message.getPlayerName() + ": " + content);

        return new OutputMessage(
                message.getPlayerName(),
                content,
                LocalDateTime.now().toString()
        );
    }

    @MessageMapping("/chat")
    @SendTo("/topic/chat")
    public OutputMessage handleChat(StompMessage message) {
        System.out.println("[CHAT] [" + message.getGameId() + "] " + message.getPlayerName() + ": " + message.getMessageText());
        return new OutputMessage(
                message.getPlayerName(),
                message.getMessageText(),
                LocalDateTime.now().toString()
        );
    }

    /**
     * Wird einmalig beim Spielstart aufgerufen.
     */
    public void createJobRepositoryForGame(String gameId) {
        JobRepository repo = new JobRepository();
        try {
            repo.loadJobs();
        } catch (Exception e) {
            e.printStackTrace();
        }
        jobRepositories.put(gameId, repo);
    }

    /**
     * Spieler fragt Jobs an (aktuell oder neue Auswahl).
     */
    @MessageMapping("/jobs/request")
    public void handleJobRequest(@Payload JobRequestMessage msg) {
        String gameId = msg.getGameId();
        String playerName = msg.getPlayerName();
        boolean hasDegree = msg.hasDegree();

        JobRepository repo = jobRepositories.get(gameId);
        if (repo == null) throw new IllegalStateException("Kein JobRepository für Game ID " + gameId);

        List<Job> jobsToSend = new ArrayList<>();
        Optional<Job> current = repo.getCurrentJobForPlayer(playerName);

        if (current.isPresent()) {
            jobsToSend.add(current.get());
            List<Job> random = repo.getRandomAvailableJobs(hasDegree, 1);
            random.remove(current.get());
            jobsToSend.addAll(random);
        } else {
            jobsToSend = repo.getRandomAvailableJobs(hasDegree, 2);
        }

        List<JobMessage> dtos = jobsToSend.stream()
                .map(j -> new JobMessage(
                        j.getJobId(),
                        j.getTitle(),
                        j.getSalary(),
                        j.getBonusSalary(),
                        j.isRequiresDegree(),
                        j.isTaken()))
                .collect(Collectors.toList());

        String dest = "/topic/" + gameId + "/jobs/" + playerName;
        messagingTemplate.convertAndSend(dest, dtos);
    }

    /**
     * Spieler akzeptiert einen konkreten Job.
     */
    @MessageMapping("/jobs/select")
    public void handleJobSelection(@Payload JobRequestMessage msg) {
        String gameId = msg.getGameId();
        String playerName = msg.getPlayerName();
        boolean hasDegree = msg.hasDegree();
        Integer chosenJobId = msg.getJobId();

        if (chosenJobId == null) {
            throw new IllegalArgumentException("JobId darf bei der Auswahl nicht null sein.");
        }

        JobRepository repo = jobRepositories.get(gameId);
        if (repo == null) {
            throw new IllegalStateException("Kein JobRepository für Game ID " + gameId + " gefunden.");
        }

        Optional<Job> selectedJob = repo.findJobById(chosenJobId);
        if (selectedJob.isEmpty()) return;

        Job job = selectedJob.get();
        if (job.isRequiresDegree() == hasDegree) {
            repo.assignJobToPlayer(playerName, job);
        }

        List<JobMessage> dtos = List.of(new JobMessage(
                job.getJobId(),
                job.getTitle(),
                job.getSalary(),
                job.getBonusSalary(),
                job.isRequiresDegree(),
                job.isTaken()
        ));

        String dest = "/topic/" + gameId + "/jobs/" + playerName;
        messagingTemplate.convertAndSend(dest, dtos);
    }

    @MessageMapping("/game/start")
    @SendTo("/topic/game")
    public OutputMessage handleGameStart(StompMessage message) {
        String gameId = message.getGameId();
        createJobRepositoryForGame(gameId);
        return new OutputMessage(
                "System",
                "Spiel [" + gameId + "] gestartet.",
                LocalDateTime.now().toString()
        );
    }
}
