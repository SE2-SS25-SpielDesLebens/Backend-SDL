package at.aau.serg.websocketserver.game;

import at.aau.serg.websocketserver.board.BoardService;
import at.aau.serg.websocketserver.lobby.Lobby;
import at.aau.serg.websocketserver.lobby.LobbyService;
import at.aau.serg.websocketserver.player.Player;
import at.aau.serg.websocketserver.session.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Service zur Verwaltung der Spiellogik.
 * Enthält Logik, die zuvor in WebSocketBrokerController war.
 */
@Service
public class GameService {
    private final JobService jobService;
    private final BoardService boardService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public GameService(JobService jobService, 
                       BoardService boardService, 
                       SimpMessagingTemplate messagingTemplate) {
        this.jobService = jobService;
        this.boardService = boardService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Führt einen Spielzug für einen Spieler aus, wenn das Spiel bereits gestartet ist.
     *
     * @param gameId Die ID des Spiels
     * @param playerName Der Name des Spielers
     * @param steps Die Anzahl der Schritte (Würfelwurf)
     * @return true, wenn der Zug ausgeführt werden konnte, sonst false
     */
    public boolean performGameTurn(String gameId, String playerName, int steps) {
        Lobby lobby = LobbyService.getInstance().getLobby(gameId);
        if (lobby != null && lobby.isStarted()) {
            GameLogic logic = lobby.getGameLogic();
            if (logic != null) {
                Player player = logic.getPlayerByName(playerName);
                if (player != null) {
                    logic.performTurn(player, steps);
                    return true;
                }
            }
        }
        return false;
    }
      /**
     * Bereitet ein neues Spiel vor und startet es.
     *
     * @param gameId Die ID des Spiels
     * @return true, wenn das Spiel gestartet werden konnte, sonst false
     */
    public boolean startGame(int gameId) {
        // Repository vorbereiten
        jobService.getOrCreateRepository(gameId);

        // Lobby und Spieler holen
        Lobby lobby = LobbyService.getInstance().getLobby(Integer.toString(gameId));
        if (lobby == null || lobby.isStarted()) {
            System.out.println("[WARNUNG] Lobby nicht gefunden oder bereits gestartet: " + gameId);
            return false;
        }

        // GameLogic erzeugen
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
        
        return true;
    }
      /**
     * Beendet ein laufendes Spiel.
     *
     * @param gameId Die ID des Spiels
     * @return Eine Meldung über den Erfolg oder Misserfolg
     */
    public String endGame(String gameId) {
        Lobby lobby = LobbyService.getInstance().getLobby(gameId);
        if (lobby == null || !lobby.isStarted()) {
            return "Spiel nicht gefunden oder nicht gestartet";
        }

        GameLogic game = lobby.getGameLogic();
        if (game == null) {
            return "Keine Spielinstanz vorhanden";
        }

        game.endGame(); // Dies ruft die finale Auswertung auf
        lobby.setStarted(false); // Spiel wird beendet

        return "Spiel wurde manuell beendet!";
    }
    
    /**
     * Ermittelt die Anzahl der Spieler in einer Lobby.
     *
     * @param gameId Die ID des Spiels
     * @return Die Anzahl der Spieler oder 0, wenn die Lobby nicht existiert
     */
    public int getPlayerCount(String gameId) {
        Lobby lobby = LobbyService.getInstance().getLobby(gameId);
        if (lobby == null) {
            return 0;
        }
        return lobby.getPlayers().size();
    }
}
