package at.aau.serg.websocketserver.websocket.broker;

import at.aau.serg.websocketserver.actioncard.ActionCard;
import at.aau.serg.websocketserver.actioncard.ActionCardDeck;
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

    private final ActionCardDeck deck;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public ActionCardController(ActionCardDeck deck,
                                SimpMessagingTemplate messagingTemplate) {
        this.deck = deck;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Handles draw card requests. Clients send to /app/drawCard/{playerName}.
     * The drawn card is sent back to /topic/card/{playerName}.
     *
     * @param playerName the identifier of the player drawing the card
     */
    @MessageMapping("/drawCard/{playerName}")
    public void drawCard(@DestinationVariable String playerName) {
        // Pull next card from the deck
        ActionCard card = deck.pull();

        // Send the card back to the specific player topic
        String destination = String.format("/topic/card/%s", playerName);
        messagingTemplate.convertAndSend(destination, card);
    }
}
