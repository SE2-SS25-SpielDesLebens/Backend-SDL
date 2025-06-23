package at.aau.serg.websocketserver.websocket.broker;

import at.aau.serg.websocketserver.game.GameController;
import at.aau.serg.websocketserver.game.GameLogic;
import at.aau.serg.websocketserver.game.PlayerTurnManager;
import at.aau.serg.websocketserver.player.Player;
import at.aau.serg.websocketserver.player.PlayerService;
import at.aau.serg.websocketserver.session.board.BoardService;
import at.aau.serg.websocketserver.session.board.Field;
import at.aau.serg.websocketserver.lobby.Lobby;
import at.aau.serg.websocketserver.lobby.LobbyService;
import at.aau.serg.websocketserver.messaging.dtos.*;
import at.aau.serg.websocketserver.session.house.HouseService;
import at.aau.serg.websocketserver.session.job.Job;
import at.aau.serg.websocketserver.session.job.JobService;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final PlayerService playerService;
    private final LobbyService lobbyService;
    private final SimpMessagingTemplate messagingTemplate;
    private final HouseService houseService;

    @Autowired
    private final BoardService boardService;    @Autowired
    public WebSocketBrokerController(JobService jobService,
                                     SimpMessagingTemplate messagingTemplate,
                                     BoardService boardService,
                                     HouseService houseService) {
        this.jobService = jobService;
        this.messagingTemplate = messagingTemplate;
        this.boardService = boardService;
        this.houseService = houseService;
        this.playerService = PlayerService.getInstance();
        this.lobbyService  = LobbyService.getInstance();
    }

    /**
     * Liefert die aktuellen Spielbrettdaten an den Client.
     * Diese Methode kann vom Frontend aufgerufen werden, um die vollständigen Board-Daten zu erhalten.
     */
    @MessageMapping("/board/data")
    public void handleBoardDataRequest() {
        // Erstelle eine neue BoardDataMessage mit den aktuellen Feldern
        BoardDataMessage boardDataMessage = new BoardDataMessage(
                boardService.getBoard(),
                now()
        );

        // Sende die Nachricht an alle verbundenen Clients
        messagingTemplate.convertAndSend("/topic/board/data", boardDataMessage);
    }

    /**
     * Nur das Job-Repository für das gegebene Spiel anlegen, ohne das Spiel zu starten.
     */
    @MessageMapping("/game/createJobRepo/{gameId}")
    public void handleJobRepoCreation(@DestinationVariable int gameId) {
        // Erzeugt (oder gibt zurück) das Job-Repository für dieses Spiel
        jobService.getOrCreateRepository(gameId);

        // Optional: sende eine Statusmeldung an alle Clients
        String destination = "/topic/game/" + gameId + "/status";
        messagingTemplate.convertAndSend(
                destination,
                new OutputMessage(
                        "System",
                        "Job-Repository für Spiel " + gameId + " wurde angelegt.",
                        now()
                )
        );    }



    @MessageMapping("/lobby")
    public void handleLobby(@Payload StompMessage message) {
        String action = message.getAction();
        String gameId = message.getGameId();
        String content;

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

        messagingTemplate.convertAndSend(
                "/topic/lobby",
                new OutputMessage(message.getPlayerName(), content, LocalDateTime.now().toString())
        );
    }

    @MessageMapping("/lobby/create")
    @SendTo("/queue/lobby/created")
    public void handleLobbyCreate(@Payload LobbyRequestMessage request) {
        String playerId = request.getPlayerName();

        // Spieler wird erstellt oder zurückgegeben (zentral!)
        Player player = playerService.createPlayerIfNotExists(playerId);

        // Lobby erstellen
        Lobby lobby = lobbyService.createLobby(player);
        System.out.println("Lobbyid: " + lobby.getId() + " " + playerId);

        LobbyResponseMessage response = new LobbyResponseMessage(lobby.getId(), playerId, true, null);
        messagingTemplate.convertAndSendToUser(
                playerId,
                "/queue/lobby/created",
                response
        );

        sendLobbyUpdates(lobby.getId());
    }

    @MessageMapping("/{lobbyid}/join")
    @SendTo("/topic/{lobbyid}")
    public void handlePlayerJoin(@DestinationVariable String lobbyid, @Payload LobbyRequestMessage request) {
        LobbyResponseMessage response;
        String playerId = request.getPlayerName();

        try {
            Player player = playerService.createPlayerIfNotExists(playerId);

            Lobby lobby = lobbyService.getLobby(lobbyid);
            if (lobby == null) {
                throw new IllegalStateException("Lobby mit dieser ID existiert nicht.");
            }

            lobby.addPlayer(player);
            response = new LobbyResponseMessage(lobbyid, playerId, true, "✅ Spieler beigetreten");

        } catch (Exception e) {
            response = new LobbyResponseMessage(lobbyid, playerId, false, e.getMessage());
        }

        messagingTemplate.convertAndSend("/topic/" + lobbyid, response);
        sendLobbyUpdates(lobbyid);
    }


    @MessageMapping("/players/check")
    public void handlePlayerExistenceCheck(@Payload StompMessage message) {
        String playerId = message.getPlayerName();
        boolean exists = playerService.isPlayerRegistered(playerId);

        messagingTemplate.convertAndSendToUser(
                playerId,
                "/queue/players/check",
                new OutputMessage(
                        "System",
                        exists
                                ? "✅ Spieler '" + playerId + "' ist registriert."
                                : "❌ Spieler '" + playerId + "' ist noch nicht registriert.",
                        LocalDateTime.now().toString()
                )
        );
    }



    @SendTo("/topic/{lobbyid}")
    public void sendLobbyUpdates(String lobbyid) {
        Lobby lobby = lobbyService.getLobby(lobbyid);
        String player1 = getPlayerIdSafe(lobby, 0);
        String player2 = getPlayerIdSafe(lobby, 1);
        String player3 = getPlayerIdSafe(lobby, 2);
        String player4 = getPlayerIdSafe(lobby, 3);

        LobbyUpdateMessage message = new LobbyUpdateMessage(player1, player2, player3, player4, lobby.isStarted());
        System.out.println(message);
        String destination = String.format("/topic/%s", lobbyid);
        messagingTemplate.convertAndSend(destination, message);
    }

    @MessageMapping("/{lobbyid}/leave")
    public void handlePlayerLeave(@DestinationVariable String lobbyid, @Payload LobbyRequestMessage request) {
        Lobby lobby = lobbyService.getLobby(lobbyid);
        System.out.printf("Spieler %s hat die Lobby verlassen\n", request.getPlayerName());
        lobby.removePlayer(playerService.getPlayerById(request.getPlayerName()));
        sendLobbyUpdates(lobbyid);
    }

    @MessageMapping("/chat")
    public void handleChat(@Payload StompMessage message) {
        messagingTemplate.convertAndSend("/topic/chat",
                new OutputMessage(message.getPlayerName(), message.getMessageText(), LocalDateTime.now().toString()));
    }

    @MessageMapping("/game/start/{gameId}")
    public void handleGameStart(@DestinationVariable int gameId) {
        // 1. Repository vorbereiten
        jobService.getOrCreateRepository(gameId);

        // 2. Lobby und Spieler holen
        Lobby lobby = LobbyService.getInstance().getLobby(Integer.toString(gameId));
        if (lobby == null || lobby.isStarted()) {
            System.out.println("[WARNUNG] Lobby nicht gefunden oder bereits gestartet: " + gameId);
            return;
        }

        // 3. GameLogic erzeugen
        GameLogic gameLogic = new GameLogic();
        gameLogic.setGameId(gameId);
        gameLogic.setJobService(jobService);
        gameLogic.setBoardService(boardService);
        gameLogic.setGameController(new GameController(gameLogic, messagingTemplate));
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

    private String now() {
        return LocalDateTime.now().toString();
    }



    @MessageMapping("/jobs/{gameId}/{playerName}/request")
    public void handleJobRequest(@DestinationVariable int gameId,
                                 @DestinationVariable String playerName,
                                 @Payload JobRequestMessage msg) {
        boolean hasDegree = false;
        //boolean hasDegree = playerService.hasDegree(playerName);
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
                .collect(Collectors.toList());

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

    private String getPlayerIdSafe(Lobby lobby, int index) {
        if (lobby == null || lobby.getPlayers() == null) {
            return "";
        }

        List<Player> players = lobby.getPlayers();
        if (index >= players.size()) {
            return "";
        }

        Player player = players.get(index);
        if (player == null || player.getId() == null) {
            return "";
        }

        return player.getId();
    }
    /**
     * 1) Auswahl-Request:
     *    Pfad enthält gameId und playerName, damit nur der richtige Client die Nachricht bekommt.
     */
    @MessageMapping("/houses/{gameId}/{playerName}/choose")
    public void handleHouseAction(@DestinationVariable int gameId,
                                  @DestinationVariable String playerName,
                                  @Payload HouseBuyElseSellMessage msg) {
        List<HouseMessage> options = houseService.handleHouseAction(
                gameId,
                playerName,
                msg.isBuyElseSell()
        );

        String dest = String.format("/topic/%d/houses/%s/options",
                gameId,
                playerName
        );
        messagingTemplate.convertAndSend(dest, options);
    }

    /**
     * 2) Final-Request:
     *    Pfad enthält gameName und playerID, colorValue wird intern gerollt.
     */
    @MessageMapping("/houses/{gameId}/{playerName}/finalize")
    public void finalizeHouseAction(@DestinationVariable int gameId,
                                    @DestinationVariable String playerName,
                                    @Payload HouseMessage houseMsg) {
        // Aufruf auf der Bean-Instanz, nicht statisch
        HouseMessage confirmation = houseService.finalizeHouseAction(
                gameId,
                playerName,
                houseMsg.getHouseId()
        );

        String dest = String.format("/topic/%d/houses/%s/confirmation",
                gameId,
                playerName
        );
        messagingTemplate.convertAndSend(dest, confirmation);
    }
    @MessageMapping("/game/createHouseRepo/{gameId}")
    public void handleHouseRepoCreation(@DestinationVariable int gameId) {
        // Erstelle (oder liefere zurück) das House-Repository
        houseService.getOrCreateRepository(gameId);

        // Optional: sende eine Statusmeldung an alle Clients
        String destination = "/topic/game/" + gameId + "/status";
        messagingTemplate.convertAndSend(
                destination,
                new OutputMessage(
                        "System",
                        "House-Repository für Spiel " + gameId + " wurde angelegt.",
                        now()
                )
        );
    }

}