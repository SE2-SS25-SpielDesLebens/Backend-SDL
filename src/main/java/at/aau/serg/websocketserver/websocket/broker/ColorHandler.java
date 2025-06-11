package at.aau.serg.websocketserver.websocket.broker;

import at.aau.serg.websocketserver.messaging.dtos.StompMessage;
import at.aau.serg.websocketserver.player.Player;
import at.aau.serg.websocketserver.player.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Controller zur Verarbeitung von Farbauswahl-Nachrichten
 */
@Controller
public class ColorHandler {
    private final PlayerService playerService;
    private final SimpMessagingTemplate messagingTemplate;
    private static final Pattern COLOR_PATTERN = Pattern.compile("^color:(BLUE|RED|GREEN|YELLOW)$");
    
    @Autowired
    public ColorHandler(SimpMessagingTemplate messagingTemplate) {
        this.playerService = PlayerService.getInstance();
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Verarbeitet eine Farbauswahl-Nachricht vom Client
     * Format der Nachricht: "color:X" wobei X die Farbe ist (RED, BLUE, GREEN oder YELLOW)
     * 
     * @param message Die empfangene Nachricht
     * @return Die Antwort mit der bestätigten Farbauswahl
     */    @MessageMapping("/player/color")
    @SendTo("/topic/game")
    public StompMessage handleColorSelection(StompMessage message) {
        String playerName = message.getPlayerName();
        String action = message.getAction();
        
        System.out.println("🎨 ColorHandler: Empfange Farbauswahl von " + playerName + ", Aktion: " + action);
          // Überprüfe, ob das Format der Aktion korrekt ist
        Matcher matcher = COLOR_PATTERN.matcher(action);
        if (!matcher.matches()) {            System.out.println("❌ Ungültiges Format für Farbauswahl: " + action);
            // Erstelle eine Fehlermeldung mit StompMessage
            StompMessage errorResponse = new StompMessage();
            errorResponse.setPlayerName(playerName);
            errorResponse.setAction("error:invalid_color_format");
            return errorResponse;
        }
        
        // Extrahiere die Farbe aus der Nachricht
        String color = action.substring(6); // Nach "color:" kommt die Farbe
          // Hole den Spieler (playerName wird als ID verwendet)
        Player player = playerService.getPlayerById(playerName);        if (player == null) {
            System.out.println("❌ Spieler nicht gefunden: " + playerName);
            StompMessage errorResponse = new StompMessage();
            errorResponse.setPlayerName(playerName);
            errorResponse.setAction("error:player_not_found");
            return errorResponse;
        }
        
        // Setze die Farbe für den Spieler
        player.setCarColor(color);
        System.out.println("🎨 Farbe für Spieler " + playerName + " gesetzt: " + color);        // Sende eine Bestätigung an alle Clients
        // Verwende StompMessage statt MoveMessage für Farbänderungen
        StompMessage response = new StompMessage();
        response.setPlayerName(playerName);
        response.setAction("color_selected:" + color);
          // Broadcast der Farbauswahl an alle Clients
        messagingTemplate.convertAndSend("/topic/player/colors", response);
        
        // Eine leere StompMessage als Antwort für /topic/game zur Bestätigung
        StompMessage confirmationResponse = new StompMessage();
        confirmationResponse.setPlayerName(playerName);
        confirmationResponse.setAction("color_confirmed:" + color);
        return confirmationResponse;
    }
}
