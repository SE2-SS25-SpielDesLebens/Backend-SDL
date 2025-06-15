package at.aau.serg.websocketserver.player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlayerServiceTest {

    private PlayerService service;

    @BeforeEach
    void setUp() {
        service = PlayerService.getInstance();
        service.clearAll(); // Saubere Ausgangslage
    }

    @Test
    void createPlayerIfNotExists_shouldCreateAndReturnPlayer() {
        Player p = service.createPlayerIfNotExists("Anna");
        assertNotNull(p);
        assertEquals("Anna", p.getId());
    }

    @Test
    void createPlayerIfNotExists_shouldNotDuplicate() {
        service.createPlayerIfNotExists("Bob");
        Player same = service.createPlayerIfNotExists("Bob");
        assertEquals(1, service.getAllPlayers().size());
        assertEquals("Bob", same.getId());
    }

    @Test
    void getPlayerById_shouldReturnCorrectPlayer() {
        service.createPlayerIfNotExists("Carl");
        Player p = service.getPlayerById("Carl");
        assertNotNull(p);
        assertEquals("Carl", p.getId());
    }

    @Test
    void getAllPlayers_shouldReturnCorrectList() {
        service.createPlayerIfNotExists("A");
        service.createPlayerIfNotExists("B");
        List<Player> all = service.getAllPlayers();
        assertEquals(2, all.size());
    }

    @Test
    void getRegisteredPlayerCount_shouldBeAccurate() {
        service.createPlayerIfNotExists("X");
        service.createPlayerIfNotExists("Y");
        assertEquals(2, service.getRegisteredPlayerCount());
    }

    @Test
    void removePlayer_shouldWork() {
        service.createPlayerIfNotExists("Z");
        service.removePlayer("Z");
        assertFalse(service.isPlayerRegistered("Z"));
    }

    @Test
    void clearAll_shouldEmptyAllPlayers() {
        service.createPlayerIfNotExists("M");
        service.clearAll();
        assertEquals(0, service.getAllPlayers().size());
    }

    @Test
    void updatePlayer_shouldReplaceExisting() {
        Player original = service.createPlayerIfNotExists("U");
        original.setDegree(true);

        Player newOne = new Player("U");
        newOne.setDegree(false);
        boolean updated = service.updatePlayer("U", newOne);

        assertTrue(updated);
        assertFalse(service.getPlayerById("U").hasDegree());
    }

    @Test
    void updatePlayer_shouldFailIfMissing() {
        Player dummy = new Player("Unknown");
        assertFalse(service.updatePlayer("Unknown", dummy));
    }

    @Test
    void isPlayerActive_shouldReflectCorrectState() {
        Player player = service.createPlayerIfNotExists("ActiveGuy");
        assertFalse(service.isPlayerActive("ActiveGuy"));

        player.setActive(true);
        assertTrue(service.isPlayerActive("ActiveGuy"));
    }

    @Test
    void addMoneyToPlayer_shouldWorkCorrectly() {
        Player p = service.createPlayerIfNotExists("Cashy");
        service.addMoneyToPlayer("Cashy", 1000);
        assertEquals(1000, p.getMoney());
    }

    @Test
    void addMoneyToPlayer_shouldThrowIfPlayerMissing() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                service.addMoneyToPlayer("Nobody", 500));
        assertTrue(ex.getMessage().contains("Spieler nicht gefunden"));
    }

    @Test
    void addMoneyToPlayer_shouldThrowIfNegativeAmount() {
        service.createPlayerIfNotExists("NegGuy");
        assertThrows(IllegalArgumentException.class, () ->
                service.addMoneyToPlayer("NegGuy", -100));
    }

    @Test
    void removeMoneyFromPlayer_shouldWorkCorrectly() {
        Player p = service.createPlayerIfNotExists("PayGuy");
        p.addMoney(1000); // Startguthaben
        service.removeMoneyFromPlayer("PayGuy", 500);
        assertEquals(500, p.getMoney());
    }

    @Test
    void removeMoneyFromPlayer_shouldThrowIfPlayerMissing() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                service.removeMoneyFromPlayer("Ghost", 200));
        assertTrue(ex.getMessage().contains("Spieler nicht gefunden"));
    }

    @Test
    void removeMoneyFromPlayer_shouldThrowIfNegativeAmount() {
        service.createPlayerIfNotExists("MinusGuy");
        assertThrows(IllegalArgumentException.class, () ->
                service.removeMoneyFromPlayer("MinusGuy", -50));
    }
}
