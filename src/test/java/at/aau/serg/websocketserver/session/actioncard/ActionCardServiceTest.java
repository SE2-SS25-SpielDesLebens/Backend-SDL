package at.aau.serg.websocketserver.session.actioncard;

import at.aau.serg.websocketserver.lobby.LobbyService;
import at.aau.serg.websocketserver.player.PlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ActionCardServiceTest {
    @Mock private LobbyService lobbyService;
    @Mock private PlayerService playerService;

    private ActionCardService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        service = ActionCardService.getInstance();
        // inject mocks via reflection or setters if available
    }

    @Test
    void testDrawAndPlayLifecycle() throws Exception {
        // assume lobby and player registered
        String lobbyId = "L1";
        String playerId = "P1";
        when(lobbyService.isLobbyRegistered(lobbyId)).thenReturn(true);
        when(playerService.isPlayerRegistered(playerId)).thenReturn(true);
        // implement minimal stubbing for draw
        ActionCard card = service.drawCard(lobbyId, playerId);
        assertNotNull(card);
        assertEquals(card, service.getPulledCard(lobbyId, playerId));

        // playing the card does not throw
        assertDoesNotThrow(() -> service.playCard(lobbyId, playerId, "A"));
    }
}
