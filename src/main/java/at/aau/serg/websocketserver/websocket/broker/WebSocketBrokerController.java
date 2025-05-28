package at.aau.serg.websocketserver.websocket.broker;

import at.aau.serg.websocketserver.board.BoardDataService;
import at.aau.serg.websocketserver.game.GameService;
import at.aau.serg.websocketserver.job.JobWrapperService;
import at.aau.serg.websocketserver.lobby.LobbyManagementService;
import at.aau.serg.websocketserver.movement.MovementService;
import at.aau.serg.websocketserver.board.BoardService;
import at.aau.serg.websocketserver.messaging.dtos.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;


@Controller
public class WebSocketBrokerController {
    private final JobWrapperService jobWrapperService;
    private final SimpMessagingTemplate messagingTemplate;
    private final MovementService movementService;
    private final GameService gameService;
    private final LobbyManagementService lobbyManagementService;
    private final BoardDataService boardDataService;
      @Autowired
    public WebSocketBrokerController(SimpMessagingTemplate messagingTemplate,
                                     BoardService boardService, // wird für Service-Klassen benötigt, aber nicht direkt verwendet
                                     MovementService movementService,
                                     GameService gameService,
                                     LobbyManagementService lobbyManagementService,
                                     BoardDataService boardDataService,
                                     JobWrapperService jobWrapperService) {
        this.messagingTemplate = messagingTemplate;
        this.movementService = movementService;
        this.gameService = gameService;
        this.lobbyManagementService = lobbyManagementService;
        this.boardDataService = boardDataService;
        this.jobWrapperService = jobWrapperService;
    }/**
     * Liefert die aktuellen Spielbrettdaten an den Client.
     * Diese Methode kann vom Frontend aufgerufen werden, um die vollständigen Board-Daten zu erhalten.
     */
    @MessageMapping("/board/data")
    public void handleBoardDataRequest() {
        // Verwende den BoardDataService
        BoardDataMessage boardDataMessage = new BoardDataMessage(
                boardDataService.getBoardData(),
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
        // JobWrapperService verwenden, um das Repository zu erstellen
        jobWrapperService.createJobRepository(gameId);

        // Optional: sende eine Statusmeldung an alle Clients
        String destination = "/topic/game/" + gameId + "/status";
        messagingTemplate.convertAndSend(
                destination,
                new OutputMessage(
                        "System",
                        "Job-Repository für Spiel " + gameId + " wurde angelegt.",
                        now()
                )
        );
    }@MessageMapping("/move")
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
                
                // MovementService zum Hinzufügen des Spielers verwenden
                MoveMessage moveMessage = movementService.addPlayerToBoard(playerId, startFieldIndex);
                
                messagingTemplate.convertAndSend("/topic/game", moveMessage);
                return;
            } catch (Exception e) {
                messagingTemplate.convertAndSend("/topic/game",
                        new OutputMessage(message.getPlayerName(), "❌ Fehler beim Betreten des Spielfelds", LocalDateTime.now().toString()));
                return;
            }
        }
        
        // Prüfe, ob es eine direkte Bewegung zu einem bestimmten Feld ist
        if (action != null && action.startsWith("move:")) {
            try {
                int targetFieldIndex = Integer.parseInt(action.substring(5));
                
                // MovementService zum Bewegen des Spielers verwenden
                MoveMessage moveMessage = movementService.movePlayerToSpecificField(playerId, targetFieldIndex);
                
                if (moveMessage != null) {
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
        Integer currentFieldOverride = null;
        if (message.getAction().contains(":")) {
            String[] parts = message.getAction().split(":");
            if (parts.length > 1) {
                try {
                    currentFieldOverride = Integer.parseInt(parts[1]);
                } catch (NumberFormatException e) {
                    // Ignoriere Fehler hier
                }
            }
        }
        
        // MovementService zum Bewegen des Spielers mit Würfel verwenden
        MoveMessage moveMessage = movementService.movePlayerWithDiceRoll(playerId, steps, currentFieldOverride);
        
        messagingTemplate.convertAndSend("/topic/game", moveMessage);        // Spiellogik mit GameService aktualisieren
        gameService.performGameTurn(message.getGameId(), message.getPlayerName(), steps);
    }    @MessageMapping("/lobby")
    public void handleLobby(@Payload StompMessage message) {
        // LobbyManagementService verwenden, um die Lobby-Anfrage zu verarbeiten
        OutputMessage outputMessage = lobbyManagementService.handleLobbyAction(
                message.getAction(),
                message.getGameId(),
                message.getPlayerName()
        );
        
        messagingTemplate.convertAndSend("/topic/lobby", outputMessage);
    }    @MessageMapping("/lobby/create")
    @SendTo("/queue/lobby/created")
    public void handleLobbyCreate(@Payload LobbyRequestMessage request, Principal principal){
        // LobbyManagementService verwenden, um eine neue Lobby zu erstellen
        LobbyResponseMessage response = lobbyManagementService.createLobby(request, principal);
        
        messagingTemplate.convertAndSendToUser(
                request.getPlayerName(),   // = Principal.getName(), wenn korrekt verwendet
                "/queue/lobby/created",    // Ziel für den Client
                response
        );
        System.out.println("Nachricht gesendet");
    }    @MessageMapping("/{lobbyid}/join")
    @SendTo("/topic/{lobbyid}")
    public void handlePlayerJoin(@DestinationVariable String lobbyid, @Payload LobbyRequestMessage request){
        // LobbyManagementService verwenden, um einen Spieler einer Lobby beitreten zu lassen
        LobbyResponseMessage response = lobbyManagementService.joinLobby(lobbyid, request);
        
        String destination = String.format("/topic/%s", lobbyid);
        messagingTemplate.convertAndSend(destination, response);
    }    @MessageMapping("/{lobbyid}/leave")
    public void handlePlayerLeave(@DestinationVariable String lobbyid, @Payload LobbyRequestMessage request){
        // LobbyManagementService verwenden, um einen Spieler aus einer Lobby zu entfernen
        lobbyManagementService.leaveLobby(lobbyid, request);
    }

    @MessageMapping("/chat")
    public void handleChat(@Payload StompMessage message) {
        messagingTemplate.convertAndSend("/topic/chat",
                new OutputMessage(message.getPlayerName(), message.getMessageText(), LocalDateTime.now().toString()));
    }    @MessageMapping("/game/start/{gameId}")
    public void handleGameStart(@DestinationVariable int gameId) {
        // GameService für die Spiellogik verwenden
        boolean started = gameService.startGame(gameId);
        
        // Falls erfolgreich, sende Nachricht an alle
        if (started) {
            // Anzahl der Spieler über GameService ermitteln
            int playerCount = gameService.getPlayerCount(Integer.toString(gameId));
            messagingTemplate.convertAndSend(
                    "/topic/game/" + gameId + "/status",
                    "Das Spiel wurde gestartet. Spieleranzahl: " + playerCount
            );
        }
    }@MessageMapping("/game/end/{gameId}")
    @SendTo("/topic/game/{gameId}/status")
    public OutputMessage handleGameEnd(@DestinationVariable String gameId) {
        // GameService für das Beenden des Spiels verwenden
        String message = gameService.endGame(gameId);
        return new OutputMessage("System", message, now());
    }

    private String now() {
        return LocalDateTime.now().toString();
    }

    @MessageMapping("/jobs/{gameId}/{playerName}/request")
    public void handleJobRequest(@DestinationVariable int gameId,
                                 @DestinationVariable String playerName,
                                 @Payload JobRequestMessage msg) {
        // JobWrapperService verwenden, um die Job-Anfrage zu verarbeiten
        jobWrapperService.handleJobRequest(gameId, playerName, msg.hasDegree());
    }    @MessageMapping("/jobs/{gameId}/{playerName}/select")
    public void handleJobSelection(@DestinationVariable int gameId,
                                   @DestinationVariable String playerName,
                                   @Payload JobMessage msg) {
        // JobWrapperService verwenden, um die Job-Auswahl zu verarbeiten
        jobWrapperService.handleJobSelection(gameId, playerName, msg.getJobId());
    }
}