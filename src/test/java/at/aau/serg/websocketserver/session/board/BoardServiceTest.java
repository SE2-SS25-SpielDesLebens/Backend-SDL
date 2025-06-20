package at.aau.serg.websocketserver.session.board;

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
 class BoardServiceTest {

    @Mock
    private BoardDataProvider mockBoardDataProvider;

    private BoardService boardService;
    private List<Field> mockFields;

    @BeforeEach
     void setUp() {
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
     void testAddPlayer() {
        String playerId = "player1";
        int startFieldIndex = 1;

        boardService.addPlayer(playerId, startFieldIndex);

        assertEquals(1, boardService.getPlayerPosition(playerId));
    }

    @Test
    public void testMovePlayer() {
        String playerId = "player1";
        boardService.addPlayer(playerId, 1);

        boardService.movePlayer(playerId, 2);

        assertEquals(3, boardService.getPlayerPosition(playerId),
                "Nach 2 Schritten vom Feld 1 sollte der Spieler auf Feld 3 sein");
    }

    @Test
     void testGetMoveOptions() {
        String playerId = "player1";
        boardService.addPlayer(playerId, 1);

        List<Integer> options = boardService.getMoveOptions(playerId, 1);

        assertEquals(1, options.size(), "Es sollte genau eine Option geben");
        assertEquals(2, options.get(0).intValue(), "Die Option sollte Feld 2 sein");
    }

    @Test
     void testMovePlayerToField() {
        String playerId = "player1";
        boardService.addPlayer(playerId, 1);

        boolean result = boardService.movePlayerToField(playerId, 2);

        assertTrue(result, "Die Bewegung zum nächsten Feld sollte erfolgreich sein");
        assertEquals(2, boardService.getPlayerPosition(playerId));
    }

    @Test
     void testMovePlayerToInvalidField() {
        String playerId = "player1";
        boardService.addPlayer(playerId, 1);

        boolean result = boardService.movePlayerToField(playerId, 3);

        assertFalse(result, "Die Bewegung zu einem nicht erreichbaren Feld sollte fehlschlagen");
        assertEquals(1, boardService.getPlayerPosition(playerId), "Position sollte unverändert bleiben");
    }



    @Test
     void testGetPlayerField() {
        boardService.addPlayer("player1", 1);

        Field field = boardService.getPlayerField("player1");

        assertNotNull(field);
        assertEquals(1, field.getIndex());
    }

    @Test
     void testGetPlayerFieldWithUnknownPlayerDefaultsToField1() {
        Field field = boardService.getPlayerField("unknown");

        assertNotNull(field);
        assertEquals(1, field.getIndex());
    }

    @Test
     void testMovePlayerMultipleSteps() {
        String playerId = "player1";
        boardService.addPlayer(playerId, 1);

        boardService.movePlayer(playerId, 3); // 1 → 2 → 3 → 1

        int newPosition = boardService.getPlayerPosition(playerId);
        assertEquals(1, newPosition);
    }

    @Test
     void testMovePlayerToFieldWithValidPath() {
        String playerId = "player1";
        boardService.addPlayer(playerId, 1);

        boolean moved = boardService.movePlayerToField(playerId, 2);

        assertTrue(moved);
        assertEquals(2, boardService.getPlayerPosition(playerId));
    }

    @Test
     void testSetPlayerPosition() {
        String playerId = "player1";
        boardService.addPlayer(playerId, 1);

        boardService.setPlayerPosition(playerId, 3);

        assertEquals(3, boardService.getPlayerPosition(playerId));
    }
    @Test
     void testGetValidNextFields() {
        String playerId = "player1";
        boardService.addPlayer(playerId, 1);

        List<Field> validNextFields = boardService.getValidNextFields(playerId);

        assertEquals(1, validNextFields.size());
        assertEquals(2, validNextFields.get(0).getIndex());
    }

    @Test
     void testIsPlayerOnField() {
        String playerId = "player1";
        boardService.addPlayer(playerId, 2);

        assertTrue(boardService.isPlayerOnField(playerId, 2));
        assertFalse(boardService.isPlayerOnField(playerId, 1));
    }

    @Test
     void testGetPlayersOnField() {
        boardService.addPlayer("player1", 1);
        boardService.addPlayer("player2", 1);
        boardService.addPlayer("player3", 2);

        List<String> players = boardService.getPlayersOnField(1);

        assertTrue(players.contains("player1"));
        assertTrue(players.contains("player2"));
        assertFalse(players.contains("player3"));
    }

    @Test
     void testRemovePlayer() {
        String playerId = "player1";
        boardService.addPlayer(playerId, 1);
        boardService.removePlayer(playerId);

        assertFalse(boardService.getAllPlayerPositions().containsKey(playerId));
    }

    @Test
     void testIsAnyPlayerOnField() {
        boardService.addPlayer("player1", 2);

        assertTrue(boardService.isAnyPlayerOnField(2));
        assertFalse(boardService.isAnyPlayerOnField(3));
    }

    @Test
     void testResetAllPlayerPositions() {
        boardService.addPlayer("player1", 1);
        boardService.addPlayer("player2", 2);

        boardService.resetAllPlayerPositions();

        assertTrue(boardService.getAllPlayerPositions().isEmpty());
    }

    @Test
     void testGetBoardSize() {
        assertEquals(mockFields.size(), boardService.getBoardSize());
    }

    @Test
     void testGetBoard() {
        List<Field> board = boardService.getBoard();
        Field field1 = new Field(999, 0, 0, List.of(), FieldType.AKTION);

        assertEquals(mockFields, board);
        assertThrows(UnsupportedOperationException.class, () -> board.add(field1));
    }

}
