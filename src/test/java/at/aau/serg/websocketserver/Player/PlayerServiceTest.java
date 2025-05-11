package at.aau.serg.websocketserver.Player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerServiceTest {

    private PlayerService service;

    @BeforeEach
    public void setup() {
        service = new PlayerService();
    }

    @Test
    public void testAddPlayerIncreasesListSize() {
        int initialSize = service.getAllPlayers().size();

        Player newPlayer = new Player("Lena", 0, 5000, 0, 2000, 0, "Master", "Single", "Ärztin", 0, 0, 9);
        service.addPlayer(newPlayer);

        assertEquals(initialSize + 1, service.getAllPlayers().size());
        assertTrue(service.getAllPlayers().stream().anyMatch(p -> p.getName().equals("Lena")));
    }

    @Test
    public void testUpdatePlayerSuccess() {
        Player original = service.getAllPlayers().get(0);
        Player updated = new Player(original.getName(), original.getId(), 20000, 1000, 3000, 1, "PhD", "Verheiratet", "Manager", 2, 2, 9);

        boolean result = service.updatePlayer(original.getId(), updated);
        assertTrue(result);

        Player resultPlayer = service.getPlayerById(original.getId()).orElseThrow();
        assertEquals(20000, resultPlayer.getMoney());
        assertEquals("Verheiratet", resultPlayer.getRelationship());
    }

    @Test
    public void testUpdatePlayer_invalidId_returnsFalse() {
        Player fake = new Player("Fake", 9999, 1000, 0, 0, 0, "None", "Single", "None", 0, 0, 1);
        boolean result = service.updatePlayer(9999, fake);
        assertFalse(result);
    }

    @Test
    public void testMarryPlayer() {
        Player player = service.getAllPlayers().get(0);
        assertEquals("Single", player.getRelationship());

        service.marryPlayer(player.getId());
        Player updated = service.getPlayerById(player.getId()).orElseThrow();
        assertEquals("Verheiratet", updated.getRelationship());
    }

    @Test
    public void testAddChildToPlayer() {
        Player player = service.getAllPlayers().get(0);
        int oldChildren = player.getChildren();

        service.addChildToPlayer(player.getId());
        Player updated = service.getPlayerById(player.getId()).orElseThrow();
        assertEquals(oldChildren + 1, updated.getChildren());
    }

    @Test
    public void testInvestForPlayer() {
        Player player = service.getAllPlayers().get(1); //15000

        assertThrows(IllegalArgumentException.class, () -> {
            service.investForPlayer(player.getId());
        });
    }

    @Test
    public void testInvestForPlayer2(){
        Player player = service.getAllPlayers().get(0);
    }

    @Test
    public void testInvestTooLittleMoney_throwsException() {
        Player lowMoneyPlayer = new Player("Tom", 99, 1000, 0, 0, 0, "Bachelor", "Single", "Pilot", 0, 0, 8);
        service.addPlayer(lowMoneyPlayer);

        assertThrows(IllegalArgumentException.class, () -> {
            service.investForPlayer(99);
        });
    }

    @Test
    public void testGetAllPlayersReturnsDefaultList() {
        var players = service.getAllPlayers();
        assertEquals(2, players.size());
        assertEquals("Hans", players.get(0).getName());
        assertEquals("Eva", players.get(1).getName());
    }

    @Test
    public void testGetPlayerById_validId_returnsPlayer() {
        Optional<Player> player = service.getPlayerById(1);
        assertTrue(player.isPresent());
        assertEquals("Hans", player.get().getName());
    }

    @Test
    public void testGetPlayerById_invalidId_returnsEmpty() {
        Optional<Player> player = service.getPlayerById(9999);
        assertTrue(player.isEmpty());
    }

}
