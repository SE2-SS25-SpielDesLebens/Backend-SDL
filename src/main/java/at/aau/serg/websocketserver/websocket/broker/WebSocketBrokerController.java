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
import java.util.List;
import java.util.Map;
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
     * Job-Anfrage oder -Akzeptanz:
     * - jobId == null: liefert verfügbare Jobs
     * - jobId != null: weist Job zu und liefert aktualisierte Liste
     */
    @MessageMapping("/jobs")
    public void handleJobFlow(@Payload JobRequestMessage message) {
        String gameId     = message.getGameId();
        String playerName = message.getPlayerName();
        boolean hasDegree = message.hasDegree();
        Integer chosenJob = message.getJobId();

        System.out.println((chosenJob == null ? "[JOB REQUEST] " : "[JOB ACCEPT] ")
                + "[Spiel " + gameId + "] Spieler: " + playerName
                + ", Hochschulreife: " + hasDegree
                + (chosenJob == null ? "" : ", JobId: " + chosenJob)); // TODO: Testausgabe, später auskommentieren

        JobRepository repo = jobRepositories.computeIfAbsent(gameId, id -> {
            JobRepository r = new JobRepository();
            try { r.loadJobs(); } catch (Exception e) { e.printStackTrace(); }
            return r;
        });

        // Bei Wahl eines Jobs: zuweisen
        if (chosenJob != null) {
            repo.findJobById(chosenJob).ifPresent(job -> {
                if (!job.isRequiresDegree() || hasDegree) {
                    repo.assignJobToPlayer(playerName, job);
                }
            });
        }

        // Liste verfügbarer bzw. bestehender Jobs senden
        List<Job> jobs = repo.getJobsForPlayer(playerName);
        List<JobMessage> response = jobs.stream()
                .map(j -> new JobMessage(
                        j.getJobId(), j.getTitle(), j.getSalary(), j.getBonusSalary(), j.isRequiresDegree(), j.isTaken()
                ))
                .collect(Collectors.toList());

        messagingTemplate.convertAndSend("/topic/" + gameId + "/jobs", response);
    }
}
