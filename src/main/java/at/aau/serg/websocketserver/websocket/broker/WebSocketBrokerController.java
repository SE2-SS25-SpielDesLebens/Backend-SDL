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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    @Autowired
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
    public void handleJobRepoCreation(@DestinationVariable String gameId) {
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

    /**
     * Diese Methode wurde entfernt und mit der Implementation im MoveHandler ersetzt,
     * um doppelte MessageMapping-Definitionen zu vermeiden.
     * 
     * @see at.aau.serg.websocketserver.websocket.broker.MoveHandler#handleMove(StompMessage)
     */
    // @MessageMapping("/move") - Entfernt wegen Konflikt mit MoveHandler
    public void handleLegacyMove(StompMessage message) {
        int playerId;
        try {
            playerId = Integer.parseInt(message.getPlayerName()); // Annahme: playerName = ID
        } catch (NumberFormatException e) {
            messagingTemplate.convertAndSend("/topic/game",
                    new OutputMessage(message.getPlayerName(), "❌ Ungültige Spieler-ID", LocalDateTime.now().toString()));
            return;
        }

        String action = message.getAction();
        // Prüfe, ob es ein "join:X" Befehl ist (Spieler betritt das Spielfeld)
        if (action != null && action.startsWith("join:")) {
            try {
                int startFieldIndex = Integer.parseInt(action.substring(5));
                boardService.addPlayer(playerId, startFieldIndex);
                Field currentField = boardService.getPlayerField(playerId);

                // Get the possible next fields
                List<Integer> nextPossibleFieldIndices = new ArrayList<>();
                for (Field nextField : boardService.getValidNextFields(playerId)) {
                    nextPossibleFieldIndices.add(nextField.getIndex());
                }

                MoveMessage moveMessage = new MoveMessage(
                        message.getPlayerName(),
                        currentField.getIndex(),
                        currentField.getType(),
                        LocalDateTime.now().toString(),
                        nextPossibleFieldIndices
                );

                messagingTemplate.convertAndSend("/topic/game", moveMessage);
                return;
            } catch (Exception e) {
                messagingTemplate.convertAndSend("/topic/game",
                        new OutputMessage(message.getPlayerName(), "❌ Fehler beim Betreten des Spielfelds", LocalDateTime.now().toString()));
                return;
            }
        }        // Prüfe, ob es eine direkte Bewegung zu einem bestimmten Feld ist
        if (action != null && action.startsWith("move:")) {
            try {
                int targetFieldIndex = Integer.parseInt(action.substring(5));
                boolean success = boardService.movePlayerToField(String.valueOf(playerId), targetFieldIndex);
                if (success) {
                    Field currentField = boardService.getPlayerField(playerId);
                    List<Integer> nextPossibleFieldIndices = new ArrayList<>();
                    for (Field nextField : boardService.getValidNextFields(playerId)) {
                        nextPossibleFieldIndices.add(nextField.getIndex());
                    }

                    MoveMessage moveMessage = new MoveMessage(
                            message.getPlayerName(),
                            currentField.getIndex(),
                            currentField.getType(),
                            LocalDateTime.now().toString(),
                            nextPossibleFieldIndices
                    );
                    messagingTemplate.convertAndSend("/topic/game", moveMessage);
                } else {
                    messagingTemplate.convertAndSend("/topic/game",
                            new OutputMessage(message.getPlayerName(), "❌ Ungültiger Zug", LocalDateTime.now().toString()));
                }
                return;
            } catch (Exception e) {
                messagingTemplate.convertAndSend("/topic/game",
                        new OutputMessage(message.getPlayerName(), "❌ Fehler bei der Bewegung", LocalDateTime.now().toString()));
                return;
            }
        }

        // Reguläre Bewegung mit Würfel
        int steps;
        try {
            steps = Integer.parseInt(message.getAction().replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            messagingTemplate.convertAndSend("/topic/game",
                    new OutputMessage(message.getPlayerName(), "❌ Ungültige Würfelzahl", LocalDateTime.now().toString()));
            return;
        }

        // Prüfe ob eine bestimmte Ausgangsposition mitgeschickt wurde
        int currentFieldIndex;
        if (message.getAction().contains(":")) {
            String[] parts = message.getAction().split(":");
            if (parts.length > 1) {
                try {
                    currentFieldIndex = Integer.parseInt(parts[1]);
                    // Wenn eine gültige aktuelle Position mitgeschickt wurde, setzen wir diese
                    if (currentFieldIndex >= 0 && currentFieldIndex < boardService.getBoardSize()) {
                        boardService.setPlayerPosition(playerId, currentFieldIndex);
                    }
                } catch (NumberFormatException e) {
                    // Ignoriere Fehler hier
                }
            }
        }        // Anstatt direkt zu bewegen, berechnen wir die möglichen Zielfelder
        List<Integer> moveOptions = boardService.getMoveOptions(String.valueOf(playerId), steps);

        // Aktuelle Position und Feld des Spielers
        Field currentField = boardService.getPlayerField(playerId);

        // Bestimme das Zielfeld basierend auf der gewürfelten Zahl
        int targetIndex;

        // Prüfe, ob wir direkt auf ein Feld basierend auf der Würfelzahl (steps) setzen können
        if (steps > 0 && steps <= moveOptions.size()) {
            // Die gewürfelte Zahl ist im gültigen Bereich der Optionen
            // Wir verwenden steps-1 als Index, da die Liste bei 0 beginnt, aber die Würfelzahl bei 1
            targetIndex = moveOptions.get(steps - 1);
            boardService.movePlayerToField(String.valueOf(playerId), targetIndex);
            currentField = boardService.getPlayerField(playerId); // Aktualisiere das Feld nach der Bewegung
        } else if (steps > moveOptions.size() && !moveOptions.isEmpty()) {
            // Die gewürfelte Zahl ist größer als die Anzahl der Optionen
            // Wir bewegen zum letzten verfügbaren Feld (Stop-Feld)
            targetIndex = moveOptions.get(moveOptions.size() - 1);
            boardService.movePlayerToField(String.valueOf(playerId), targetIndex);
            currentField = boardService.getPlayerField(playerId); // Aktualisiere das Feld nach der Bewegung
        }

        // Hole die nächsten möglichen Felder nach der Bewegung
        List<Integer> nextPossibleFieldIndices = new ArrayList<>();
        for (Field nextField : boardService.getValidNextFields(playerId)) {
            nextPossibleFieldIndices.add(nextField.getIndex());
        }

        // Erstelle die Nachricht für den Client
        MoveMessage moveMessage = new MoveMessage(
                message.getPlayerName(),
                currentField.getIndex(),
                currentField.getType(),
                LocalDateTime.now().toString(),
                nextPossibleFieldIndices
        );

        messagingTemplate.convertAndSend("/topic/game", moveMessage);

        Lobby lobby = LobbyService.getInstance().getLobby(message.getGameId());
        if (lobby != null && lobby.isStarted()) {
            GameLogic logic = lobby.getGameLogic();
            if (logic != null) {
                Player player = logic.getPlayerByName(message.getPlayerName());
                logic.performTurn(player, steps);
            }
        }

    }

    @MessageMapping("/lobby/create")
    @SendTo("/queue/lobby/created")
    public void handleLobbyCreate(@Payload LobbyRequestMessage request) {
        String playerId = request.getPlayerName();

        Player player = playerService.getPlayerById(playerId);

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
            Player player = playerService.getPlayerById(playerId);

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
    @SendTo("/queue/player/check")
    public void handlePlayerExistenceCheckAndRegisterPlayer(@Payload LobbyRequestMessage message) {
        String playerId = message.getPlayerName();
        System.out.println("Player " + playerId + " hat sich angemeldet.");
        boolean success = playerService.createPlayerIfNotExists(playerId);
        PlayerCheckMessage return_message = new PlayerCheckMessage(playerId, success);
        String destination = "/queue/player/check";
        System.out.println(return_message);

        messagingTemplate.convertAndSendToUser(playerId, destination, return_message);
    }


    public void sendLobbyUpdates(String lobbyid) {
        Lobby lobby = lobbyService.getLobby(lobbyid);
        String player1 = getPlayerIdSafe(lobby, 0);
        String player2 = getPlayerIdSafe(lobby, 1);
        String player3 = getPlayerIdSafe(lobby, 2);
        String player4 = getPlayerIdSafe(lobby, 3);

        LobbyUpdateMessage message = new LobbyUpdateMessage(player1, player2, player3, player4, lobby.isStarted());
        System.out.println(message);
        String destination = String.format("/topic/lobby/%s", lobbyid);
        System.out.println(now());
        messagingTemplate.convertAndSend(destination, message);
        try {
            System.out.println(new ObjectMapper().writeValueAsString(message));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @MessageMapping("/{lobbyid}/leave")
    public void handlePlayerLeave(@DestinationVariable String lobbyid, @Payload LobbyRequestMessage request) {
        Lobby lobby = lobbyService.getLobby(lobbyid);
        System.out.printf("Spieler %s hat die Lobby verlassen\n", request.getPlayerName());
        lobby.removePlayer(playerService.getPlayerById(request.getPlayerName()));
        if(lobby.getPlayers().isEmpty()) {
            lobbyService.deleteLobby(lobbyid);
        }else{
        sendLobbyUpdates(lobbyid);
        }
    }

    @MessageMapping("/chat")
    public void handleChat(@Payload StompMessage message) {
        messagingTemplate.convertAndSend("/topic/chat",
                new OutputMessage(message.getPlayerName(), message.getMessageText(), LocalDateTime.now().toString()));
    }

    @MessageMapping("/game/start/{lobbyID}")
    public void handleGameStart(@DestinationVariable String lobbyID) {
        // 1. Repository vorbereiten
        jobService.getOrCreateRepository(lobbyID);
        System.out.println("Starte Spiel mit GameId: " + lobbyID);

        // 2. Lobby und Spieler holen
        Lobby lobby = lobbyService.getLobby(lobbyID);
        if (lobby == null || lobby.isStarted()) {
            System.out.println("[WARNUNG] Lobby nicht gefunden oder bereits gestartet: " + lobbyID);
            return;
        }

        // 3. GameLogic erzeugen
        GameLogic gameLogic = new GameLogic();
        gameLogic.setGameId(lobbyID);
        gameLogic.setJobService(jobService);
        gameLogic.setBoardService(boardService);
        gameLogic.setGameController(new GameController(gameLogic, messagingTemplate));
        gameLogic.setTurnManager(new PlayerTurnManager(gameLogic));


        gameLogic.prepareGameStart();
        lobby.setStarted(true);
        lobby.setGameLogic(gameLogic);

        sendLobbyUpdates(lobbyID);
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
    public void handleJobRequest(@DestinationVariable String gameId,
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
    public void handleJobSelection(@DestinationVariable String gameId,
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