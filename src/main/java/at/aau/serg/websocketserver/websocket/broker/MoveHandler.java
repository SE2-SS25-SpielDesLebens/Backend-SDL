package at.aau.serg.websocketserver.websocket.broker;

import at.aau.serg.websocketserver.session.board.BoardService;
import at.aau.serg.websocketserver.session.board.Field;
import at.aau.serg.websocketserver.session.board.FieldType;
import at.aau.serg.websocketserver.messaging.dtos.MoveMessage;
import at.aau.serg.websocketserver.messaging.dtos.StompMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Controller zur Verarbeitung von Spielzügen/Bewegungen
 */
@Controller
public class MoveHandler {
    private final BoardService boardService;
    private static final Pattern DICE_ROLL_PATTERN = Pattern.compile("(\\d+) gewürfelt(?::(\\d+))?");
    
    @Autowired
    public MoveHandler(BoardService boardService) {
        this.boardService = boardService;
    }

    /**
     * Verarbeitet eine Bewegungsnachricht vom Client
     * Format der Nachricht: "X gewürfelt:Y" wobei X die Würfelzahl und Y der aktuelle Feldindex ist
     * 
     * @param message Die empfangene Nachricht
     * @return Die Antwort mit der neuen Position
     */    @MessageMapping("/move")
    @SendTo("/topic/game")
    public MoveMessage handleMove(StompMessage message) {
        String playerName = message.getPlayerName();
        String action = message.getAction();
        
        System.out.println("🎲 MoveHandler: Empfange Nachricht von " + playerName + ", Aktion: " + action);
        
        // Aktuelle Zeit für Timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        
        // Nachricht in Format "X gewürfelt:Y" parsen
        Matcher matcher = DICE_ROLL_PATTERN.matcher(action);
        if (matcher.find()) {
            int diceRoll = Integer.parseInt(matcher.group(1));
            int currentFieldIndex = matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : 0;
            
            System.out.println("🎲 MoveHandler: Würfelwurf " + diceRoll + " von Feld " + currentFieldIndex);
            
            // Spielerbewegung berechnen
            MoveMessage response = processPlayerMove(playerName, diceRoll, currentFieldIndex, timestamp);
            System.out.println("🎲 MoveHandler: Sende Antwort - Zielfeld: " + response.getIndex() + ", Typ: " + response.getType() + ", Nächste Felder: " + response.getNextPossibleFields());
            return response;
        }
        
        // Fallback für ungültiges Format
        return new MoveMessage(playerName, 0, FieldType.AKTION, timestamp);
    }
    
    /**
     * Verarbeitet die Spielerbewegung basierend auf Würfelwurf
     */
    private MoveMessage processPlayerMove(String playerName, int diceRoll, int currentFieldIndex, String timestamp) {
        // Startfeld ermitteln
        Field currentField = boardService.getFieldByIndex(currentFieldIndex);
        if (currentField == null) {
            // Fallback: Nutze das erste Feld (Index 1)
            currentField = boardService.getFieldByIndex(1);
            if (currentField == null) {
                // Absolute Notfalloption
                return new MoveMessage(playerName, 0, FieldType.AKTION, timestamp);
            }
        }
        
        // Schritte gehen und Zielfeld ermitteln
        Field targetField = walkSteps(currentField, diceRoll);
        
        // Position des Spielers aktualisieren
        boardService.updatePlayerPosition(playerName, targetField.getIndex());
        
        // Liste der möglichen nächsten Felder bestimmen
        List<Integer> nextPossibleFields = targetField.getNextFields();
        
        // MoveMessage mit neuem Feld und möglichen nächsten Feldern zurückgeben
        return new MoveMessage(
            playerName, 
            targetField.getIndex(), 
            targetField.getType(), 
            timestamp, 
            nextPossibleFields
        );
    }
    
    /**
     * Bestimmt das Zielfeld basierend auf dem Würfelwert und der nextFields-Liste des Startfeldes
     * 
     * @param startField Das Startfeld
     * @param steps Die gewürfelte Anzahl der Schritte (Würfelwert)
     * @return Das Zielfeld nach dem Würfeln
     */
    private Field walkSteps(Field startField, int steps) {
        Field currentField = startField;
        List<Integer> nextFields = currentField.getNextFields();
        
        // Wenn es keine weiteren Felder gibt, bleiben wir hier stehen
        if (nextFields == null || nextFields.isEmpty()) {
            return currentField;
        }
        
        // Bestimme das Zielfeld basierend auf dem Würfelwert (steps)
        Integer nextFieldIndex;
        if (steps <= nextFields.size()) {
            // Wenn der Würfelwert kleiner oder gleich der Anzahl der nextFields ist,
            // nehmen wir den Eintrag an der Position (steps - 1)
            // (weil Listen in Java bei 0 beginnen)
            nextFieldIndex = nextFields.get(steps - 1);
        } else {
            // Wenn der Würfelwert größer ist als die Anzahl der nextFields,
            // nehmen wir den letzten verfügbaren Eintrag
            nextFieldIndex = nextFields.get(nextFields.size() - 1);
        }
        
        Field targetField = boardService.getFieldByIndex(nextFieldIndex);
        
        // Wenn das Zielfeld nicht gefunden wird, bleiben wir am aktuellen Feld
        if (targetField == null) {
            return currentField;
        }
        
        return targetField;
    }
}
