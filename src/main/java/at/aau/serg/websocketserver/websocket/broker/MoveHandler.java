package at.aau.serg.websocketserver.websocket.broker;

import at.aau.serg.websocketserver.session.board.BoardService;
import at.aau.serg.websocketserver.session.board.Field;
import at.aau.serg.websocketserver.session.board.FieldType;
import at.aau.serg.websocketserver.messaging.dtos.MoveMessage;
import at.aau.serg.websocketserver.messaging.dtos.PlayerPositionsMessage;
import at.aau.serg.websocketserver.messaging.dtos.StompMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import at.aau.serg.websocketserver.session.payout.PayoutService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Controller zur Verarbeitung von Spielzügen/Bewegungen
 */
@Controller
public class MoveHandler {
    private final BoardService boardService;
    private final SimpMessagingTemplate messagingTemplate;
    private static final Pattern DICE_ROLL_PATTERN = Pattern.compile("^(10|[1-9]) gewürfelt(?::(1[0-4][0-9]|[1-9]?[0-9]))?$");
    private final PayoutService payoutService;

    @Autowired
    public MoveHandler(BoardService boardService, SimpMessagingTemplate messagingTemplate, PayoutService payoutService) {
        this.boardService = boardService;
        this.messagingTemplate = messagingTemplate;
        this.payoutService = payoutService;
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
        
        if (action != null && action.equals("get-all-players")) {
            System.out.println("👥 DEBUG: Spieleranfrage erkannt für Spieler " + playerName);
            int currentPosition = getPlayerPosition(playerName);
            System.out.println("👥 DEBUG: Aktuelle Position von " + playerName + " ist " + currentPosition);
        }
        
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
            
            // Aktualisierte Positionen aller Spieler an alle Clients senden
            sendAllPlayerPositions(timestamp);
            
            return response;
        }
          // Wenn die Nachricht "join:X" ist, dann füge den Spieler zum Spielbrett hinzu
        if (action != null && action.startsWith("join:")) {
            try {
                int startFieldIndex = Integer.parseInt(action.substring(5));
                boardService.addPlayer(playerName, startFieldIndex);
                
                // Aktualisierte Positionen aller Spieler an alle Clients senden
                sendAllPlayerPositions(timestamp);
            } catch (Exception e) {
                System.out.println("⚠️ MoveHandler: Fehler beim Verarbeiten von 'join': " + e.getMessage());
            }
        }
        
        // Spezielle Behandlung für "get-all-players" Aktion
        if (action != null && action.equals("get-all-players")) {
            System.out.println("👥 MoveHandler: Anfrage für Spielerliste von " + playerName);
            
            // Sende alle Spielerpositionen, aber gebe KEINE Default-MoveMessage zurück
            sendAllPlayerPositions(timestamp);
            
            // Hole die aktuelle Position des Spielers anstatt auf 0 zurückzusetzen
            int currentPosition = boardService.getPlayerPosition(playerName);
            Field currentField = boardService.getFieldByIndex(currentPosition);
            
            if (currentField != null) {
                return new MoveMessage(
                    playerName,
                    currentField.getIndex(),
                    currentField.getType(),
                    timestamp,
                    currentField.getNextFields()
                );
            }
        }
        
        // Fallback für ungültiges Format
        return new MoveMessage(playerName, 0, FieldType.AKTION, timestamp);
    }
      /**
     * Sendet die Positionen aller Spieler an alle Clients.
     * 
     * @param timestamp Der aktuelle Zeitstempel
     */
    private void sendAllPlayerPositions(String timestamp) {
        Map<String, Integer> allPositions = boardService.getAllPlayerPositions();
        
        if (!allPositions.isEmpty()) {
            PlayerPositionsMessage positionsMessage = new PlayerPositionsMessage(allPositions, timestamp);
            messagingTemplate.convertAndSend("/topic/players/positions", positionsMessage);
            System.out.println("👥 MoveHandler: Sende Positionen aller " + allPositions.size() + " Spieler");
        }
    }
    
    /**
     * Hilfsmethode zur Ermittlung der aktuellen Position eines Spielers
     * 
     * @param playerName Die ID des Spielers
     * @return Die aktuelle Position des Spielers
     */
    private int getPlayerPosition(String playerName) {
        return boardService.getPlayerPosition(playerName);
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

        // 💸 Prüfe auf Auszahlung bei Zahltag
        if (targetField.getType() == FieldType.ZAHLTAG) {
            payoutService.handlePayoutAfterMovement(playerName);
        }


        // Liste der möglichen nächsten Felder bestimmen
        List<Integer> nextPossibleFields = targetField.getNextFields();


        // Erkenne, ob das Feld ein Zahltag-Feld ist (aktiv laut PayoutService)
        FieldType reportedType = targetField.getType();
        if (payoutService.isActivePaydayField(playerName, targetField.getIndex())) {
            reportedType = FieldType.ZAHLTAG;
            System.out.println("💸 Feld " + targetField.getIndex() + " ist ein Zahltag!");
        }

        payoutService.handlePayoutAfterMovement(playerName);


        // MoveMessage mit neuem Feld und möglichen nächsten Feldern zurückgeben
        return new MoveMessage(
            playerName, 
            targetField.getIndex(),
                reportedType,
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
