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
}
