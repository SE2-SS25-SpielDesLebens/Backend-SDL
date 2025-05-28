package at.aau.serg.websocketserver.websocket.broker;

import at.aau.serg.websocketserver.board.BoardService;
import at.aau.serg.websocketserver.board.Field;
import at.aau.serg.websocketserver.board.FieldType;
import at.aau.serg.websocketserver.messaging.dtos.BoardDataMessage;
import at.aau.serg.websocketserver.session.JobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;


import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WebSocketBrokerControllerTest {
    
    @Mock
    private JobService jobService;
    
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    
    @Mock
    private BoardService boardService;
    
    private WebSocketBrokerController webSocketBrokerController;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        webSocketBrokerController = new WebSocketBrokerController(
                jobService,
                messagingTemplate,
                boardService
        );
    }
    
    @Test
    void testHandleBoardDataRequest() {
        // Mock BoardService
        List<Field> mockFields = Arrays.asList(
            new Field(1, 0.1, 0.2, Collections.singletonList(2), FieldType.STARTNORMAL),
            new Field(2, 0.3, 0.4, Collections.singletonList(3), FieldType.AKTION)
        );
        when(boardService.getBoard()).thenReturn(mockFields);
        
        // Call the method being tested
        webSocketBrokerController.handleBoardDataRequest();
        
        // Capture the message that was sent
        ArgumentCaptor<BoardDataMessage> messageCaptor = ArgumentCaptor.forClass(BoardDataMessage.class);
        verify(messagingTemplate).convertAndSend(
            eq("/topic/board/data"),
            messageCaptor.capture()
        );
        
        // Verify the message content
        BoardDataMessage capturedMessage = messageCaptor.getValue();
        assertNotNull(capturedMessage);
        assertEquals(mockFields, capturedMessage.getFields());
        assertNotNull(capturedMessage.getTimestamp());
    }
}