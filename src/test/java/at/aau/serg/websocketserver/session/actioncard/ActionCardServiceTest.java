package at.aau.serg.websocketserver.session.actioncard;

import at.aau.serg.websocketserver.game.GameLogic;
import at.aau.serg.websocketserver.lobby.Lobby;
import at.aau.serg.websocketserver.lobby.LobbyService;
import at.aau.serg.websocketserver.player.Player;
import at.aau.serg.websocketserver.player.PlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ActionCardServiceTest {

    private ActionCardService service;

    @Mock private PlayerService mockPlayerService;
    @Mock private LobbyService mockLobbyService;
    @Mock private PlayActionCardLogic mockPlayLogic;

    // now use HashMap so that containsKey() uses equals()
    private Map<String, ActionCardDeck> decks   = new HashMap<>();
    private Map<String, ActionCard>   pulled  = new HashMap<>();

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        service = ActionCardService.getInstance();

        injectField("playerService",     mockPlayerService);
        injectField("lobbyService",      mockLobbyService);
        injectField("playActionCardLogic", mockPlayLogic);
        injectField("decks",             decks);
        injectField("pulledCards",       pulled);
    }

    private void injectField(String name, Object value) throws Exception {
        Field f = ActionCardService.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(service, value);
    }

    @Test
    void drawCard_success() throws Exception {
        String lobbyId = "l1", playerId = "0";
        when(mockLobbyService.isLobbyRegistered(lobbyId)).thenReturn(true);
        when(mockPlayerService.isPlayerRegistered(playerId)).thenReturn(true);

        Lobby mockLobby = mock(Lobby.class);
        GameLogic mockLogic = mock(GameLogic.class);
        when(mockLogic.getCurrentPlayerIndex()).thenReturn(0);
        when(mockLobby.getGameLogic()).thenReturn(mockLogic);

        Player mockPlayer = mock(Player.class);
        when(mockPlayer.getId()).thenReturn(playerId);
        when(mockLobby.getPlayers()).thenReturn(List.of(mockPlayer));
        when(mockLobbyService.getLobby(lobbyId)).thenReturn(mockLobby);

        ActionCard card = service.drawCard(lobbyId, playerId);
        assertNotNull(card, "Should draw a non-null card");
        assertTrue(pulled.containsKey(lobbyId + playerId),
                "Pulled-cards map should record the draw");
    }

    @Test
    void drawCard_lobbyNotRegistered_throws() {
        when(mockLobbyService.isLobbyRegistered("bad")).thenReturn(false);
        when(mockPlayerService.isPlayerRegistered("p")).thenReturn(true);

        ActionCardServiceException ex = assertThrows(
                ActionCardServiceException.class,
                () -> service.drawCard("bad", "p")
        );
        assertEquals("The lobby ID: bad does not exist!", ex.getMessage());
    }

    @Test
    void drawCard_notYourTurn_throws() {
        String lobbyId = "l2", playerId = "1";
        when(mockLobbyService.isLobbyRegistered(lobbyId)).thenReturn(true);
        when(mockPlayerService.isPlayerRegistered(playerId)).thenReturn(true);

        Lobby mockLobby = mock(Lobby.class);
        GameLogic mockLogic = mock(GameLogic.class);
        when(mockLogic.getCurrentPlayerIndex()).thenReturn(0);
        when(mockLobby.getGameLogic()).thenReturn(mockLogic);

        Player p = mock(Player.class);
        when(p.getId()).thenReturn(playerId);
        when(mockLobby.getPlayers()).thenReturn(List.of(p));
        when(mockLobbyService.getLobby(lobbyId)).thenReturn(mockLobby);

        ActionCardServiceException ex = assertThrows(
                ActionCardServiceException.class,
                () -> service.drawCard(lobbyId, playerId)
        );
        assertTrue(ex.getMessage().contains(
                "It's not the turn of the player ID: " + playerId + "!"
        ));
    }

    @Test
    void playCard_success() throws Exception {
        String lobbyId = "l3", playerId = "0", key = lobbyId + playerId;
        when(mockLobbyService.isLobbyRegistered(lobbyId)).thenReturn(true);
        when(mockPlayerService.isPlayerRegistered(playerId)).thenReturn(true);

        Lobby mockLobby = mock(Lobby.class);
        GameLogic mockLogic = mock(GameLogic.class);
        when(mockLogic.getCurrentPlayerIndex()).thenReturn(0);
        when(mockLobby.getGameLogic()).thenReturn(mockLogic);

        Player p = mock(Player.class);
        when(p.getId()).thenReturn(playerId);
        when(mockLobby.getPlayers()).thenReturn(List.of(p));
        when(mockLobbyService.getLobby(lobbyId)).thenReturn(mockLobby);

        // put a pulled card into the map
        ActionCard dummy = new ActionCard(42, "h", "hl", "act", "img", new String[]{"r"});
        pulled.put(key, dummy);

        service.playCard(lobbyId, playerId, "DECIDE");
        verify(mockPlayLogic).playActionCard(dummy, lobbyId, playerId, "DECIDE");
    }

    @Test
    void playCard_noPulledCard_noLogicCall() throws Exception {
        String lobbyId = "l4", playerId = "0";

        when(mockLobbyService.isLobbyRegistered(lobbyId)).thenReturn(true);
        when(mockPlayerService.isPlayerRegistered(playerId)).thenReturn(true);

        Lobby mockLobby = mock(Lobby.class);
        GameLogic mockLogic = mock(GameLogic.class);
        when(mockLogic.getCurrentPlayerIndex()).thenReturn(0);
        when(mockLobby.getGameLogic()).thenReturn(mockLogic);

        Player p = mock(Player.class);
        when(p.getId()).thenReturn(playerId);
        when(mockLobby.getPlayers()).thenReturn(List.of(p));
        when(mockLobbyService.getLobby(lobbyId)).thenReturn(mockLobby);

        // ensure no card stored
        pulled.remove(lobbyId + playerId);

        // should throw when no card has been pulled
        ActionCardServiceException ex = assertThrows(
                ActionCardServiceException.class,
                () -> service.playCard(lobbyId, playerId, "X")
        );
        assertTrue(ex.getMessage().contains(
                "There is no card currently played for Lobby ID: " + lobbyId + " and Player ID: " + playerId
        ));

        // and no action logic is invoked
        verify(mockPlayLogic, never())
                .playActionCard(any(), anyString(), anyString(), anyString());
    }
}