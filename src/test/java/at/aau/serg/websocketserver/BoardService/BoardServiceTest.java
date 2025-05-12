package at.aau.serg.websocketserver.BoardService;

import at.aau.serg.websocketserver.fieldlogic.BoardService;
import at.aau.serg.websocketserver.fieldlogic.Field;
import at.aau.serg.websocketserver.fieldlogic.FieldType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BoardServiceTest {

    private BoardService boardService;

    @BeforeEach
    void setUp() {
        boardService = new BoardService();
    }

    @Test
    void testAddPlayerStoresPositionCorrectly() {
        boardService.addPlayer("Player1", 1);
        Field field = boardService.getCurrentField("Player1");
        assertEquals(1, field.getIndex());
    }

    @Test
    void testGetCurrentFieldReturnsDefaultIfPlayerNotFound() {
        Field field = boardService.getCurrentField("Unknown");
        assertEquals(0, field.getIndex());
    }

    @Test
    void testMovePlayerLinearPath() {
        boardService.addPlayer("Player1", 0);
        boardService.movePlayer("Player1", 2);
        Field current = boardService.getCurrentField("Player1");
        assertEquals(2, current.getIndex());
        assertEquals(FieldType.ACTION, current.getType());
    }

    @Test
    void testMovePlayerStopsAtDeadEnd() {
        // Field 2 has no next fields
        boardService.addPlayer("Player1", 2);
        boardService.movePlayer("Player1", 1);
        Field current = boardService.getCurrentField("Player1");
        assertEquals(3, current.getIndex()); // still at 2
    }

    @Test
    void testGetCurrentFieldTypeReturnsCorrectType() {
        boardService.addPlayer("Player1", 1);
        FieldType type = boardService.getCurrentFieldType("Player1");
        assertEquals(FieldType.PAYDAY, type);
    }
}
