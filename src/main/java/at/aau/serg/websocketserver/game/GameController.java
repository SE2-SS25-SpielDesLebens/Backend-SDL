package at.aau.serg.websocketserver.game;

import at.aau.serg.websocketserver.messaging.dtos.OutputMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controller für die Game-Aktionen
 */
@Controller
public class GameController {

    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, GameState> games = new ConcurrentHashMap<>();

    @Autowired
    public GameController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Spieler tritt dem Spiel bei
     */
    @MessageMapping("/game/joinGame")
    public void joinGame(JoinGameRequest request) {
        String gameId = request.getGameId();
        String playerName = request.getPlayerName();
        int startFieldIndex = request.getStartFieldIndex();

        // Spiel abrufen oder neues erstellen
        GameState game = games.computeIfAbsent(gameId, GameState::new);
        game.addPlayer(playerName, startFieldIndex);

        // Antwort senden
        sendGameStateUpdate(gameId, game);
    }

    /**
     * Spieler bewegt sich
     */
    @MessageMapping("/game/movePlayer")
    public void movePlayer(MovePlayerRequest request) {
        String gameId = request.getGameId();
        String playerName = request.getPlayerName();
        int steps = request.getSteps();

        // Spiel abrufen
        GameState game = games.get(gameId);
        if (game == null) {
            sendErrorMessage(gameId, playerName, "Spiel nicht gefunden");
            return;
        }

        // Spieler bewegen
        MoveResult result = game.movePlayer(playerName, steps);

        // Nur wenn keine Auswahl nötig ist, den Spielstatus aktualisieren
        if (!result.isRequiresChoice()) {
            sendGameStateUpdate(gameId, game);
        } else {
            // Sonst Auswahlmöglichkeiten senden
            sendChoiceOptions(gameId, playerName, result);
        }
    }

    /**
     * Spieler wählt ein Feld
     */
    @MessageMapping("/game/chooseField")
    public void chooseField(ChooseFieldRequest request) {
        String gameId = request.getGameId();
        String playerName = request.getPlayerName();
        int fieldIndex = request.getFieldIndex();

        // Spiel abrufen
        GameState game = games.get(gameId);
        if (game == null) {
            sendErrorMessage(gameId, playerName, "Spiel nicht gefunden");
            return;
        }

        // Spieler zum gewählten Feld bewegen
        MoveResult result = game.manualMoveTo(playerName, fieldIndex);
        
        // Spielstatus aktualisieren
        sendGameStateUpdate(gameId, game);
    }

    /**
     * Sendet ein Update des Spielstatus
     */
    private void sendGameStateUpdate(String gameId, GameState game) {
        GameActionResponse response = new GameActionResponse();
        
        // Backend-Status für Frontend konvertieren
        Map<String, Object> backendState = new HashMap<>();
        backendState.put("playerPositions", game.getPlayerPositions());
        backendState.put("gameId", gameId);
        
        response.setGameState(backendState);
        response.setSuccess(true);

        messagingTemplate.convertAndSend("/topic/game/" + gameId, response);
        // Auch an das allgemeine Topic senden für Clients, die noch nicht auf das spezifische Topic abonniert haben
        messagingTemplate.convertAndSend("/topic/game", response);
    }

    /**
     * Sendet eine Fehlermeldung
     */
    private void sendErrorMessage(String gameId, String playerName, String message) {
        GameActionResponse response = new GameActionResponse();
        response.setMessage(message);
        response.setSuccess(false);

        messagingTemplate.convertAndSend("/topic/game/" + gameId, response);
        messagingTemplate.convertAndSend("/topic/game", response);
    }

    /**
     * Sendet Auswahlmöglichkeiten für einen Spieler
     */
    private void sendChoiceOptions(String gameId, String playerName, MoveResult result) {
        GameActionResponse response = new GameActionResponse();
        response.setMoveResult(result);
        response.setSuccess(true);

        messagingTemplate.convertAndSend("/topic/game/" + gameId, response);
        messagingTemplate.convertAndSend("/topic/game", response);
    }
}
