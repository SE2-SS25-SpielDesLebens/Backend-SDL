package at.aau.serg.websocketserver.messaging.dtos;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StompMessageTest {

    @Test
    void testAllArgsConstructor() {
        StompMessage message = new StompMessage("Alice", "zieht Karte", "Hallo!");

        assertEquals("Alice", message.getPlayerName());
        assertEquals("zieht Karte", message.getAction());
        assertEquals("Hallo!", message.getMessageText());
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        StompMessage message = new StompMessage();

        message.setPlayerName("Bob");
        message.setAction("geht 3 Felder");
        message.setMessageText("Wie geht's?");

        assertEquals("Bob", message.getPlayerName());
        assertEquals("geht 3 Felder", message.getAction());
        assertEquals("Wie geht's?", message.getMessageText());
    }
}
