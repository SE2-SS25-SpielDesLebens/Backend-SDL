package at.aau.serg.websocketserver.messaging.dtos;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LobbyRequestMessageTest {
    @Test
    void testAllArgsConstructorAndGetter() {
        LobbyRequestMessage message = new LobbyRequestMessage("Alice");
        Assertions.assertEquals("Alice", message.getPlayerName());
    }

    @Test
    void testNoArgsConstructorAndSetter() {
        LobbyRequestMessage message = new LobbyRequestMessage();
        message.setPlayerName("Bob");
        Assertions.assertEquals("Bob", message.getPlayerName());
    }

    @Test
    void testEqualsAndHashCode() {
        LobbyRequestMessage msg1 = new LobbyRequestMessage("Charlie");
        LobbyRequestMessage msg2 = new LobbyRequestMessage("Charlie");

        Assertions.assertEquals(msg1, msg2);
        Assertions.assertEquals(msg1.hashCode(), msg2.hashCode());
    }

    @Test
    void testToString() {
        LobbyRequestMessage message = new LobbyRequestMessage("Dana");
        String expected = "LobbyRequestMessage(playerName=Dana)";
        Assertions.assertEquals(expected, message.toString());
    }
}
