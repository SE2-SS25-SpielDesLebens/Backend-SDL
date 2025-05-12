package at.aau.serg.websocketserver.fieldlogic;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BoardService {

    private final Map<String, Integer> playerPositions = new HashMap<>();

    //  Nachbildung des Spielfelds
    private final List<Field> board = List.of(
            new Field(0, FieldType.NEUTRAL, List.of(1)),
            new Field(1, FieldType.PAYDAY, List.of(2)),
            new Field(2, FieldType.ACTION, List.of(3)),
            new Field(3, FieldType.INVESTMENT, List.of(4)),
            new Field(4, FieldType.ACTION, List.of(5)),
            new Field(5, FieldType.STOP_FAMILY, List.of(6)),
            new Field(6, FieldType.ACTION, List.of(7)),
            new Field(7, FieldType.HOUSE, List.of(8)),
            new Field(8, FieldType.PAYDAY, List.of(9)),
            new Field(9, FieldType.ACTION, List.of(10)),
            new Field(10, FieldType.STOP_MARRIAGE, List.of(11)),
            new Field(11, FieldType.STOP_RETIREMENT, List.of())
    );

    public void addPlayer(String playerId, int startField) {
        playerPositions.put(playerId, startField);
    }

    public Field getCurrentField(String playerId) {
        int index = playerPositions.getOrDefault(playerId, 0);
        return board.get(index);
    }

    public FieldType getCurrentFieldType(String playerId) {
        return getCurrentField(playerId).getType();
    }

    public void movePlayer(String playerId, int steps) {
        int current = playerPositions.getOrDefault(playerId, 0);

        for (int i = 0; i < steps; i++) {
            List<Integer> next = board.get(current).getNextFields();
            if (next.isEmpty()) break;
            if (next.size() > 1) break; // Mehrere Pfade: Frontend muss wählen
            current = next.get(0);
        }

        playerPositions.put(playerId, current);
    }

    public void manualMoveTo(String playerId, int targetFieldIndex) {
        int current = playerPositions.getOrDefault(playerId, 0);
        List<Integer> allowed = board.get(current).getNextFields();
        if (allowed.contains(targetFieldIndex)) {
            playerPositions.put(playerId, targetFieldIndex);
        } else {
            throw new IllegalArgumentException("Bewegung zu diesem Feld nicht erlaubt.");
        }
    }

    public int getPlayerPosition(String playerId) {
        return playerPositions.getOrDefault(playerId, 0);
    }
}
