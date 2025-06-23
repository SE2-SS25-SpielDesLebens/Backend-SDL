package at.aau.serg.websocketserver.session.actioncard;

import at.aau.serg.websocketserver.lobby.Lobby;
import at.aau.serg.websocketserver.lobby.LobbyService;
import at.aau.serg.websocketserver.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlayActionCardLogicTest {

    @Mock
    private LobbyService lobbyService;
    @Mock
    private Lobby lobby;
    @Mock
    private Player playerA;
    @Mock
    private Player playerB;

    private PlayActionCardLogic logic;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        // Stub lobby
        when(lobbyService.getLobby("L1")).thenReturn(lobby);
        when(lobby.getPlayers()).thenReturn(Arrays.asList(playerA, playerB));
        when(playerA.getId()).thenReturn("A");
        when(playerB.getId()).thenReturn("B");
        when(playerA.getMoney()).thenReturn(100);
        when(playerB.getMoney()).thenReturn(50);
        // Create logic and inject LobbyService
        logic = new PlayActionCardLogic();
        logic.setLobbyService(lobbyService);
        // Use deterministic random via setter
        logic.setRandomSupplier(bound -> 1);
    }

    @Test
    void testDecisionCollectFromEachPlayer() {
        ActionCard card = new ActionCard(1, "decision_animals_enrich", "", "", "", new String[0]);
        logic.playActionCard(card, "L1", "A", "B");
        // Expect playerB pays 20
        verify(playerB).removeMoney(20);
        verify(playerA).addMoney(20);
    }

    @Test
    void testSpinColorOutcome_Red() {
        // spin=1 (odd)
        ActionCard card = new ActionCard(2, "spin_biography_launch", "", "", "", new String[0]);
        logic.playActionCard(card, "L1", "A", null);
        verify(playerA).addMoney(30_000);
    }

    @Test
    void testSpinColorOutcome_Black() {
        // even spin
        logic.setRandomSupplier(bound -> 2);
        ActionCard card = new ActionCard(2, "spin_biography_launch", "", "", "", new String[0]);
        logic.playActionCard(card, "L1", "A", null);
        verify(playerA).removeMoney(100);
    }
}
