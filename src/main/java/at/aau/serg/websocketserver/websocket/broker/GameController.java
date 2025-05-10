package at.aau.serg.websocketserver.websocket.broker;

import at.aau.serg.websocketserver.game.BoardService;
import at.aau.serg.websocketserver.game.GameState;
import at.aau.serg.websocketserver.game.MoveResult;
import at.aau.serg.websocketserver.messaging.dtos.GameActionRequest;
import at.aau.serg.websocketserver.messaging.dtos.GameActionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class GameController {

    @Autowired
    private BoardService boardService;

    @MessageMapping("/game/createGame")
    @SendTo("/topic/game")
    public GameActionResponse createGame(GameActionRequest request) {
        String gameId = request.getGameId();
        GameState gameState = boardService.createGame(gameId);
        
        return new GameActionResponse(
            "GAME_CREATED",
            request.getPlayerName(),
            gameId,
            gameState,
            LocalDateTime.now().toString()
        );
    }

    @MessageMapping("/game/joinGame")
    @SendTo("/topic/game")
    public GameActionResponse joinGame(GameActionRequest request) {
        String gameId = request.getGameId();
        String playerId = request.getPlayerName();
        int startField = request.getFieldIndex();
        
        boolean success = boardService.addPlayer(gameId, playerId, startField);
        GameState gameState = boardService.getGame(gameId);
        
        return new GameActionResponse(
            success ? "PLAYER_JOINED" : "JOIN_FAILED",
            playerId,
            gameId,
            gameState,
            LocalDateTime.now().toString()
        );
    }

    @MessageMapping("/game/movePlayer")
    @SendTo("/topic/game")
    public GameActionResponse movePlayer(GameActionRequest request) {
        String gameId = request.getGameId();
        String playerId = request.getPlayerName();
        int steps = request.getSteps();
        
        MoveResult result = boardService.movePlayer(gameId, playerId, steps);
        GameState gameState = boardService.getGame(gameId);
        
        return new GameActionResponse(
            result != null && result.isRequiresChoice() ? "CHOICE_REQUIRED" : "MOVED",
            playerId,
            gameId,
            gameState,
            result,
            LocalDateTime.now().toString()
        );
    }

    @MessageMapping("/game/chooseField")
    @SendTo("/topic/game")
    public GameActionResponse chooseField(GameActionRequest request) {
        String gameId = request.getGameId();
        String playerId = request.getPlayerName();
        int fieldIndex = request.getFieldIndex();
        
        MoveResult result = boardService.manualMoveTo(gameId, playerId, fieldIndex);
        GameState gameState = boardService.getGame(gameId);
        
        return new GameActionResponse(
            "FIELD_CHOSEN",
            playerId,
            gameId,
            gameState,
            result,
            LocalDateTime.now().toString()
        );
    }
}