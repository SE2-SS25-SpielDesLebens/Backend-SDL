package at.aau.serg.websocketserver.websocket.broker;

import at.aau.serg.websocketserver.player.Player;
import at.aau.serg.websocketserver.player.PlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlayerControllerTest {

    private PlayerController playerController;
    private PlayerService playerService;

    @BeforeEach
    void setUp() {
        playerService = PlayerService.getInstance();
        playerService.clearAll();
        playerController = new PlayerController();
    }

    @Test
    void getAllPlayers_shouldReturnNoContentWhenEmpty() {
        ResponseEntity<List<Player>> response = playerController.getAllPlayers();
        assertEquals(204, response.getStatusCodeValue());
    }

    @Test
    void getAllPlayers_shouldReturnPlayersWhenExist() {
        playerService.createPlayerIfNotExists("player1");
        playerService.createPlayerIfNotExists("player2");

        ResponseEntity<List<Player>> response = playerController.getAllPlayers();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void createPlayer_shouldCreateNewPlayer() {
        Player request = new Player("newPlayer");

        ResponseEntity<Player> response = playerController.createPlayer(request);

        assertEquals(201, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("newPlayer", response.getBody().getId());
    }

    @Test
    void getPlayerById_shouldReturnNotFoundForUnknownPlayer() {
        ResponseEntity<Player> response = playerController.getPlayerById("unknown");
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void getPlayerById_shouldReturnPlayerWhenExists() {
        playerService.createPlayerIfNotExists("player1");

        ResponseEntity<Player> response = playerController.getPlayerById("player1");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("player1", response.getBody().getId());
    }

    @Test
    void deletePlayer_shouldReturnNotFoundForUnknownPlayer() {
        ResponseEntity<Void> response = playerController.deletePlayer("unknown");
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void deletePlayer_shouldRemovePlayer() {
        playerService.createPlayerIfNotExists("player1");

        ResponseEntity<Void> response = playerController.deletePlayer("player1");

        assertEquals(204, response.getStatusCodeValue());
        assertFalse(playerService.isPlayerRegistered("player1"));
    }

    @Test
    void addChild_shouldReturnNotFoundForUnknownPlayer() {
        ResponseEntity<String> response = playerController.addChild("unknown");
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void addChild_shouldAddChildWhenPossible() {
        Player player = playerService.createPlayerIfNotExists("player1");
        player.setCarColor("RED"); // Auto vorhanden

        ResponseEntity<String> response = playerController.addChild("player1");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, player.getChildren());
    }

    @Test
    void marryPlayer_shouldReturnNotFoundForUnknownPlayer() {
        ResponseEntity<String> response = playerController.marryPlayer("unknown");
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void marryPlayer_shouldMarryWhenSingle() {
        Player player = playerService.createPlayerIfNotExists("player1");

        ResponseEntity<String> response = playerController.marryPlayer("player1");

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(player.getRelationship());
    }

    @Test
    void marryPlayer_shouldReturnBadRequestWhenAlreadyMarried() {
        Player player = playerService.createPlayerIfNotExists("player1");
        player.marry();

        ResponseEntity<String> response = playerController.marryPlayer("player1");

        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("bereits verheiratet"));
    }

    @Test
    void invest_shouldReturnNotFoundForUnknownPlayer() {
        ResponseEntity<String> response = playerController.invest("unknown");
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void invest_shouldInvestWhenNotInvestedBefore() {
        Player player = playerService.createPlayerIfNotExists("player1");
        player.addMoney(50000); // Ausreichend Geld

        ResponseEntity<String> response = playerController.invest("player1");

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(player.getInvestments() > 0);
    }

    @Test
    void triggerEvent_shouldHandleValidEvent() {
        Player player = playerService.createPlayerIfNotExists("player1");
        player.setCarColor("BLUE"); // Auto für Kinder notwendig

        ResponseEntity<String> response = playerController.triggerEvent("player1", "kind");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, player.getChildren());
    }

    @Test
    void triggerEvent_shouldReturnBadRequestForInvalidEvent() {
        playerService.createPlayerIfNotExists("player1");

        ResponseEntity<String> response = playerController.triggerEvent("player1", "invalid_event");

        assertEquals(400, response.getStatusCodeValue());
    }
}