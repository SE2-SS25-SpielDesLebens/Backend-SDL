package at.aau.serg.websocketserver.messaging.dtos;

import org.junit.Test;

import static org.junit.Assert.*;
public class LobbyUpdateMessageTest {
    @Test
    public void testAllArgsConstructor() {
        LobbyUpdateMessage message = new LobbyUpdateMessage(
                "Alice", "Bob", "Charlie", "Dana", true
        );

        assertEquals("Alice", message.getPlayer1());
        assertEquals("Bob", message.getPlayer2());
        assertEquals("Charlie", message.getPlayer3());
        assertEquals("Dana", message.getPlayer4());
        assertTrue(message.isStarted());
    }

    @Test
    public void testNoArgsConstructorAndSetters() {
        LobbyUpdateMessage message = new LobbyUpdateMessage();
        message.setPlayer1("A");
        message.setPlayer2("B");
        message.setPlayer3("C");
        message.setPlayer4("D");
        message.setStarted(false);

        assertEquals("A", message.getPlayer1());
        assertEquals("B", message.getPlayer2());
        assertEquals("C", message.getPlayer3());
        assertEquals("D", message.getPlayer4());
        assertFalse(message.isStarted());
    }

    @Test
    public void testEqualsAndHashCode() {
        LobbyUpdateMessage m1 = new LobbyUpdateMessage("A", "B", "C", "D", true);
        LobbyUpdateMessage m2 = new LobbyUpdateMessage("A", "B", "C", "D", true);

        assertEquals(m1, m2);
        assertEquals(m1.hashCode(), m2.hashCode());
    }

    @Test
    public void testToString() {
        LobbyUpdateMessage message = new LobbyUpdateMessage("P1", "P2", "P3", "P4", true);
        String str = message.toString();

        assertTrue(str.contains("P1"));
        assertTrue(str.contains("P2"));
        assertTrue(str.contains("P3"));
        assertTrue(str.contains("P4"));
        assertTrue(str.contains("true"));
    }
}
