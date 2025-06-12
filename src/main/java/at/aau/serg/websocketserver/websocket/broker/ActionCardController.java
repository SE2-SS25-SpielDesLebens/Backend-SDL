package at.aau.serg.websocketserver.websocket.broker;

import at.aau.serg.websocketserver.lobby.Lobby;
import at.aau.serg.websocketserver.lobby.LobbyService;
import at.aau.serg.websocketserver.player.Player;
import at.aau.serg.websocketserver.player.PlayerService;
import at.aau.serg.websocketserver.session.actioncard.ActionCard;
import at.aau.serg.websocketserver.session.actioncard.ActionCardDeck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;

/**
 * Controller to handle drawing of action cards over WebSocket.
 */
@Controller
public class ActionCardController {

    private final IdentityHashMap<String, ActionCardDeck> decks;
    /**
     * Key for decks HashMap is lobbyId
     */

    private final IdentityHashMap<String, ActionCard> pulledCards;
    /**
     * Key for pulledCards HashMap is lobbyId + playerId
     */

    private final SimpMessagingTemplate messagingTemplate;

    private final PlayerService playerService;
    private final LobbyService lobbyService;

    @Autowired
    public ActionCardController(ActionCardDeck deck,
                                SimpMessagingTemplate messagingTemplate) {
        this.decks = new IdentityHashMap<>();
        this.pulledCards = new IdentityHashMap<>();
        this.messagingTemplate = messagingTemplate;

        this.playerService = PlayerService.getInstance();
        this.lobbyService = LobbyService.getInstance();
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
        //Does lobby exist? If so, does a deck for this game exist? If no, add one.
        if(!lobbyService.isLobbyRegistered(lobbyId)) return;
        if(!decks.containsKey(lobbyId)) decks.put(lobbyId, new ActionCardDeck());

        //Does player exist in this lobby?
        Lobby lobby = lobbyService.getLobby(lobbyId);
        List<Player> players = lobby.getPlayers();
        if(players.stream().noneMatch(player -> Objects.equals(player.getId(), playerId))) return;

        //TODO: Is it player's turn?

        //Get deck for this lobby
        ActionCardDeck deck = decks.get(lobbyId);

        // Pull next card from the deck
        ActionCard card = deck.pull();
        pulledCards.put(lobbyId + playerId, card);

        // Send the card back to the specific player topic
        String destination = String.format("/topic/card/%s/%s", lobbyId, playerId);
        messagingTemplate.convertAndSend(destination, card);
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
        //Does lobby exist?
        if(!lobbyService.isLobbyRegistered(lobbyId)) return;

        //Does player exist?
        if(!playerService.isPlayerRegistered(playerId)) return;

        //Does player exist in this lobby?
        Lobby lobby = lobbyService.getLobby(lobbyId);
        List<Player> players = lobby.getPlayers();
        if(players.stream().noneMatch(player -> Objects.equals(player.getId(), playerId))) return;

        //TODO: Is it player's turn?

        //Is a card currently pulled in this game?
        if(pulledCards.containsKey(lobbyId + playerId)) return;
        ActionCard actionCard = pulledCards.get(lobbyId + playerId);

        //TODO: Play pulled card
    }
}
