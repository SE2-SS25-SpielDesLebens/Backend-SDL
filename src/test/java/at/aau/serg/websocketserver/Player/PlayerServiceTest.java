package at.aau.serg.websocketserver.Player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PlayerServiceTest {

    private PlayerService service;

    @BeforeEach
    void setUp() {
        service = PlayerService.getInstance();
        service.addPlayer("Player1");
        service.addPlayer("Player2");
        service.addPlayer("Player3");
    }

    @Test
    void shouldAddNewPlayerSuccessfully() {
        service.addPlayer("Player3");

        Player player = service.getPlayerById("Player3");
        assertNotNull(player);
        assertEquals("Player3", player.getId());
    }

    @Test
    void shouldUpdatePlayerSuccessfully() {
        Player original = service.getPlayerById("Player1");
        original.setMoney(50000);

        Player updatedPlayer = service.getPlayerById("Player1");
        assertEquals(50000, updatedPlayer.getMoney());
    }

    @Test
    void updatePlayer_ShouldReturnFalseIfIdNotFound() {
        Player p = new Player("NonExisting");
        assertFalse(service.updatePlayer("WrongId", p));
    }

    @Test
    void addChildToPlayer_ShouldIncreaseChildrenCount() {
        Player p = service.getPlayerById("Player1");
        p.setChildrenCount(2);
        service.updatePlayer("Player1", p);

        boolean success = service.addChildToPlayer("Player1");
        assertTrue(success);
        assertEquals(3, service.getPlayerById("Player1").getChildren());
    }

    @Test
    void addChildToPlayer_ShouldThrowIfPlayerNotFound() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                service.addChildToPlayer("UnknownPlayer"));
        assertTrue(ex.getMessage().contains("nicht gefunden"));
    }

    @Test
    void addChildToPlayer_ShouldThrowIfTooManyChildren() {
        Player p = service.getPlayerById("Player1");
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

        Player p = service.getPlayerById("Player1");
        assertTrue(p.isMarried());
    }

    @Test
    void marryPlayer_ShouldThrowIfAlreadyMarried() {
        Player p = service.getPlayerById("Player1");
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
        Player p = service.getPlayerById("Player1");
        p.setMoney(25000);
        service.updatePlayer("Player1", p);

        boolean success = service.investForPlayer("Player1");
        assertTrue(success);

        Player updated = service.getPlayerById("Player1");
        assertEquals(5000, updated.getMoney());
        assertEquals(20000, updated.getInvestments());
    }

    @Test
    void investForPlayer_ShouldThrowIfInsufficientFunds() {
        Player p = service.getPlayerById("Player1");
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
