package at.aau.serg.websocketserver.websocket.broker;

import at.aau.serg.websocketserver.session.actioncard.ActionCard;
import at.aau.serg.websocketserver.session.actioncard.ActionCardDeck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.IdentityHashMap;

/**
 * Controller to handle drawing of action cards over WebSocket.
 */
@Controller
public class ActionCardController {

    private final IdentityHashMap<String, ActionCardDeck> decks;
    private final IdentityHashMap<String, ActionCard> pulledCards;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public ActionCardController(ActionCardDeck deck,
                                SimpMessagingTemplate messagingTemplate) {
        this.decks = new IdentityHashMap<>();
        this.pulledCards = new IdentityHashMap<>();
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Handles draw card requests. Clients send to /app/drawCard/{gameSession}/{playerName}.
     * The drawn card is sent back to /topic/card/{gameSession}/{playerName}.
     *
     * @param gameSession the identifier for the game where card is drawn
     * @param playerName the identifier of the player drawing the card
     */
    @MessageMapping("/drawCard/{gameSession}/{playerName}")
    public void drawCard(@DestinationVariable String gameSession, @DestinationVariable String playerName) {
        //TODO: Does session exist? If so, does a deck for this game exist? If no, add one.

        //TODO: Does player exist in this game?

        //TODO: Is it player's turn?

        ActionCardDeck deck = decks.get(gameSession);

        // Pull next card from the deck
        ActionCard card = deck.pull();

        // Send the card back to the specific player topic
        String destination = String.format("/topic/card/%s/%s", gameSession, playerName);
        messagingTemplate.convertAndSend(destination, card);
    }

    @MessageMapping("/playCard/{gameSession}/{playerName}")
    public void playCard(@DestinationVariable String gameSession, @DestinationVariable String playerName) {
        //TODO: Does session exist?

        //TODO: Does player exist in this game?

        //TODO: Is it player's turn?

        //TODO: Is a card currently pulled in this game?
        
    }
}
