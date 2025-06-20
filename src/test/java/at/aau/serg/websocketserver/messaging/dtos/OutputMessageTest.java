package at.aau.serg.websocketserver.messaging.dtos;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

 class OutputMessageTest {

    @Test
     void testConstructorAndGetters() {
        OutputMessage message = new OutputMessage("Alice", "Hallo!", "2025-04-06T10:15:30");

        assertEquals("Alice", message.getPlayerName());
        assertEquals("Hallo!", message.getContent());
        assertEquals("2025-04-06T10:15:30", message.getTimestamp());
    }

    @Test
     void testSetters() {
        OutputMessage message = new OutputMessage(null, null, null);

        message.setPlayerName("Bob");
        message.setContent("Testnachricht");
        message.setTimestamp("2025-04-06T12:00:00");

        assertEquals("Bob", message.getPlayerName());
        assertEquals("Testnachricht", message.getContent());
        assertEquals("2025-04-06T12:00:00", message.getTimestamp());
    }
}
