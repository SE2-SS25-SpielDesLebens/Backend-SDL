package at.aau.serg.websocketserver.messaging.dtos;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HouseBuyElseSellMessageTest {

    @Test
    void testGettersAndSetters() {
        HouseBuyElseSellMessage message = new HouseBuyElseSellMessage();

        message.setPlayerID("player123");
        message.setGameId(42);
        message.setBuyElseSell(true);

        assertEquals("player123", message.getPlayerID());
        assertEquals(42, message.getGameId());
        assertTrue(message.isBuyElseSell());

        // Test der redundanten Methode
        assertTrue(message.buyElseSell());
    }
}
