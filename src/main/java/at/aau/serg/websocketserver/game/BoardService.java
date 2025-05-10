package at.aau.serg.websocketserver.game;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class BoardService {
    
    private Map<String, GameState> games = new HashMap<>();
    
    public GameState createGame(String gameId) {
        GameState gameState = new GameState(gameId);
        games.put(gameId, gameState);
        return gameState;
    }
    
    public boolean addPlayer(String gameId, String playerId, int startFieldIndex) {
        GameState game = games.get(gameId);
        if (game == null) return false;
        
        game.addPlayer(playerId, startFieldIndex);
        return true;
    }
    
    public MoveResult movePlayer(String gameId, String playerId, int steps) {
        GameState game = games.get(gameId);
        if (game == null) return null;
        
        return game.movePlayer(playerId, steps);
    }
    
    public MoveResult manualMoveTo(String gameId, String playerId, int fieldIndex) {
        GameState game = games.get(gameId);
        if (game == null) return null;
        
        return game.manualMoveTo(playerId, fieldIndex);
    }
    
    public GameState getGame(String gameId) {
        return games.get(gameId);
    }
}