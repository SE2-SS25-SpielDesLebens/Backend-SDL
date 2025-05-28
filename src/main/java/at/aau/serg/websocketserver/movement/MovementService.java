package at.aau.serg.websocketserver.movement;

import at.aau.serg.websocketserver.board.BoardService;
import at.aau.serg.websocketserver.board.Field;
import at.aau.serg.websocketserver.messaging.dtos.MoveMessage;
import at.aau.serg.websocketserver.messaging.dtos.OutputMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service zur Verwaltung der Spielerbewegungen.
 * Enthält Logik, die zuvor in WebSocketBrokerController war.
 */
@Service
public class MovementService {
    private final BoardService boardService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public MovementService(BoardService boardService, SimpMessagingTemplate messagingTemplate) {
        this.boardService = boardService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Fügt einen Spieler zum Spielbrett hinzu.
     *
     * @param playerId Der Spieler, der hinzugefügt werden soll
     * @param startFieldIndex Das Startfeld des Spielers
     * @return Eine MoveMessage für die Antwort
     */
    public MoveMessage addPlayerToBoard(int playerId, int startFieldIndex) {
        try {
            boardService.addPlayer(playerId, startFieldIndex);
            Field currentField = boardService.getPlayerField(playerId);

            // Mögliche nächste Felder ermitteln
            List<Integer> nextPossibleFieldIndices = new ArrayList<>();
            for (Field nextField : boardService.getValidNextFields(playerId)) {
                nextPossibleFieldIndices.add(nextField.getIndex());
            }

            return new MoveMessage(
                    String.valueOf(playerId),
                    currentField.getIndex(),
                    currentField.getType(),
                    LocalDateTime.now().toString(),
                    nextPossibleFieldIndices
            );
        } catch (Exception e) {
            // In einer Service-Klasse sollten wir die Exception weitergeben
            // statt sie zu verschlucken, damit der Controller sie behandeln kann
            throw new RuntimeException("Fehler beim Betreten des Spielfelds", e);
        }
    }

    /**
     * Bewegt einen Spieler zu einem bestimmten Feld.
     *
     * @param playerId Der Spieler, der bewegt werden soll
     * @param targetFieldIndex Das Zielfeld
     * @return Eine MoveMessage für die Antwort oder null bei Fehler
     */
    public MoveMessage movePlayerToSpecificField(int playerId, int targetFieldIndex) {
        try {
            boolean success = boardService.movePlayerToField(playerId, targetFieldIndex);
            if (success) {
                Field currentField = boardService.getPlayerField(playerId);
                List<Integer> nextPossibleFieldIndices = new ArrayList<>();
                for (Field nextField : boardService.getValidNextFields(playerId)) {
                    nextPossibleFieldIndices.add(nextField.getIndex());
                }

                return new MoveMessage(
                        String.valueOf(playerId),
                        currentField.getIndex(),
                        currentField.getType(),
                        LocalDateTime.now().toString(),
                        nextPossibleFieldIndices
                );
            }
            return null; // Ungültiger Zug
        } catch (Exception e) {
            throw new RuntimeException("Fehler bei der Bewegung", e);
        }
    }

    /**
     * Bewegt einen Spieler basierend auf einer Würfelzahl.
     *
     * @param playerId Der Spieler, der bewegt werden soll
     * @param steps Die Anzahl der Schritte (Würfelzahl)
     * @param currentFieldOverride Ein optionales Feld, von dem aus die Bewegung starten soll
     * @return Eine MoveMessage für die Antwort
     */
    public MoveMessage movePlayerWithDiceRoll(int playerId, int steps, Integer currentFieldOverride) {
        // Wenn eine bestimmte Ausgangsposition mitgeschickt wurde, setzen wir diese
        if (currentFieldOverride != null) {
            if (currentFieldOverride >= 0 && currentFieldOverride < boardService.getBoardSize()) {
                boardService.setPlayerPosition(playerId, currentFieldOverride);
            }
        }

        // Mögliche Zielfelder berechnen
        List<Integer> moveOptions = boardService.getMoveOptions(String.valueOf(playerId), steps);
        
        // Aktuelle Position und Feld des Spielers
        Field currentField = boardService.getPlayerField(playerId);
        
        // Bestimme das Zielfeld basierend auf der gewürfelten Zahl
        int targetIndex;
        
        // Prüfe, ob wir direkt auf ein Feld basierend auf der Würfelzahl (steps) setzen können
        if (steps > 0 && steps <= moveOptions.size()) {
            // Die gewürfelte Zahl ist im gültigen Bereich der Optionen
            // Wir verwenden steps-1 als Index, da die Liste bei 0 beginnt, aber die Würfelzahl bei 1
            targetIndex = moveOptions.get(steps - 1);
            boardService.movePlayerToField(playerId, targetIndex);
            currentField = boardService.getPlayerField(playerId); // Aktualisiere das Feld nach der Bewegung
        } else if (steps > moveOptions.size() && !moveOptions.isEmpty()) {
            // Die gewürfelte Zahl ist größer als die Anzahl der Optionen
            // Wir bewegen zum letzten verfügbaren Feld (Stop-Feld)
            targetIndex = moveOptions.get(moveOptions.size() - 1);
            boardService.movePlayerToField(playerId, targetIndex);
            currentField = boardService.getPlayerField(playerId); // Aktualisiere das Feld nach der Bewegung
        }
        
        // Hole die nächsten möglichen Felder nach der Bewegung
        List<Integer> nextPossibleFieldIndices = new ArrayList<>();
        for (Field nextField : boardService.getValidNextFields(playerId)) {
            nextPossibleFieldIndices.add(nextField.getIndex());
        }
        
        // Erstelle die Nachricht für den Client
        return new MoveMessage(
                String.valueOf(playerId),
                currentField.getIndex(),
                currentField.getType(),
                LocalDateTime.now().toString(),
                nextPossibleFieldIndices
        );
    }
}
