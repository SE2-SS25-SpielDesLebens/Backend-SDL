package at.aau.serg.websocketserver.board;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testklasse für den BoardService, die zeigt, wie man mit Mockito-Mocks testen kann.
 */
public class BoardServiceTest {

    @Mock
    private BoardDataProvider mockBoardDataProvider;

    private BoardService boardService;
    private List<Field> mockFields;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Erstelle ein vereinfachtes Board für Tests
        mockFields = Arrays.asList(
            new Field(1, 0.1, 0.1, Collections.singletonList(2), FieldType.STARTNORMAL),
            new Field(2, 0.2, 0.2, Collections.singletonList(3), FieldType.ZAHLTAG),
            new Field(3, 0.3, 0.3, Collections.singletonList(1), FieldType.AKTION)
        );

        // Konfiguriere den Mock
        when(mockBoardDataProvider.getBoard()).thenReturn(mockFields);
        when(mockBoardDataProvider.getFieldByIndex(1)).thenReturn(mockFields.get(0));
        when(mockBoardDataProvider.getFieldByIndex(2)).thenReturn(mockFields.get(1));
        when(mockBoardDataProvider.getFieldByIndex(3)).thenReturn(mockFields.get(2));

        // Erstelle den BoardService mit dem Mock
        boardService = new BoardService(mockBoardDataProvider);
    }

    @Test
    public void testAddPlayer() {
        // Arrangement
        String playerId = "player1";
        int startFieldIndex = 1;

        // Action
        boardService.addPlayer(playerId, startFieldIndex);

        // Assertion
        assertEquals(1, boardService.getPlayerPosition(playerId));
    }

    @Test
    public void testMovePlayer() {
        // Arrangement
        String playerId = "player1";
        boardService.addPlayer(playerId, 1);

        // Action
        boardService.movePlayer(playerId, 2);

        // Assertion
        assertEquals(3, boardService.getPlayerPosition(playerId), 
                    "Nach 2 Schritten vom Feld 1 sollte der Spieler auf Feld 3 sein");
    }

    @Test
    public void testGetMoveOptions() {
        // Arrangement
        String playerId = "player1";
        boardService.addPlayer(playerId, 1);

        // Action
        List<Integer> options = boardService.getMoveOptions(playerId, 1);

        // Assertion
        assertEquals(1, options.size(), "Es sollte genau eine Option geben");
        assertEquals(2, options.get(0).intValue(), "Die Option sollte Feld 2 sein");
    }

    @Test
    public void testMovePlayerToField() {
        // Arrangement
        String playerId = "player1";
        boardService.addPlayer(playerId, 1);

        // Action
        boolean result = boardService.movePlayerToField(playerId, 2);

        // Assertion
        assertTrue(result, "Die Bewegung zum nächsten Feld sollte erfolgreich sein");
        assertEquals(2, boardService.getPlayerPosition(playerId));
    }

    @Test
    public void testMovePlayerToInvalidField() {
        // Arrangement
        String playerId = "player1";
        boardService.addPlayer(playerId, 1);

        // Action
        boolean result = boardService.movePlayerToField(playerId, 3); // Feld 3 ist nicht direkt von Feld 1 erreichbar

        // Assertion
        assertFalse(result, "Die Bewegung zu einem nicht erreichbaren Feld sollte fehlschlagen");
        assertEquals(1, boardService.getPlayerPosition(playerId), "Position sollte unverändert bleiben");
    }
}
