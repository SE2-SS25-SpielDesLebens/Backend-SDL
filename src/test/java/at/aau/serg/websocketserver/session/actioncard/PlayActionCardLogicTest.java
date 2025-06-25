package at.aau.serg.websocketserver.session.actioncard;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import at.aau.serg.websocketserver.lobby.Lobby;
import at.aau.serg.websocketserver.lobby.LobbyService;
import at.aau.serg.websocketserver.player.Player;
import at.aau.serg.websocketserver.player.PlayerService;
import at.aau.serg.websocketserver.session.actioncard.ActionCard;
import at.aau.serg.websocketserver.session.actioncard.ActionCardDeck;
import at.aau.serg.websocketserver.session.actioncard.ActionCardService;
import at.aau.serg.websocketserver.session.actioncard.PlayActionCardLogic;
import at.aau.serg.websocketserver.session.actioncard.ActionCardServiceException;
import at.aau.serg.websocketserver.game.GameLogic;

import java.security.SecureRandom;
import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class PlayActionCardLogicTest {

    @Mock
    private LobbyService lobbyService;

    @Mock
    private PlayerService playerService;

    @Mock
    private Lobby lobby;

    private PlayActionCardLogic logic;
    private Player alice;
    private Player bob;
    private MockedStatic<LobbyService> lobbyServiceStatic;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        // Mock static getInstance
        lobbyServiceStatic = Mockito.mockStatic(LobbyService.class);
        lobbyServiceStatic.when(LobbyService::getInstance).thenReturn(lobbyService);
        // Stub lobby and playerService lookup
        when(lobbyService.getLobby("lobby1")).thenReturn(lobby);
        when(lobbyService.getPlayerService()).thenReturn(playerService);

        logic = new PlayActionCardLogic();

        // Create players
        alice = mock(Player.class);
        when(alice.getId()).thenReturn("alice");
        when(alice.getMoney()).thenReturn(100000);
        bob = mock(Player.class);
        when(bob.getId()).thenReturn("bob");
        when(bob.getMoney()).thenReturn(50000);
        // Stub lobby players
        when(lobby.getPlayers()).thenReturn(Arrays.asList(alice, bob));
        // Stub playerService.getPlayerById
        when(playerService.getPlayerById("alice")).thenReturn(alice);
        when(playerService.getPlayerById("bob")).thenReturn(bob);
    }

    @AfterEach
    public void tearDown() {
        lobbyServiceStatic.close();
    }

    @Test
    public void testTransferMoney_bankToPlayer() {
        ActionCard card = new ActionCard(
                27,
                "receive_ancestry_research",
                "Du betreibst Ahnenforschung!",
                "Du findest einen reichen Verwandten!",
                "ancestry_research",
                new String[]{"Du erhältst 50.000 von der Bank."}
        );
        logic.playActionCard(card, "lobby1", "alice", null);
        verify(alice).addMoney(50000);
    }

    @Test
    public void testTransferMoney_playerPaysOther() {
        ActionCard card = new ActionCard(
                4,
                "decision_festival_tickets",
                "Du gewinnst Freikarten für ein Festival!",
                "Wähle EINE Aktion aus:",
                "festival_tickets",
                new String[]{
                        "A. Gehe hin und lerne deinen neuen besten Freund kennen. Stecke einen Stift in dein Auto.",
                        "B. Verkaufe die Tickets und kassiere von einem anderen Spieler 40.000."
                }
        );
        logic.playActionCard(card, "lobby1", "alice", "B");
        verify(bob).removeMoney(40000);
        verify(alice).addMoney(40000);
    }

    @Test
    public void testSpinColorOutcome_odd() {
        setRandomSpin(1); // odd
        ActionCard card = new ActionCard(
                17,
                "spin_biography_launch",
                "Du veröffentlichst deine Lebensgeschichte!",
                "Drehe die Scheibe und erfahre, ob sie toppt oder floppt:",
                "biography_launch",
                new String[]{
                        "Rot – Du erhältst 30.000 von der Bank.",
                        "Schwarz – Zahle 30.000 an die Bank."
                }
        );
        logic.playActionCard(card, "lobby1", "alice", null);
        verify(alice).addMoney(30000);
    }

    @Test
    public void testSpinColorOutcome_even() {
        setRandomSpin(2); // even
        ActionCard card = new ActionCard(
                17,
                "spin_biography_launch",
                "Du veröffentlichst deine Lebensgeschichte!",
                "Drehe die Scheibe und erfahre, ob sie toppt oder floppt:",
                "biography_launch",
                new String[]{
                        "Rot – Du erhältst 30.000 von der Bank.",
                        "Schwarz – Zahle 30.000 an die Bank."
                }
        );
        logic.playActionCard(card, "lobby1", "alice", null);
        verify(alice).removeMoney(30000);
    }

    private void setRandomSpin(int spinValue) {
        try {
            SecureRandom rnd = new SecureRandom() {
                @Override
                public int nextInt(int bound) {
                    return spinValue - 1; // yield desired spin
                }
            };
            var randField = PlayActionCardLogic.class.getDeclaredField("random");
            randField.setAccessible(true);
            randField.set(logic, rnd);
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }
}