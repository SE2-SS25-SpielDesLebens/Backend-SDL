package at.aau.serg.websocketserver.websocket.broker;

import Game.GameController;
import Game.GameLogic;
import Game.PlayerTurnManager;
import at.aau.serg.websocketserver.Player.Player;
import at.aau.serg.websocketserver.Player.PlayerService;
import at.aau.serg.websocketserver.board.BoardService;
import at.aau.serg.websocketserver.board.Field;
import at.aau.serg.websocketserver.lobby.Lobby;
import at.aau.serg.websocketserver.lobby.LobbyService;
import at.aau.serg.websocketserver.messaging.dtos.*;
import at.aau.serg.websocketserver.session.Job;
import at.aau.serg.websocketserver.session.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private final BoardService boardService;

    @Autowired
    public WebSocketBrokerController(JobService jobService,
                                     SimpMessagingTemplate messagingTemplate,
                                     BoardService boardService) {
        this.jobService = jobService;
        this.messagingTemplate = messagingTemplate;
        playerService = PlayerService.getInstance();
        lobbyService = LobbyService.getInstance();
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
                boolean success = boardService.movePlayerToField(playerId, targetFieldIndex);
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
        }

        boardService.movePlayer(playerId, steps);                Field currentField = boardService.getPlayerField(playerId);
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

        Lobby lobby = LobbyService.getInstance().getLobby(message.getGameId());
        if (lobby != null && lobby.isStarted()) {
            GameLogic logic = lobby.getGameLogic();
            if (logic != null) {
                Player player = logic.getPlayerByName(message.getPlayerName());
                logic.performTurn(player, steps);
            }
        }

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

        System.out.println("[LOBBY] [" + gameId + "] " + message.getPlayerName() + ": " + content);

        messagingTemplate.convertAndSend("/topic/lobby",
                new OutputMessage(message.getPlayerName(), content, LocalDateTime.now().toString()));
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
            System.out.println("Spieler" + request.getPlayerName() + " ist beigetreten");

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
    public void handleChat(@Payload StompMessage message) {
        messagingTemplate.convertAndSend("/topic/chat",
                new OutputMessage(message.getPlayerName(), message.getMessageText(), LocalDateTime.now().toString()));
    }

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
        gameLogic.setGameId(Integer.parseInt(gameId));
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
