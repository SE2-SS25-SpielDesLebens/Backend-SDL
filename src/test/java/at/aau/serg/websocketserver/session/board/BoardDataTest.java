package at.aau.serg.websocketserver.session.board;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testklasse für die BoardData-Implementation des BoardDataProviders.
 */
public class BoardDataTest {

    private BoardData boardData;

    @BeforeEach
    public void setUp() {
        boardData = new BoardData();
    }

    @Test
    public void testGetBoard() {
        List<Field> board = boardData.getBoard();

        assertNotNull(board, "Das Board sollte nicht null sein");
        assertFalse(board.isEmpty(), "Das Board sollte nicht leer sein");
        assertEquals(27, board.size(), "Das Board sollte 27 Felder enthalten");

        assertThrows(UnsupportedOperationException.class, () -> board.add(null),
                "Die zurückgegebene Liste sollte unveränderbar sein");
    }
    
    @Test
    public void testGetFieldByIndex_ValidIndex() {
        // Action
        Field field = boardData.getFieldByIndex(1);
        
        // Assertion
        assertNotNull(field, "Ein vorhandenes Feld sollte gefunden werden");
        assertEquals(1, field.getIndex(), "Das Feld sollte den korrekten Index haben");
        assertEquals(FieldType.STARTNORMAL, field.getType(), "Das Feld sollte den korrekten Typ haben");
    }
    
    @Test
    public void testGetFieldByIndex_InvalidIndex() {
        // Action
        Field field = boardData.getFieldByIndex(999);
        
        // Assertion
        assertNull(field, "Ein nicht existierendes Feld sollte null zurückgeben");
    }
    
    @Test
    public void testStaticCompatibilityMethods() {
        // Action & Assert für statische Methoden, die für Abwärtskompatibilität vorgesehen sind
        List<Field> staticBoard = BoardData.getBoardStatic();
        assertNotNull(staticBoard, "Die statische getBoard-Methode sollte funktionieren");
        assertEquals(boardData.getBoard().size(), staticBoard.size(), "Statische und Instanzmethode sollten das gleiche Board zurückgeben");
        
        Field staticField = BoardData.getFieldByIndexStatic(1);
        assertNotNull(staticField, "Die statische getFieldByIndex-Methode sollte funktionieren");
        assertEquals(boardData.getFieldByIndex(1).getIndex(), staticField.getIndex(), 
                "Statische und Instanzmethode sollten das gleiche Feld zurückgeben");
    }

    @Test
    public void testBoardStructure() {
        Field field1 = boardData.getFieldByIndex(1);
        assertNotNull(field1, "Feld 1 sollte existieren");
        assertTrue(field1.getNextFields().contains(2), "Feld 1 sollte zu Feld 2 führen");

        Field field18 = boardData.getFieldByIndex(18);
        assertNotNull(field18, "Feld 18 sollte existieren");
        assertTrue(field18.getNextFields().contains(20), "Feld 18 sollte zu Feld 20 führen");
    }
}
