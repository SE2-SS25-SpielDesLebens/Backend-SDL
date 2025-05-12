package at.aau.serg.websocketserver.websocket.broker;

import at.aau.serg.websocketserver.board.BoardService;
import at.aau.serg.websocketserver.board.Field;
import at.aau.serg.websocketserver.messaging.dtos.*;
import at.aau.serg.websocketserver.session.Job;
import at.aau.serg.websocketserver.session.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class WebSocketBrokerController {

    private final JobService jobService;
    private final SimpMessagingTemplate messagingTemplate;
    private final BoardService boardService;

    @Autowired
    public WebSocketBrokerController(JobService jobService,
                                     SimpMessagingTemplate messagingTemplate,
                                     BoardService boardService) {
        this.jobService = jobService;
        this.messagingTemplate = messagingTemplate;
        this.boardService = boardService;
    }

    @MessageMapping("/move")
    public void handleMove(StompMessage message) {
        int playerId;
        try {
            playerId = Integer.parseInt(message.getPlayerName()); // Annahme: playerName = ID
        } catch (NumberFormatException e) {
            messagingTemplate.convertAndSend("/topic/game",
                    new OutputMessage(message.getPlayerName(), "❌ Ungültige Spieler-ID", LocalDateTime.now().toString()));
            return;
        }

        int steps;
        try {
            steps = Integer.parseInt(message.getAction().replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            messagingTemplate.convertAndSend("/topic/game",
                    new OutputMessage(message.getPlayerName(), "❌ Ungültige Würfelzahl", LocalDateTime.now().toString()));
            return;
        }

        boardService.movePlayer(playerId, steps);
        Field currentField = boardService.getPlayerField(playerId);

        MoveMessage moveMessage = new MoveMessage(
                message.getPlayerName(),
                currentField.getIndex(),
                currentField.getX(),
                currentField.getY(),
                currentField.getType(),
                LocalDateTime.now().toString()
        );

        messagingTemplate.convertAndSend("/topic/game", moveMessage);
    }

    @MessageMapping("/lobby")
    public void handleLobby(@Payload StompMessage message) {
        String action = message.getAction();
        String gameId = message.getGameId();
        String content;

        if (action == null) {
            content = "❌ Keine Aktion angegeben.";
        } else {
            content = switch (action) {
                case "createLobby" -> "🆕 Lobby [" + gameId + "] von " + message.getPlayerName() + " erstellt.";
                case "joinLobby" -> "✅ " + message.getPlayerName() + " ist Lobby [" + gameId + "] beigetreten.";
                default -> "Unbekannte Lobby-Aktion.";
            };
        }

        messagingTemplate.convertAndSend("/topic/lobby",
                new OutputMessage(message.getPlayerName(), content, LocalDateTime.now().toString()));
    }

    @MessageMapping("/chat")
    public void handleChat(@Payload StompMessage message) {
        messagingTemplate.convertAndSend("/topic/chat",
                new OutputMessage(message.getPlayerName(), message.getMessageText(), LocalDateTime.now().toString()));
    }

    @MessageMapping("/game/start/{gameId}")
    public void handleGameStart(@DestinationVariable int gameId) {
        jobService.getOrCreateRepository(gameId);
    }

    @MessageMapping("/jobs/{gameId}/{playerName}/request")
    public void handleJobRequest(@DestinationVariable int gameId,
                                 @DestinationVariable String playerName,
                                 @Payload JobRequestMessage msg) {

        boolean hasDegree = msg.hasDegree();
        var repo = jobService.getOrCreateRepository(gameId);
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
                        j.isTaken(),
                        gameId
                ))
                .toList();

        String dest = String.format("/topic/%d/jobs/%s", gameId, playerName);
        messagingTemplate.convertAndSend(dest, dtos);
    }

    @MessageMapping("/jobs/{gameId}/{playerName}/select")
    public void handleJobSelection(@DestinationVariable int gameId,
                                   @DestinationVariable String playerName,
                                   @Payload JobMessage msg) {

        var repo = jobService.getOrCreateRepository(gameId);
        Optional<Job> currentOpt = repo.getCurrentJobForPlayer(playerName);

        if (currentOpt.isPresent() && currentOpt.get().getJobId() == msg.getJobId()) {
            return;
        }

        repo.findJobById(msg.getJobId())
                .ifPresent(job -> repo.assignJobToPlayer(playerName, job));
    }
}
