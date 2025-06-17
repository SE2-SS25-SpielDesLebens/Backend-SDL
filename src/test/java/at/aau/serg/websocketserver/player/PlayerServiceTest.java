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
    void createPlayerIfNotExists_shouldCreatePlayer() {
        boolean success = service.createPlayerIfNotExists("Anna");
        assertTrue(success);
        assertNotNull(service.getPlayerById("Anna"));
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
    void updatePlayer_shouldFailIfMissing() {
        Player dummy = new Player("Unknown");
        assertFalse(service.updatePlayer("Unknown", dummy));
    }

    @Test
    void isPlayerActive_shouldReflectCorrectState() {
        service.createPlayerIfNotExists("ActiveGuy");
        Player player = service.getPlayerById("ActiveGuy");
        assertFalse(service.isPlayerActive("ActiveGuy"));

        player.setActive(true);
        assertTrue(service.isPlayerActive("ActiveGuy"));
    }
}
