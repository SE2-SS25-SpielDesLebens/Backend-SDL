package at.aau.serg.websocketserver.websocket.broker;

import at.aau.serg.websocketserver.messaging.dtos.OutputMessage;
import at.aau.serg.websocketserver.messaging.dtos.StompMessage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WebSocketBrokerControllerTest {

    private final WebSocketBrokerController controller = new WebSocketBrokerController();

    @Test
    public void testHandleMove() {
        StompMessage input = new StompMessage();
        input.setPlayerName("Alice");
        input.setAction("Würfelt eine 5");

        OutputMessage output = controller.handleMove(input);

        assertEquals("Alice", output.getPlayerName());
        assertEquals("Würfelt eine 5", output.getContent());
        assertNotNull(output.getTimestamp());
    }

    @Test
    public void testHandleChat() {
        StompMessage input = new StompMessage();
        input.setPlayerName("Bob");
        input.setMessageText("Hallo zusammen!");

        OutputMessage output = controller.handleChat(input);

        assertEquals("Bob", output.getPlayerName());
        assertEquals("Hallo zusammen!", output.getContent());
        assertNotNull(output.getTimestamp());
    }
    
    @Test
    public void testHandleLobby_CreateLobby() {
        // Arrange
        StompMessage input = new StompMessage();
        input.setPlayerName("Charlie");
        input.setGameId("game123");
        input.setAction("createLobby");
        
        // Act
        OutputMessage output = controller.handleLobby(input);
        
        // Assert
        assertEquals("Charlie", output.getPlayerName());
        assertTrue(output.getContent().contains("Lobby [game123] von Charlie erstellt"));
        assertNotNull(output.getTimestamp());
    }
    
    @Test
    public void testHandleLobby_JoinLobby() {
        // Arrange
        StompMessage input = new StompMessage();
        input.setPlayerName("David");
        input.setGameId("game123");
        input.setAction("joinLobby");
        
        // Act
        OutputMessage output = controller.handleLobby(input);
        
        // Assert
        assertEquals("David", output.getPlayerName());
        assertTrue(output.getContent().contains("David ist Lobby [game123] beigetreten"));
        assertNotNull(output.getTimestamp());
    }
    
    @Test
    public void testHandleLobby_UnknownAction() {
        // Arrange
        StompMessage input = new StompMessage();
        input.setPlayerName("Eva");
        input.setGameId("game123");
        input.setAction("unbekannteAktion");
        
        // Act
        OutputMessage output = controller.handleLobby(input);
        
        // Assert
        assertEquals("Eva", output.getPlayerName());
        assertEquals("Unbekannte Lobby-Aktion.", output.getContent());
        assertNotNull(output.getTimestamp());
    }
    
    @Test
    public void testHandleLobby_NullAction() {
        // Arrange
        StompMessage input = new StompMessage();
        input.setPlayerName("Frank");
        input.setGameId("game123");
        input.setAction(null);
        
        // Act
        OutputMessage output = controller.handleLobby(input);
        
        // Assert
        assertEquals("Frank", output.getPlayerName());
        assertEquals("❌ Keine Aktion angegeben.", output.getContent());
        assertNotNull(output.getTimestamp());
    }
    
    @Test
    public void testHandleMove_NullValues() {
        // Arrange
        StompMessage input = new StompMessage();
        // Keine Werte setzen
        
        // Act
        OutputMessage output = controller.handleMove(input);
        
        // Assert
        assertNull(output.getPlayerName());
        assertNull(output.getContent());
        assertNotNull(output.getTimestamp());
    }
    
    @Test
    public void testHandleChat_NullValues() {
        // Arrange
        StompMessage input = new StompMessage();
        // Keine Werte setzen
        
        // Act
        OutputMessage output = controller.handleChat(input);
        
        // Assert
        assertNull(output.getPlayerName());
        assertNull(output.getContent());
        assertNotNull(output.getTimestamp());
    }
}
