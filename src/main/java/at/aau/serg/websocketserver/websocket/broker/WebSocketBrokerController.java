package at.aau.serg.websocketserver.websocket.broker;

import Game.GameController;
import Game.GameLogic;
import Game.PlayerTurnManager;
import at.aau.serg.websocketserver.Player.Player;
import at.aau.serg.websocketserver.lobby.Lobby;
import at.aau.serg.websocketserver.lobby.LobbyService;
import at.aau.serg.websocketserver.messaging.dtos.JobMessage;
import at.aau.serg.websocketserver.messaging.dtos.JobRequestMessage;
import at.aau.serg.websocketserver.messaging.dtos.OutputMessage;
import at.aau.serg.websocketserver.messaging.dtos.StompMessage;
import at.aau.serg.websocketserver.session.Job;
import at.aau.serg.websocketserver.session.JobService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class WebSocketBrokerController {

    private final JobService jobService;
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketBrokerController(JobService jobService,
                                     SimpMessagingTemplate messagingTemplate) {
        this.jobService = jobService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/game/{gameId}/move")
    @SendTo("/topic/game/{gameId}")
    public OutputMessage handleMove(
            @DestinationVariable String gameId,
            StompMessage message
    ) {
        System.out.println("[MOVE] [" + gameId + "] " + message.getPlayerName() + ": " + message.getAction());

        Lobby lobby = LobbyService.getInstance().getLobby(gameId);
        if (lobby == null || !lobby.isStarted()) {
            return new OutputMessage("System", "Spiel nicht gefunden oder nicht gestartet", now());
        }

        GameLogic game = lobby.getGameLogic();

        if (game == null) {
            return new OutputMessage("System", "Spielinstanz nicht vorhanden", now());
        }

        Player player = game.getCurrentPlayer();
        if (!player.getId().equals(message.getPlayerName())) {
            return new OutputMessage("System", "Nicht dein Zug!", now());
        }

        int spin = parseSpinResult(message.getAction());
        if (spin <= 0 || spin > 10) {
            return new OutputMessage("System", "Ungültiger Drehwert", now());
        }

        game.performTurn(player, spin);
        return new OutputMessage(player.getId(), "dreht " + spin, now());
    }


    private int parseSpinResult(String action) {
        if (action != null && action.startsWith("drehe:")) {
            try {
                return Integer.parseInt(action.split(":")[1]);
            } catch (Exception e) {
                System.out.println("[FEHLER] Ungültiges Drehradformat: " + action);
            }
        }
        return 0;
    }

    private String now() {
        return java.time.LocalDateTime.now().toString();
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
    public void handleGameStart(@DestinationVariable String gameId) {
        // 1. Repository vorbereiten
        jobService.getOrCreateRepository(Integer.parseInt(gameId));

        // 2. Lobby und Spieler holen
        Lobby lobby = LobbyService.getInstance().getLobby(gameId);
        if (lobby == null || lobby.isStarted()) {
            System.out.println("[WARNUNG] Lobby nicht gefunden oder bereits gestartet: " + gameId);
            return;
        }

        // 3. GameLogic erzeugen
        GameLogic gameLogic = new GameLogic();
        gameLogic.setJobService(jobService);
        gameLogic.setGameController(new GameController(gameLogic));
        gameLogic.setTurnManager(new PlayerTurnManager(gameLogic));

        for (Player player : lobby.getPlayers()) {
            gameLogic.registerPlayer(player.getId());
        }

        gameLogic.prepareGameStart();
        lobby.setStarted(true);
        lobby.setGameLogic(gameLogic);


        // 4. Optional: Nachricht an alle senden
        messagingTemplate.convertAndSend(
                "/topic/game/" + gameId + "/status",
                "Das Spiel wurde gestartet. Spieleranzahl: " + lobby.getPlayers().size()
        );

    }

    @MessageMapping("/game/end/{gameId}")
    @SendTo("/topic/game/{gameId}/status")
    public OutputMessage handleGameEnd(@DestinationVariable String gameId) {
        Lobby lobby = LobbyService.getInstance().getLobby(gameId);
        if (lobby == null || !lobby.isStarted()) {
            return new OutputMessage("System", "Spiel nicht gefunden oder nicht gestartet", now());
        }

        GameLogic game = lobby.getGameLogic();
        if (game == null) {
            return new OutputMessage("System", "Keine Spielinstanz vorhanden", now());
        }

        game.endGame(); // ❗ Dies ruft die finale Auswertung auf
        lobby.setStarted(false); // Spiel wird beendet

        return new OutputMessage("System", "Spiel wurde manuell beendet!", now());
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
