package at.aau.serg.websocketserver.websocket.broker;

import at.aau.serg.websocketserver.messaging.dtos.MoveMessage;
import at.aau.serg.websocketserver.messaging.dtos.StompMessage;
import at.aau.serg.websocketserver.session.board.BoardService;
import at.aau.serg.websocketserver.session.board.Field;
import at.aau.serg.websocketserver.session.board.FieldType;
import at.aau.serg.websocketserver.session.payout.PayoutService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class MoveHandlerTest {

   @Mock
   private BoardService boardService;

   @Mock
   private SimpMessagingTemplate messagingTemplate;

   @Mock
   private PayoutService payoutService;

   @InjectMocks
   private MoveHandler moveHandler;

   @BeforeEach
   void setup() {
      // nötig nur bei manueller Initialisierung, hier durch @InjectMocks automatisch
   }

   @Test
   void handleMove_validDiceMove_returnsCorrectMoveMessage() {
      // Arrange
      StompMessage msg = new StompMessage();
      msg.setPlayerName("Tester");
      msg.setAction("3 gewürfelt:1");

      Field start = new Field(1, 0, 0, Arrays.asList(2, 3, 4), FieldType.STARTNORMAL);
      Field target = new Field(4, 0, 0, Arrays.asList(5, 6), FieldType.ZAHLTAG);

      when(boardService.getFieldByIndex(1)).thenReturn(start);
      when(boardService.getFieldByIndex(4)).thenReturn(target);
      when(payoutService.isActivePaydayField("Tester", 4)).thenReturn(true);

      // Act
      MoveMessage result = moveHandler.handleMove(msg);

      // Assert
      assertNotNull(result);
      assertEquals("Tester", result.getPlayerName());
      assertEquals(4, result.getIndex());
      assertEquals(FieldType.ZAHLTAG, result.getType());
      assertEquals(Arrays.asList(5, 6), result.getNextPossibleFields());

      verify(boardService).updatePlayerPosition("Tester", 4);
      verify(payoutService, times(1)).handlePayoutAfterMovement("Tester");
      verify(messagingTemplate).convertAndSend(Optional.ofNullable(eq("/topic/players/positions")), any());
   }

   @Test
   void handleMove_invalidAction_returnsFallbackMessage() {
      StompMessage msg = new StompMessage();
      msg.setPlayerName("X");
      msg.setAction("ungültig");

      MoveMessage result = moveHandler.handleMove(msg);

      assertNotNull(result);
      assertEquals("X", result.getPlayerName());
      assertEquals(0, result.getIndex());
      assertEquals(FieldType.AKTION, result.getType());

      verify(boardService, never()).updatePlayerPosition(any(), anyInt());
   }
}
