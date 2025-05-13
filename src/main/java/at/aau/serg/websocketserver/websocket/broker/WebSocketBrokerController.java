package at.aau.serg.websocketserver.websocket.broker;

import at.aau.serg.websocketserver.Player.PlayerService;
import at.aau.serg.websocketserver.lobby.Lobby;
import at.aau.serg.websocketserver.lobby.LobbyService;
import at.aau.serg.websocketserver.messaging.dtos.*;
import at.aau.serg.websocketserver.session.Job;
import at.aau.serg.websocketserver.session.JobService;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class WebSocketBrokerController {

    private final JobService jobService;
    private final PlayerService playerService;
    private final LobbyService lobbyService;
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketBrokerController(JobService jobService,
                                     SimpMessagingTemplate messagingTemplate) {
        this.jobService = jobService;
        this.messagingTemplate = messagingTemplate;
        playerService = PlayerService.getInstance();
        lobbyService = LobbyService.getInstance();
    }

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
            content = switch (action) {
                case "createLobby" -> "🆕 Lobby [" + gameId + "] von " + message.getPlayerName() + " erstellt.";
                case "joinLobby" -> "✅ " + message.getPlayerName() + " ist Lobby [" + gameId + "] beigetreten.";
                default -> "Unbekannte Lobby-Aktion.";
            };
        }

        System.out.println("[LOBBY] [" + gameId + "] " + message.getPlayerName() + ": " + content);

        return new OutputMessage(
                message.getPlayerName(),
                content,
                LocalDateTime.now().toString()
        );

    }

    @MessageMapping("/lobby/create")
    @SendTo("/queue/lobby/created")
    public void handleLobbyCreate(@Payload LobbyRequestMessage request, Principal principal){
        //Spieler sollte schon in PlayerService enthalten sein
        Lobby lobby = lobbyService.createLobby(playerService.getPlayerById(request.getPlayerName()));
        System.out.println("Lobbyid: " + lobby.getId() + " " + request.getPlayerName() + " " + principal.getName());
        LobbyResponseMessage response = new LobbyResponseMessage(lobby.getId(), request.getPlayerName(), true, null);
        messagingTemplate.convertAndSendToUser(
                request.getPlayerName(),   // = Principal.getName(), wenn korrekt verwendet
                "/queue/lobby/created",    // Ziel für den Client
                response
        );
        System.out.println("Nachricht gesendet");
    }

    @MessageMapping("/{lobbyid}/join")
    @SendTo("/topic/{lobbyid}")
    public void handlePlayerJoin(@DestinationVariable String lobbyid, @Payload LobbyRequestMessage request){
        LobbyResponseMessage response = null;
        try {
            Lobby lobby = lobbyService.getLobby(lobbyid);
            lobby.addPlayer(playerService.getPlayerById(request.getPlayerName()));
            response = new LobbyResponseMessage(lobbyid, request.getPlayerName(), true, "Spieler " + request.getPlayerName() + " ist erfolgreich beigetreten");

        } catch (Exception e) {
            response = new LobbyResponseMessage(lobbyid, request.getPlayerName(), false, e.getMessage());
        }finally {
            String destination = String.format("/topic/%s", lobbyid);
            assert response != null;
            messagingTemplate.convertAndSend(destination, response);
        }
    }

    @MessageMapping("/{lobbyid}/leave")
    public void handlePlayerLeave(@DestinationVariable String lobbyid, @Payload LobbyRequestMessage request){
        Lobby lobby = lobbyService.getLobby(lobbyid);
        lobby.removePlayer(playerService.getPlayerById(request.getPlayerName()));
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
     * Wird aufgerufen, wenn ein Spiel startet. Legt über JobService
     * das Repository für diese gameId an.
     */

    @MessageMapping("/game/start/{gameId}")
    public void handleGameStart(@DestinationVariable int gameId) {
        // Erstelle oder lade das Repo für diese gameId
        jobService.getOrCreateRepository(gameId);
        // kein convertAndSend, es wird nichts zurückgeschickt
    }


    /**
     * Spieler fragt Jobs an: Holt sich das korrekte Repository
     * über JobService und sendet zwei Jobs zurück.
     */
    @MessageMapping("/jobs/{gameId}/{playerName}/request")
    public void handleJobRequest(@DestinationVariable int gameId,
                                 @DestinationVariable String playerName,
                                 @Payload JobRequestMessage msg) {
        System.out.println("[JOB_REQUEST] Spiel " + gameId +
                ", Spieler " + playerName +
                ", hasDegree=" + msg.hasDegree());

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

        // NEU: Ausgabe nachdem die beiden Jobs verschickt wurden
        System.out.println("[JOB_RESPONSE] Spiel " + gameId +
                ", Spieler " + playerName +
                ", gesendete Jobs: " +
                dtos.stream()
                        .map(JobMessage::getTitle)
                        .reduce((a, b) -> a + " + " + b)
                        .orElse("<keine>"));
    }

    /**
     * Spieler wählt einen Job aus: Repository-Zugriff über JobService.
     */
    @MessageMapping("/jobs/{gameId}/{playerName}/select")
    public void handleJobSelection(@DestinationVariable int gameId,
                                   @DestinationVariable String playerName,
                                   @Payload JobMessage msg) {
        System.out.println("[JOB_SELECT_REQUEST] Spiel " + gameId +
                ", Spieler " + playerName +
                ", gewählter JobId=" + msg.getJobId());

        var repo = jobService.getOrCreateRepository(gameId);
        Optional<Job> currentOpt = repo.getCurrentJobForPlayer(playerName);

        // Neu: Wenn der aktuelle Job bereits dieser ist, nichts tun
        if (currentOpt.isPresent() && currentOpt.get().getJobId() == msg.getJobId()) {
            System.out.println("[JOB_SELECT_SKIP] Spieler " + playerName +
                    " hat JobId=" + msg.getJobId() + " bereits zugewiesen – überspringe");
            return;
        }

        // Sonst ganz normal zuweisen
        repo.findJobById(msg.getJobId())
                .ifPresent(job -> {
                    repo.assignJobToPlayer(playerName, job);
                    System.out.println("[JOB_SELECT] Spiel " + gameId +
                            ", Spieler " + playerName +
                            " erhält neuen Job: " + job.getTitle() +
                            " (ID " + job.getJobId() + ")");
                });
    }
}
