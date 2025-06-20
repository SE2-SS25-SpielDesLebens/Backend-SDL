package at.aau.serg.websocketserver.messaging.dtos;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

 class LobbyResponseMessageTest {
    @Test
    void testAllArgsConstructorAndGetters() {
        LobbyResponseMessage message = new LobbyResponseMessage("lobby123", "Alice", true, "Success");

        Assertions.assertEquals("lobby123", message.getLobbyID());
        Assertions.assertEquals("Alice", message.getPlayerName());
        Assertions.assertTrue(message.isSuccessful());
        Assertions.assertEquals("Success", message.getMessage());
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        LobbyResponseMessage message = new LobbyResponseMessage();

        message.setLobbyID("lobby456");
        message.setPlayerName("Bob");
        message.setSuccessful(false);
        message.setMessage("Failed");

        Assertions.assertEquals("lobby456", message.getLobbyID());
        Assertions.assertEquals("Bob", message.getPlayerName());
        Assertions.assertFalse(message.isSuccessful());
        Assertions.assertEquals("Failed", message.getMessage());
    }

    @Test
    void testEqualsAndHashCode() {
        LobbyResponseMessage msg1 = new LobbyResponseMessage("lobby789", "Charlie", true, "Ok");
        LobbyResponseMessage msg2 = new LobbyResponseMessage("lobby789", "Charlie", true, "Ok");

        Assertions.assertEquals(msg1, msg2);
        Assertions.assertEquals(msg1.hashCode(), msg2.hashCode());
    }

    @Test
    void testToString() {
        LobbyResponseMessage message = new LobbyResponseMessage("lobby999", "Dana", false, "Error");

        String expected = "LobbyResponseMessage(lobbyID=lobby999, playerName=Dana, isSuccessful=false, message=Error)";
        Assertions.assertEquals(expected, message.toString());
    }
}
