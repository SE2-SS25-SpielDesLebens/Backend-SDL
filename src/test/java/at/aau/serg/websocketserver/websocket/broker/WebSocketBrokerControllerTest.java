package at.aau.serg.websocketserver.websocket.broker;

import at.aau.serg.websocketserver.messaging.dtos.OutputMessage;
import at.aau.serg.websocketserver.messaging.dtos.StompMessage;
import at.aau.serg.websocketserver.session.JobService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class WebSocketBrokerControllerTest {

    @Mock
    private JobService jobService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private WebSocketBrokerController controller;

    @Before
    public void setUp() {
        // hier injizieren wir die beiden Mocks in den Controller
        controller = new WebSocketBrokerController(jobService, messagingTemplate);
    }

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
