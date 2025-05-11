package at.aau.serg.websocketserver.board;

import org.springframework.stereotype.Service;

@Service
public class BoardService {

    private final Board board;

    public BoardService() {
        this.board = new Board(BoardFactory.createDefaultBoard());
    }

    public void movePlayer(int playerId, int steps) {
        board.movePlayer(playerId, steps);
    }

    public Field getPlayerField(int playerId) {
        return board.getPlayerField(playerId);
    }

    public void addPlayer(int playerId, int startIndex) {
        board.addPlayer(playerId, startIndex);
    }

    public void manualMove(int playerId, int nextField) {
        board.manualMove(playerId, nextField);
    }
}
