package at.aau.serg.websocketserver.messaging.dtos;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StompMessageTest {

    @Test
    void testAllArgsConstructor() {
        StompMessage message = new StompMessage("Alice", "zieht Karte", "Hallo!", "game-42");

        assertEquals("Alice", message.getPlayerName());
        assertEquals("zieht Karte", message.getAction());
        assertEquals("Hallo!", message.getMessageText());
        assertEquals("game-42", message.getGameId());
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        StompMessage message = new StompMessage();

        message.setPlayerName("Bob");
        message.setAction("geht 3 Felder");
        message.setMessageText("Wie geht's?");
        message.setGameId("game-99");

        assertEquals("Bob", message.getPlayerName());
        assertEquals("geht 3 Felder", message.getAction());
        assertEquals("Wie geht's?", message.getMessageText());
        assertEquals("game-99", message.getGameId());
    }
}
