package at.aau.serg.websocketserver.websocket.broker;

import at.aau.serg.websocketserver.messaging.dtos.StompMessage;
import at.aau.serg.websocketserver.player.Player;
import at.aau.serg.websocketserver.player.PlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ColorHandlerTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private ColorHandler colorHandler;

    private PlayerService playerService;

    @BeforeEach
    void setUp() {
        playerService = PlayerService.getInstance();
        playerService.clearAll(); // Clear all players before each test
    }

    @Test
    void handleColorSelection_validColor_shouldSetColorAndBroadcast() {
        // Arrange
        String playerId = "player1";
        String color = "RED";
        playerService.createPlayerIfNotExists(playerId);

        StompMessage message = new StompMessage();
        message.setPlayerName(playerId);
        message.setAction("color:" + color);

        // Act
        StompMessage response = colorHandler.handleColorSelection(message);

        // Assert
        assertNotNull(response);
        assertEquals(playerId, response.getPlayerName());
        assertEquals("color_confirmed:" + color, response.getAction());

        // Verify player color was set
        Player player = playerService.getPlayerById(playerId);
        assertEquals(color, player.getCarColor());

        // Verify broadcast was sent
        verify(messagingTemplate).convertAndSend(eq("/topic/player/colors"), any(StompMessage.class));
    }

    @Test
    void handleColorSelection_invalidFormat_shouldReturnError() {
        // Arrange
        String playerId = "player1";
        StompMessage message = new StompMessage();
        message.setPlayerName(playerId);
        message.setAction("invalid_color_format");

        // Act
        StompMessage response = colorHandler.handleColorSelection(message);

        // Assert
        assertNotNull(response);
        assertEquals(playerId, response.getPlayerName());
        assertEquals("error:invalid_color_format", response.getAction());
        verifyNoInteractions(messagingTemplate);
    }

    @Test
    void handleColorSelection_playerNotFound_shouldReturnError() {
        // Arrange
        String playerId = "nonExistentPlayer";
        StompMessage message = new StompMessage();
        message.setPlayerName(playerId);
        message.setAction("color:BLUE");

        // Act
        StompMessage response = colorHandler.handleColorSelection(message);

        // Assert
        assertNotNull(response);
        assertEquals(playerId, response.getPlayerName());
        assertEquals("error:player_not_found", response.getAction());
        verifyNoInteractions(messagingTemplate);
    }

    @Test
    void handleColorSelection_differentValidColor_shouldWork() {
        // Arrange
        String playerId = "player1";
        String color = "GREEN";
        playerService.createPlayerIfNotExists(playerId);

        StompMessage message = new StompMessage();
        message.setPlayerName(playerId);
        message.setAction("color:" + color);

        // Act
        StompMessage response = colorHandler.handleColorSelection(message);

        // Assert
        assertNotNull(response);
        assertEquals("color_confirmed:" + color, response.getAction());
        assertEquals(color, playerService.getPlayerById(playerId).getCarColor());
        verify(messagingTemplate).convertAndSend(eq("/topic/player/colors"), any(StompMessage.class));
    }

    @Test
    void handleColorSelection_allColorVariants_shouldBeAccepted() {
        // Test all valid color options
        String[] colors = {"RED", "BLUE", "GREEN", "YELLOW"};
        String playerId = "player1";
        playerService.createPlayerIfNotExists(playerId);

        for (String color : colors) {
            StompMessage message = new StompMessage();
            message.setPlayerName(playerId);
            message.setAction("color:" + color);

            StompMessage response = colorHandler.handleColorSelection(message);

            assertEquals("color_confirmed:" + color, response.getAction());
            assertEquals(color, playerService.getPlayerById(playerId).getCarColor());
            playerService.getPlayerById(playerId).setCarColor(null); // Reset for next test
        }

        verify(messagingTemplate, times(colors.length)).convertAndSend(anyString(), any(StompMessage.class));
    }
}