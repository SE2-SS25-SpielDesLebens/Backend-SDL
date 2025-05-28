package at.aau.serg.websocketserver.board;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service zur Verwaltung der Spielbrettdaten.
 * Enthält Logik, die zuvor in WebSocketBrokerController war.
 */
@Service
public class BoardDataService {
    private final BoardService boardService;
    
    @Autowired
    public BoardDataService(BoardService boardService) {
        this.boardService = boardService;
    }
    
    /**
     * Liefert alle Felder des Spielbretts.
     *
     * @return Eine Liste aller Felder des Spielbretts
     */
    public List<Field> getBoardData() {
        return boardService.getBoard();
    }
    
    /**
     * Liefert ein bestimmtes Feld des Spielbretts.
     *
     * @param index Der Index des gesuchten Felds
     * @return Das gesuchte Feld oder null, wenn kein Feld mit diesem Index existiert
     */
    public Field getFieldByIndex(int index) {
        return boardService.getFieldByIndex(index);
    }
    
    /**
     * Liefert die Anzahl der Felder des Spielbretts.
     *
     * @return Die Anzahl der Felder
     */
    public int getBoardSize() {
        return boardService.getBoardSize();
    }
    
    /**
     * Liefert das Feld, auf dem sich ein Spieler befindet.
     *
     * @param playerId Die ID des Spielers
     * @return Das Feld, auf dem sich der Spieler befindet
     */
    public Field getPlayerField(Object playerId) {
        return boardService.getPlayerField(playerId);
    }
    
    /**
     * Liefert alle gültigen nächsten Felder für einen Spieler.
     *
     * @param playerId Die ID des Spielers
     * @return Eine Liste aller gültigen nächsten Felder
     */


    /**
     * Liefert alle gültigen nächsten Felder für einen Spieler.
     *
     * @param playerId Die ID des Spielers
     * @return Eine Liste aller gültigen nächsten Felder
     */
    public List<Field> getValidNextFields(int playerId) {
        return boardService.getValidNextFields(playerId);
    }
    
    /**
     * Liefert alle Spieler, die sich auf einem bestimmten Feld befinden.
     *
     * @param fieldIndex Der Index des Felds
     * @return Eine Liste aller Spieler-IDs, die sich auf diesem Feld befinden
     */
    public List<String> getPlayersOnField(int fieldIndex) {
        return boardService.getPlayersOnField(fieldIndex);
    }
}
