package at.aau.serg.websocketserver.Player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PlayerServiceTest {

    private PlayerService service;

    @BeforeEach
    void setUp() {
        service = new PlayerService();
    }

    @Test
    void shouldInitializeWithTwoPlayers() {
        assertEquals(2, service.getAllPlayers().size());
    }

    @Test
    void shouldAddNewPlayerSuccessfully() {
        service.addPlayer("Player3");

        Optional<Player> result = service.getPlayerById("Player3");
        assertTrue(result.isPresent());
        assertEquals("Player3", result.get().getId());
    }

    @Test
    void shouldUpdatePlayerSuccessfully() {
        Player original = service.getPlayerById("Player1").orElseThrow();
        original.setMoney(50000);

        boolean updated = service.updatePlayer("Player1", original);
        assertTrue(updated);

        Player updatedPlayer = service.getPlayerById("Player1").orElseThrow();
        assertEquals(50000, updatedPlayer.getMoney());
    }

    @Test
    void updatePlayer_ShouldReturnFalseIfIdNotFound() {
        Player p = new Player("NonExisting");
        assertFalse(service.updatePlayer("WrongId", p));
    }

    @Test
    void addChildToPlayer_ShouldIncreaseChildrenCount() {
        Player p = service.getPlayerById("Player1").orElseThrow();
        p.setChildrenCount(2);
        service.updatePlayer("Player1", p);

        boolean success = service.addChildToPlayer("Player1");
        assertTrue(success);
        assertEquals(3, service.getPlayerById("Player1").orElseThrow().getChildren());
    }

    @Test
    void addChildToPlayer_ShouldThrowIfPlayerNotFound() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                service.addChildToPlayer("UnknownPlayer"));
        assertTrue(ex.getMessage().contains("nicht gefunden"));
    }

    @Test
    void addChildToPlayer_ShouldThrowIfTooManyChildren() {
        Player p = service.getPlayerById("Player1").orElseThrow();
        p.setChildrenCount(4);
        service.updatePlayer("Player1", p);

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                service.addChildToPlayer("Player1"));
        assertTrue(ex.getMessage().contains("maximal 4 Kinder"));
    }

    @Test
    void marryPlayer_ShouldSetMarriedFlag() {
        boolean married = service.marryPlayer("Player1");
        assertTrue(married);

        Player p = service.getPlayerById("Player1").orElseThrow();
        assertTrue(p.isMarried());
    }

    @Test
    void marryPlayer_ShouldThrowIfAlreadyMarried() {
        Player p = service.getPlayerById("Player1").orElseThrow();
        p.setMarried(true);
        service.updatePlayer("Player1", p);

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                service.marryPlayer("Player1"));
        assertTrue(ex.getMessage().contains("bereits verheiratet"));
    }

    @Test
    void marryPlayer_ShouldThrowIfPlayerNotFound() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                service.marryPlayer("Unknown"));
        assertTrue(ex.getMessage().contains("nicht gefunden"));
    }

    @Test
    void investForPlayer_ShouldSubtractMoneyAndAddInvestment() {
        Player p = service.getPlayerById("Player1").orElseThrow();
        p.setMoney(25000);
        service.updatePlayer("Player1", p);

        boolean success = service.investForPlayer("Player1");
        assertTrue(success);

        Player updated = service.getPlayerById("Player1").orElseThrow();
        assertEquals(5000, updated.getMoney());
        assertEquals(20000, updated.getInvestments());
    }

    @Test
    void investForPlayer_ShouldThrowIfInsufficientFunds() {
        Player p = service.getPlayerById("Player1").orElseThrow();
        p.setMoney(10000);
        service.updatePlayer("Player1", p);

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                service.investForPlayer("Player1"));
        assertTrue(ex.getMessage().contains("Nicht genug Geld"));
    }

    @Test
    void investForPlayer_ShouldThrowIfPlayerMissing() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                service.investForPlayer("NichtDa"));
        assertTrue(ex.getMessage().contains("Spieler nicht gefunden"));
    }
}
