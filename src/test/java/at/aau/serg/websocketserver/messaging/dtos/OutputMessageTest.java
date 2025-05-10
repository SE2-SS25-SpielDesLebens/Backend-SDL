package at.aau.serg.websocketserver.messaging.dtos;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class OutputMessageTest {

    @Test
    public void testConstructorAndGetters() {
        OutputMessage message = new OutputMessage("Alice", "Hallo!", "2025-04-06T10:15:30");

        assertEquals("Alice", message.getPlayerName());
        assertEquals("Hallo!", message.getContent());
        assertEquals("2025-04-06T10:15:30", message.getTimestamp());
    }

    @Test
    public void testSetters() {
        OutputMessage message = new OutputMessage(null, null, null);

        message.setPlayerName("Bob");
        message.setContent("Testnachricht");
        message.setTimestamp("2025-04-06T12:00:00");

        assertEquals("Bob", message.getPlayerName());
        assertEquals("Testnachricht", message.getContent());
        assertEquals("2025-04-06T12:00:00", message.getTimestamp());
    }

    @Test
    public void testNullValues() {
        // Act
        OutputMessage message = new OutputMessage(null, null, null);
        
        // Assert
        assertNull(message.getPlayerName(), "PlayerName sollte null sein");
        assertNull(message.getContent(), "Content sollte null sein");
        assertNull(message.getTimestamp(), "Timestamp sollte null sein");
    }

    @Test
    public void testEmptyValues() {
        // Arrange
        String emptyString = "";
        
        // Act
        OutputMessage message = new OutputMessage(emptyString, emptyString, emptyString);
        
        // Assert
        assertEquals(emptyString, message.getPlayerName(), "PlayerName sollte leer sein");
        assertEquals(emptyString, message.getContent(), "Content sollte leer sein");
        assertEquals(emptyString, message.getTimestamp(), "Timestamp sollte leer sein");
    }
}
