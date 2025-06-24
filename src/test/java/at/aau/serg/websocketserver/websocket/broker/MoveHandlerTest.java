package at.aau.serg.websocketserver.websocket.broker;

import at.aau.serg.websocketserver.messaging.dtos.MoveMessage;
import at.aau.serg.websocketserver.messaging.dtos.StompMessage;
import at.aau.serg.websocketserver.session.board.BoardService;
import at.aau.serg.websocketserver.session.board.Field;
import at.aau.serg.websocketserver.session.board.FieldType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

 class MoveHandlerTest {

    @Mock
    private BoardService boardService;

    @InjectMocks
    private MoveHandler moveHandler;
    @BeforeEach
     void setUp() {
        // Verwende Standard-Mocking statt Inline-Mocking
        System.setProperty("mockito.mock.serializable", "false");
        System.setProperty("mockito.mockmaker", "mock-maker-default");
        MockitoAnnotations.openMocks(this);
    }

    @Test
     void testHandleMove_ValidDiceRoll_UpdatesPosition() {
        // Arrange
        String playerName = "testPlayer";
        int diceRoll = 3;
        int currentFieldIndex = 5;
        int targetFieldIndex = 8;

        // Vorbereitung eines gültigen Spielfelds mit einer Liste von möglichen nächsten Feldern
        List<Integer> nextFields = Arrays.asList(6, 7, 8);
        Field currentField = new Field(currentFieldIndex, 100.0, 100.0, nextFields, FieldType.AKTION);
        Field targetField = new Field(targetFieldIndex, 200.0, 200.0, Arrays.asList(9, 10), FieldType.ZAHLTAG);

        // Mock-Antworten konfigurieren
        when(boardService.getFieldByIndex(currentFieldIndex)).thenReturn(currentField);
        when(boardService.getFieldByIndex(targetFieldIndex)).thenReturn(targetField);

        // Der StompMessage erstellen, die "X gewürfelt:Y" enthält
        StompMessage stompMessage = new StompMessage();
        stompMessage.setPlayerName(playerName);
        stompMessage.setAction(diceRoll + " gewürfelt:" + currentFieldIndex);

        // Act
        MoveMessage result = moveHandler.handleMove(stompMessage);

        // Assert
        assertNotNull(result);
        assertEquals(playerName, result.getPlayerName());
        assertEquals(targetFieldIndex, result.getIndex());
        assertEquals(FieldType.ZAHLTAG, result.getType());
        assertEquals(Arrays.asList(9, 10), result.getNextPossibleFields());

        // Überprüfen, ob die Spielerposition aktualisiert wurde
        verify(boardService).updatePlayerPosition(playerName, targetFieldIndex);
    }

    @Test
     void testHandleMove_InvalidFormat_ReturnsFallback() {
        // Arrange
        String playerName = "testPlayer";

        // Ungültiges Format der Aktion
        StompMessage stompMessage = new StompMessage();
        stompMessage.setPlayerName(playerName);
        stompMessage.setAction("ungültiges Format");

        // Act
        MoveMessage result = moveHandler.handleMove(stompMessage);

        // Assert
        assertNotNull(result);
        assertEquals(playerName, result.getPlayerName());
        assertEquals(0, result.getIndex());
        assertEquals(FieldType.AKTION, result.getType());

        // Keine Aktualisierung der Spielerposition
        verify(boardService, never()).updatePlayerPosition(anyString(), anyInt());
    }

    @Test
     void testHandleMove_NoCurrentFieldIndex_UsesDefault() {
        // Arrange
        String playerName = "testPlayer";
        int diceRoll = 3;
        int defaultFieldIndex = 1;
        int targetFieldIndex = 4;

        // Format ohne aktuellen Feldindex
        StompMessage stompMessage = new StompMessage();
        stompMessage.setPlayerName(playerName);
        stompMessage.setAction(diceRoll + " gewürfelt");

        // Mock-Antworten konfigurieren
        Field defaultField = new Field(defaultFieldIndex, 0.0, 0.0, Arrays.asList(2, 3, 4), FieldType.STARTNORMAL);
        Field targetField = new Field(targetFieldIndex, 100.0, 100.0, Arrays.asList(5, 6), FieldType.AKTION);

        when(boardService.getFieldByIndex(0)).thenReturn(null);  // Der Standardwert 0 existiert nicht
        when(boardService.getFieldByIndex(defaultFieldIndex)).thenReturn(defaultField);
        when(boardService.getFieldByIndex(targetFieldIndex)).thenReturn(targetField);

        // Act
        MoveMessage result = moveHandler.handleMove(stompMessage);

        // Assert
        assertNotNull(result);
        assertEquals(playerName, result.getPlayerName());
        assertEquals(targetFieldIndex, result.getIndex());
        assertEquals(FieldType.AKTION, result.getType());

        // Überprüfen, ob die Spielerposition aktualisiert wurde
        verify(boardService).updatePlayerPosition(eq(playerName), eq(targetFieldIndex));
    }

    @Test
     void testWalkSteps_WithinNextFieldsSize() {
        // Arrange
        int startFieldIndex = 1;
        int steps = 2;
        int targetFieldIndex = 3;

        // Vorbereitung der Testdaten
        List<Integer> nextFields = Arrays.asList(2, 3, 4);
        Field startField = new Field(startFieldIndex, 0.0, 0.0, nextFields, FieldType.STARTNORMAL);
        Field targetField = new Field(targetFieldIndex, 50.0, 50.0, Arrays.asList(5, 6), FieldType.AKTION);

        when(boardService.getFieldByIndex(startFieldIndex)).thenReturn(startField);
        when(boardService.getFieldByIndex(targetFieldIndex)).thenReturn(targetField);

        // StompMessage erstellen
        StompMessage stompMessage = new StompMessage();
        stompMessage.setPlayerName("testPlayer");
        stompMessage.setAction(steps + " gewürfelt:" + startFieldIndex);

        // Act
        MoveMessage result = moveHandler.handleMove(stompMessage);

        // Assert
        assertEquals(targetFieldIndex, result.getIndex());
        assertEquals(FieldType.AKTION, result.getType());
    }

    @Test
     void testWalkSteps_StepsGreaterThanNextFieldsSize() {
        // Arrange
        int startFieldIndex = 1;
        int steps = 5;  // Größer als die Anzahl der nextFields
        int targetFieldIndex = 4;  // Letztes Feld in nextFields

        // Vorbereitung der Testdaten
        List<Integer> nextFields = Arrays.asList(2, 3, 4);
        Field startField = new Field(startFieldIndex, 0.0, 0.0, nextFields, FieldType.STARTNORMAL);
        Field targetField = new Field(targetFieldIndex, 50.0, 50.0, Arrays.asList(5, 6), FieldType.AKTION);

        when(boardService.getFieldByIndex(startFieldIndex)).thenReturn(startField);
        when(boardService.getFieldByIndex(targetFieldIndex)).thenReturn(targetField);

        // StompMessage erstellen
        StompMessage stompMessage = new StompMessage();
        stompMessage.setPlayerName("testPlayer");
        stompMessage.setAction(steps + " gewürfelt:" + startFieldIndex);

        // Act
        MoveMessage result = moveHandler.handleMove(stompMessage);

        // Assert
        assertEquals(targetFieldIndex, result.getIndex());
        assertEquals(FieldType.AKTION, result.getType());
    }

    @Test
     void testWalkSteps_EmptyNextFields() {
        // Arrange
        int fieldIndex = 10;
        int steps = 3;

        // Feld ohne nächste Felder
        Field field = new Field(fieldIndex, 100.0, 100.0, Arrays.asList(), FieldType.RUHESTAND);

        when(boardService.getFieldByIndex(fieldIndex)).thenReturn(field);

        // StompMessage erstellen
        StompMessage stompMessage = new StompMessage();
        stompMessage.setPlayerName("testPlayer");
        stompMessage.setAction(steps + " gewürfelt:" + fieldIndex);

        // Act
        MoveMessage result = moveHandler.handleMove(stompMessage);

        // Assert
        // Erwarten, dass der Spieler auf demselben Feld bleibt
        assertEquals(fieldIndex, result.getIndex());
        assertEquals(FieldType.RUHESTAND, result.getType());
    }

    @Test
     void testWalkSteps_TargetFieldNotFound() {
        // Arrange
        int startFieldIndex = 1;
        int steps = 2;
        int targetFieldIndex = 3;

        // Vorbereitung der Testdaten
        List<Integer> nextFields = Arrays.asList(2, 3, 4);
        Field startField = new Field(startFieldIndex, 0.0, 0.0, nextFields, FieldType.STARTNORMAL);

        when(boardService.getFieldByIndex(startFieldIndex)).thenReturn(startField);
        when(boardService.getFieldByIndex(targetFieldIndex)).thenReturn(null);  // Zielfeld existiert nicht

        // StompMessage erstellen
        StompMessage stompMessage = new StompMessage();
        stompMessage.setPlayerName("testPlayer");
        stompMessage.setAction(steps + " gewürfelt:" + startFieldIndex);

        // Act
        MoveMessage result = moveHandler.handleMove(stompMessage);

        // Assert
        // Erwarten, dass der Spieler auf demselben Feld bleibt
        assertEquals(startFieldIndex, result.getIndex());
        assertEquals(FieldType.STARTNORMAL, result.getType());
    }
}
