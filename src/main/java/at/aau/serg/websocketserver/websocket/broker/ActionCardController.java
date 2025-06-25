package at.aau.serg.websocketserver.websocket.broker;

import at.aau.serg.websocketserver.session.actioncard.ActionCard;
import at.aau.serg.websocketserver.session.actioncard.ActionCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * Controller to handle drawing of action cards over WebSocket.
 */
@Controller
public class ActionCardController {

    private final SimpMessagingTemplate messagingTemplate;

    private final ActionCardService actionCardService;

    @Autowired
    public ActionCardController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;

        this.actionCardService = ActionCardService.getInstance();
    }

    /**
     * Handles draw card requests. Clients send to /app/drawCard/{gameSession}/{playerName}.
     * The drawn card is sent back to /topic/card/{gameSession}/{playerName}.
     *
     * @param lobbyId the identifier for the game where card is drawn
     * @param playerId the identifier of the player drawing the card
     */
    @MessageMapping("/drawCard/{lobbyId}/{playerId}")
    public void drawCard(@DestinationVariable String lobbyId, @DestinationVariable String playerId) {
        ActionCard actionCard = actionCardService.drawCard(lobbyId, playerId);

        // Send the card back to the specific player topic
        String destination = String.format("/topic/card/%s/%s", lobbyId, playerId);
        messagingTemplate.convertAndSend(destination, actionCard);
    }

    /**
     * Handles draw card requests. Clients send to /app/drawCard/{gameSession}/{playerName}.
     * The drawn card is sent back to /topic/card/{gameSession}/{playerName}.
     *
     * @param lobbyId the identifier for the game where card is drawn
     * @param playerId the identifier of the player drawing the card
     */
    @MessageMapping("/playCard/{lobbyId}/{playerId}/{decision}")
    public void playCard(@DestinationVariable String lobbyId, @DestinationVariable String playerId, @DestinationVariable String decision) {
        actionCardService.playCard(lobbyId, playerId, decision);
    }
}
