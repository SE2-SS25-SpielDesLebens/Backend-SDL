package at.aau.serg.websocketserver.player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerServiceTest {

    private PlayerService service;

    @BeforeEach
    void setUp() {
        service = PlayerService.getInstance();
        service.clearAll(); // wichtig für saubere Tests
        service.addPlayer("Player1");
        service.addPlayer("Player2");
    }

    @Test
    void shouldAddNewPlayerSuccessfully() {
        Player player = service.addPlayer("Player3");
        assertNotNull(player);
        assertEquals("Player3", player.getId());
    }

    @Test
    void shouldReturnExistingPlayer() {
        Player player1 = service.addPlayer("Player1");
        Player same = service.getPlayerById("Player1");
        assertEquals(player1, same);
    }

    @Test
    void shouldRegisterOnlyOnce() {
        service.addPlayer("Player1");
        assertEquals(2, service.getPlayers().size());
    }


    @Test
    void removePlayer_ShouldDeleteSuccessfully() {
        service.removePlayer("Player1");
        assertNull(service.getPlayerById("Player1"));
    }

    @Test
    void clearAll_ShouldEmptyMap() {
        service.clearAll();
        assertTrue(service.getPlayers().isEmpty());
    }

    @Test
    void isPlayerRegistered_ShouldReturnCorrectValue() {
        assertTrue(service.isPlayerRegistered("Player1"));
        assertFalse(service.isPlayerRegistered("Unknown"));
    }
}
