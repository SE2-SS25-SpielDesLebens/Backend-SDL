// Backend: WebSocketBrokerController.java
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
     * Behandelt Job-Anfragen und -Akzeptanzen.
     * Sendet an /topic/{gameId}/jobs/{playerName}.
     */
    @MessageMapping("/jobs")
    public void handleJobFlow(@Payload JobRequestMessage msg) {
        String gameId       = msg.getGameId();
        String playerName   = msg.getPlayerName();
        boolean hasDegree   = msg.hasDegree();
        Integer chosenJobId = msg.getJobId();

        JobRepository repo = jobRepositories.computeIfAbsent(gameId, id -> {
            JobRepository r = new JobRepository();
            try { r.loadJobs(); } catch (Exception e) { e.printStackTrace(); }
            return r;
        });

        List<Job> responseJobs;
        if (chosenJobId == null) {
            // Erst-Anfrage: bestehenden Job (falls vergeben) + Zufalls-Option(en)
            Optional<Job> current = Arrays.stream(repo.getJobArray())
                    .filter(j -> playerName.equals(j.getAssignedToPlayerName()))
                    .findFirst();
            List<Job> candidates = Arrays.stream(repo.getJobArray())
                    .filter(j -> !j.isTaken())
                    .filter(j -> hasDegree || !j.isRequiresDegree())
                    .collect(Collectors.toList());
            Collections.shuffle(candidates);
            responseJobs = new ArrayList<>();
            current.ifPresent(responseJobs::add);
            if (responseJobs.isEmpty()) {
                // neuer Spieler: 2 Optionen
                responseJobs.addAll(candidates.stream().limit(2).toList());
            } else if (!candidates.isEmpty()) {
                // bereits zugewiesen: 1 Zusatz-Option
                responseJobs.add(candidates.get(0));
            }
        } else {
            // Akzeptanz: Job zuweisen und nur diesen zurückgeben
            responseJobs = new ArrayList<>();
            repo.findJobById(chosenJobId).ifPresent(job -> {
                if (!job.isRequiresDegree() || hasDegree) {
                    repo.assignJobToPlayer(playerName, job);
                }
                responseJobs.add(job);
            });
        }

        // DTO-Mapping
        List<JobMessage> dtos = responseJobs.stream()
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
}
