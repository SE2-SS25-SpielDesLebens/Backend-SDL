package at.aau.serg.websocketserver.session.actioncard;

import at.aau.serg.websocketserver.game.GameLogic;
import at.aau.serg.websocketserver.lobby.Lobby;
import at.aau.serg.websocketserver.lobby.LobbyService;
import at.aau.serg.websocketserver.player.Player;
import at.aau.serg.websocketserver.player.PlayerService;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;

public class ActionCardService {

    private final IdentityHashMap<String, ActionCardDeck> decks;
    /**
     * Key for decks HashMap is lobbyId
     */

    private final IdentityHashMap<String, ActionCard> pulledCards;
    /**
     * Key for pulledCards HashMap is lobbyId + playerId
     */

    private final PlayerService playerService;
    private final LobbyService lobbyService;

    private final PlayActionCardLogic playActionCardLogic;

    private static final ActionCardService actionCardService = new ActionCardService();

    private ActionCardService () {
        this.decks = new IdentityHashMap<>();
        this.pulledCards = new IdentityHashMap<>();

        this.playerService = PlayerService.getInstance();
        this.lobbyService = LobbyService.getInstance();

        this.playActionCardLogic = new PlayActionCardLogic();
    }

    public static ActionCardService getInstance() {
        return actionCardService;
    }

    /**
     * Handles draw card requests. Clients send to /app/drawCard/{gameSession}/{playerName}.
     * The drawn card is sent back to /topic/card/{gameSession}/{playerName}.
     *
     * @param lobbyId the identifier for the game where card is drawn
     * @param playerId the identifier of the player drawing the card
     */
    public ActionCard drawCard(String lobbyId, String playerId) throws ActionCardServiceException {
        verifyPlayerTurnInLobby(lobbyId, playerId);

        //If lobby exists, does a deck for this lobby exist? If no, add one.
        if(!decks.containsKey(lobbyId)) decks.put(lobbyId, new ActionCardDeck());

        //Get deck for this lobby
        ActionCardDeck deck = decks.get(lobbyId);

        // Pull next card from the deck
        ActionCard card = deck.pull();
        pulledCards.put(lobbyId + playerId, card);

        return card;
    }

    /**
     * Handles draw card requests. Clients send to /app/drawCard/{gameSession}/{playerName}.
     * The drawn card is sent back to /topic/card/{gameSession}/{playerName}.
     *
     * @param lobbyId the identifier for the game where card is drawn
     * @param playerId the identifier of the player drawing the card
     */
    public void playCard(String lobbyId, String playerId, String decision) throws ActionCardServiceException {
        verifyPlayerTurnInLobby(lobbyId, playerId);

        //Is a card currently pulled in this game?
        if(pulledCards.containsKey(lobbyId + playerId)) return;
        ActionCard actionCard = pulledCards.get(lobbyId + playerId);

        this.playActionCardLogic.playActionCard(actionCard, lobbyId, playerId, decision);
    }

    private void verifyPlayerTurnInLobby (String lobbyId, String playerId) throws ActionCardServiceException {
        //Does lobby exist?
        if(!lobbyService.isLobbyRegistered(lobbyId)) {
            throw new ActionCardServiceException("The lobby ID: " + lobbyId + " does not exist!");
        }

        //Does player exist?
        if(!playerService.isPlayerRegistered(playerId)) {
            throw new ActionCardServiceException("The player ID: " + playerId + " does not exist!");
        }

        //Does player exist in this lobby?
        Lobby lobby = lobbyService.getLobby(lobbyId);
        List<Player> players = lobby.getPlayers();
        if(players.stream().noneMatch(player -> Objects.equals(player.getId(), playerId))) {
            throw new ActionCardServiceException("The player ID: " + playerId + " does not exist in lobby ID: " + lobbyId + "!");
        }

        //Is it player's turn?
        GameLogic gameLogic = lobby.getGameLogic();
        if(!Integer.toString(gameLogic.getCurrentPlayerIndex()).equals(playerId)) {
            throw new ActionCardServiceException("It's not the turn of the player ID: " + playerId + "!");
        }
    }
}
