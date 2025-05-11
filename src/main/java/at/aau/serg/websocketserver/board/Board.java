package at.aau.serg.websocketserver.board;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@RequiredArgsConstructor // erzeugt Konstruktor für final-Felder (fields)
public class Board {

    private final List<Field> fields;
    private final Map<Integer, Integer> playerPositions = new HashMap<>();

    public void addPlayer(int playerId, int startFieldIndex) {
        playerPositions.put(playerId, startFieldIndex);
    }

    public void movePlayer(int playerId, int steps) {
        int current = playerPositions.getOrDefault(playerId, 0);
        for (int i = 0; i < steps; i++) {
            Field field = fields.get(current);
            if (field.getNextFields().isEmpty()) break;
            if (field.getNextFields().size() > 1) break;
            current = field.getNextFields().get(0);
        }
        playerPositions.put(playerId, current);
    }

    public Field getPlayerField(int playerId) {
        return fields.get(playerPositions.getOrDefault(playerId, 0));
    }

    public void manualMove(int playerId, int targetFieldIndex) {
        Field currentField = fields.get(playerPositions.getOrDefault(playerId, 0));
        if (currentField.getNextFields().contains(targetFieldIndex)) {
            playerPositions.put(playerId, targetFieldIndex);
        }
    }
}
