package at.aau.serg.websocketserver.session.board;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Erweiterte Tests für den BoardService mit komplexeren Spielsituationen.
 */
public class BoardServiceComplexTest {

    private BoardDataProvider mockBoardDataProvider;
    private BoardService boardService;
    private List<Field> mockFields;
    private Map<String, Integer> initialPlayerPositions;

    @BeforeEach
    public void setUp() {
        mockBoardDataProvider = mock(BoardDataProvider.class);
        initialPlayerPositions = new HashMap<>();
        
        // Erstelle ein komplexeres Board für Tests mit Verzweigungen
        mockFields = Arrays.asList(
            new Field(1, 0.1, 0.1, Arrays.asList(2, 10), FieldType.STARTNORMAL),
            new Field(2, 0.2, 0.2, Collections.singletonList(3), FieldType.ZAHLTAG),
            new Field(3, 0.3, 0.3, Collections.singletonList(4), FieldType.AKTION),
            new Field(4, 0.4, 0.4, Collections.singletonList(5), FieldType.FREUND),
            new Field(5, 0.5, 0.5, Collections.singletonList(1), FieldType.HEIRAT),
            
            // Zweiter Pfad vom Startfeld
            new Field(10, 0.6, 0.1, Collections.singletonList(11), FieldType.HAUS),
            new Field(11, 0.7, 0.2, Collections.singletonList(12), FieldType.ZAHLTAG),
            new Field(12, 0.8, 0.3, Collections.singletonList(5), FieldType.AKTION)
        );

        // Konfiguriere den Mock
        when(mockBoardDataProvider.getBoard()).thenReturn(mockFields);
        for (int i = 0; i < mockFields.size(); i++) {
            Field field = mockFields.get(i);
            when(mockBoardDataProvider.getFieldByIndex(field.getIndex())).thenReturn(field);
        }

        // Erstelle den BoardService mit dem Mock und vordefiniertem Zustand
        initialPlayerPositions.put("player1", 1); // Spieler 1 auf Feld 1
        initialPlayerPositions.put("player2", 3); // Spieler 2 auf Feld 3
        boardService = new BoardService(mockBoardDataProvider, initialPlayerPositions);
    }

    @Test
    public void testGetMoveOptionsWithBranch() {
        // Ein Spieler auf dem Startfeld sollte bei einem Schritt zwei Optionen haben
        List<Integer> options = boardService.getMoveOptions("player1", 1);
        assertEquals(2, options.size(), "Es sollten zwei Bewegungsoptionen vorhanden sein");
        assertTrue(options.contains(2), "Option 1 sollte Feld 2 sein");
        assertTrue(options.contains(10), "Option 2 sollte Feld 10 sein");
    }

    @Test
    public void testGetMoveOptionsWithMultipleSteps() {
        // Bei zwei Schritten sollten wir auf Feld 3 und Feld 11 landen können
        List<Integer> options = boardService.getMoveOptions("player1", 2);
        assertEquals(2, options.size(), "Nach zwei Schritten sollten zwei Optionen verfügbar sein");
        assertTrue(options.contains(3), "Eine Option sollte Feld 3 sein");
        assertTrue(options.contains(11), "Eine Option sollte Feld 11 sein");
    }

    @Test
    public void testIsPlayerOnField() {
        assertTrue(boardService.isPlayerOnField("player1", 1), "Spieler 1 sollte auf Feld 1 sein");
        assertTrue(boardService.isPlayerOnField("player2", 3), "Spieler 2 sollte auf Feld 3 sein");
        assertFalse(boardService.isPlayerOnField("player1", 2), "Spieler 1 sollte nicht auf Feld 2 sein");
    }

    @Test
    public void testGetPlayersOnField() {
        List<String> playersOnField1 = boardService.getPlayersOnField(1);
        assertEquals(1, playersOnField1.size(), "Es sollte ein Spieler auf Feld 1 sein");
        assertEquals("player1", playersOnField1.get(0), "Spieler 1 sollte auf Feld 1 sein");
        
        List<String> playersOnField3 = boardService.getPlayersOnField(3);
        assertEquals(1, playersOnField3.size(), "Es sollte ein Spieler auf Feld 3 sein");
        assertEquals("player2", playersOnField3.get(0), "Spieler 2 sollte auf Feld 3 sein");
        
        List<String> playersOnField2 = boardService.getPlayersOnField(2);
        assertTrue(playersOnField2.isEmpty(), "Es sollte kein Spieler auf Feld 2 sein");
    }

    @Test
    public void testMovePlayerToBranch() {
        // Spieler 1 kann sich zu Feld 2 oder Feld 10 bewegen
        assertTrue(boardService.movePlayerToField("player1", 2), "Bewegung zu Feld 2 sollte erfolgreich sein");
        assertEquals(2, boardService.getPlayerPosition("player1"), "Spieler sollte jetzt auf Feld 2 sein");
        
        // Zurück zum Start setzen
        boardService.setPlayerPosition("player1", 1);
        
        // Bewegung zur alternativen Route
        assertTrue(boardService.movePlayerToField("player1", 10), "Bewegung zu Feld 10 sollte erfolgreich sein");
        assertEquals(10, boardService.getPlayerPosition("player1"), "Spieler sollte jetzt auf Feld 10 sein");
    }

    @Test
    public void testGetAllPlayerPositions() {
        Map<String, Integer> positions = boardService.getAllPlayerPositions();
        assertEquals(2, positions.size(), "Es sollten zwei Spielerpositionen vorhanden sein");
        assertEquals(1, positions.get("player1").intValue(), "Spieler 1 sollte auf Feld 1 sein");
        assertEquals(3, positions.get("player2").intValue(), "Spieler 2 sollte auf Feld 3 sein");
    }

    @Test
    public void testResetAllPlayerPositions() {
        boardService.resetAllPlayerPositions();
        Map<String, Integer> positions = boardService.getAllPlayerPositions();
        assertTrue(positions.isEmpty(), "Nach dem Reset sollten keine Spielerpositionen vorhanden sein");
    }

    @Test
    public void testIsAnyPlayerOnField() {
        assertTrue(boardService.isAnyPlayerOnField(1), "Es sollte ein Spieler auf Feld 1 sein");
        assertTrue(boardService.isAnyPlayerOnField(3), "Es sollte ein Spieler auf Feld 3 sein");
        assertFalse(boardService.isAnyPlayerOnField(2), "Es sollte kein Spieler auf Feld 2 sein");
    }

    @Test
    public void testGetPlayerField() {
        Field field = boardService.getPlayerField("player1");
        assertNotNull(field, "Das Feld des Spielers sollte nicht null sein");
        assertEquals(1, field.getIndex(), "Spieler 1 sollte auf Feld 1 sein");
        
        Field field2 = boardService.getPlayerField("player2");
        assertNotNull(field2, "Das Feld des Spielers sollte nicht null sein");
        assertEquals(3, field2.getIndex(), "Spieler 2 sollte auf Feld 3 sein");
        
        // Überprüfen der Überladung mit Integer-ID
        Field field3 = boardService.getPlayerField(Integer.valueOf(1));
        assertNotNull(field3, "Das Feld des Spielers sollte bei Integer-ID nicht null sein");
        assertEquals(1, field3.getIndex(), "Bei Integer-ID 1 sollte das Feld-Index 1 sein");
    }
}
