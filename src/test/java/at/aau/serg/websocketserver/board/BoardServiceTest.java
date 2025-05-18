package at.aau.serg.websocketserver.board;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testet die BoardService-Klasse, die für die Verwaltung des Spielbretts und
 * der Spielerbewegungen zuständig ist.
 */
public class BoardServiceTest {
    
    private BoardService boardService;
    
    @BeforeEach
    void setUp() {
        // Erstelle eine neue BoardService-Instanz für jeden Test
        boardService = new BoardService();
    }
    
    @Test
    void testBoardInitialization() {
        // Überprüfe, dass das Brett korrekt erstellt wurde
        assertNotNull(boardService);
        assertEquals(20, boardService.getBoardSize()); // Wir erwarten 20 Felder
        
        // Überprüfe einige spezifische Felder
        Field startField = boardService.getFieldByIndex(0);
        assertNotNull(startField);
        assertEquals(0, startField.getIndex());
        assertEquals("START_NORMAL", startField.getType());
        
        Field uniStartField = boardService.getFieldByIndex(17);
        assertNotNull(uniStartField);
        assertEquals(17, uniStartField.getIndex());
        assertEquals("START_UNIVERSITY", uniStartField.getType());
    }
    
    @Test
    void testAddPlayer() {
        // Spieler mit gültiger Startposition hinzufügen
        int playerId = 1;
        int startFieldIndex = 0;
        boardService.addPlayer(playerId, startFieldIndex);
        
        // Überprüfen, dass die Position korrekt gesetzt wurde
        Field playerField = boardService.getPlayerField(playerId);
        assertEquals(startFieldIndex, playerField.getIndex());
        
        // Spieler mit Uni-Start hinzufügen
        int player2Id = 2;
        int uniStartFieldIndex = 17;
        boardService.addPlayer(player2Id, uniStartFieldIndex);
        
        // Überprüfen, dass die Position korrekt gesetzt wurde
        Field player2Field = boardService.getPlayerField(player2Id);
        assertEquals(uniStartFieldIndex, player2Field.getIndex());
    }
    
    @Test
    void testAddPlayerWithInvalidIndex() {
        // Spieler mit ungültigem Index hinzufügen (sollte auf 0 gesetzt werden)
        int playerId = 1;
        int invalidIndex = 999;
        boardService.addPlayer(playerId, invalidIndex);
        
        // Überprüfen, dass die Position auf 0 gesetzt wurde
        Field playerField = boardService.getPlayerField(playerId);
        assertEquals(0, playerField.getIndex());
    }
    
    @Test
    void testMovePlayer() {
        // Spieler hinzufügen
        int playerId = 1;
        boardService.addPlayer(playerId, 0);
        
        // Spieler um 3 Felder bewegen
        boardService.movePlayer(playerId, 3);
        
        // Überprüfen, dass der Spieler jetzt auf Feld 3 ist
        Field currentField = boardService.getPlayerField(playerId);
        assertEquals(3, currentField.getIndex());
        
        // Überprüfe, ob der Feldtyp stimmt
        assertEquals("INVESTMENT", currentField.getType());
    }
    
    @Test
    void testMovePlayerWithExactSteps() {
        // Spieler auf Feld 16 (Heirat, Endfeld) setzen
        int playerId = 1;
        boardService.setPlayerPosition(playerId, 16);
        
        // Versuche, den Spieler weiterzubewegen
        boardService.movePlayer(playerId, 3);
        
        // Überprüfen, dass der Spieler immer noch auf Feld 16 ist (Endfeld)
        Field currentField = boardService.getPlayerField(playerId);
        assertEquals(16, currentField.getIndex());
    }
    
    @Test
    void testMovePlayerWithZeroSteps() {
        // Spieler hinzufügen
        int playerId = 1;
        boardService.addPlayer(playerId, 5);
        
        // Spieler um 0 Felder bewegen (sollte keine Änderung bewirken)
        boardService.movePlayer(playerId, 0);
        
        // Überprüfen, dass der Spieler immer noch auf Feld 5 ist
        Field currentField = boardService.getPlayerField(playerId);
        assertEquals(5, currentField.getIndex());
    }
    
    @Test
    void testMovePlayerWithNegativeSteps() {
        // Spieler hinzufügen
        int playerId = 1;
        boardService.addPlayer(playerId, 5);
        
        // Spieler um negative Schritte bewegen (sollte keine Änderung bewirken)
        boardService.movePlayer(playerId, -3);
        
        // Überprüfen, dass der Spieler immer noch auf Feld 5 ist
        Field currentField = boardService.getPlayerField(playerId);
        assertEquals(5, currentField.getIndex());
    }
    
    @Test
    void testGetValidNextFields() {
        // Spieler hinzufügen
        int playerId = 1;
        boardService.addPlayer(playerId, 0);
        
        // Gültige nächste Felder abrufen
        List<Field> nextFields = boardService.getValidNextFields(playerId);
        
        // Überprüfen, dass es genau ein nächstes Feld gibt (Feld 1)
        assertEquals(1, nextFields.size());
        assertEquals(1, nextFields.get(0).getIndex());
    }
    
    @Test
    void testGetValidNextFieldsForUnknownPlayer() {
        // Gültige nächste Felder für einen nicht registrierten Spieler abrufen
        // Sollte die nextFields für Feld 0 (Standardposition) zurückgeben
        int unknownPlayerId = 999;
        List<Field> nextFields = boardService.getValidNextFields(unknownPlayerId);
        
        // Überprüfen, dass es genau ein nächstes Feld gibt (Feld 1)
        assertEquals(1, nextFields.size());
        assertEquals(1, nextFields.get(0).getIndex());
    }
    
    @Test
    void testMovePlayerToField() {
        // Spieler hinzufügen
        int playerId = 1;
        boardService.addPlayer(playerId, 0);
        
        // Versuche, den Spieler direkt auf Feld 1 zu bewegen (gültiger Zug)
        boolean success = boardService.movePlayerToField(playerId, 1);
        
        // Überprüfen, dass der Zug erfolgreich war und der Spieler jetzt auf Feld 1 ist
        assertTrue(success);
        Field currentField = boardService.getPlayerField(playerId);
        assertEquals(1, currentField.getIndex());
        
        // Versuche, den Spieler direkt auf Feld 5 zu bewegen (ungültiger Zug)
        success = boardService.movePlayerToField(playerId, 5);
        
        // Überprüfen, dass der Zug nicht erfolgreich war und der Spieler immer noch auf Feld 1 ist
        assertFalse(success);
        currentField = boardService.getPlayerField(playerId);
        assertEquals(1, currentField.getIndex());
    }
    
    @Test
    void testSetPlayerPosition() {
        // Spieler hinzufügen
        int playerId = 1;
        boardService.addPlayer(playerId, 0);
        
        // Position des Spielers direkt setzen
        boardService.setPlayerPosition(playerId, 5);
        
        // Überprüfen, dass der Spieler jetzt auf Feld 5 ist
        Field currentField = boardService.getPlayerField(playerId);
        assertEquals(5, currentField.getIndex());
        
        // Versuche, eine ungültige Position zu setzen
        boardService.setPlayerPosition(playerId, 999);
        
        // Überprüfen, dass die Position nicht geändert wurde
        currentField = boardService.getPlayerField(playerId);
        assertEquals(5, currentField.getIndex());
    }
    
    @Test
    void testGetFieldByInvalidIndex() {
        // Versuche, ein Feld mit ungültigem Index abzurufen
        Field field = boardService.getFieldByIndex(-1);
        assertNull(field);
        
        field = boardService.getFieldByIndex(999);
        assertNull(field);
    }
    
    @Test
    void testSpecialPathFromUniToWork() {
        // Testen des speziellen Pfads vom Uni-Examen zum Freundesfeld
        
        // Spieler hinzufügen und auf das Examen-Feld setzen
        int playerId = 1;
        boardService.setPlayerPosition(playerId, 19); // Examen-Feld
        
        // Den Spieler einen Schritt bewegen
        boardService.movePlayer(playerId, 1);
        
        // Überprüfen, dass der Spieler jetzt auf Feld 5 (Freund) ist
        // Dies ist die spezielle Verbindung vom Examen ins Berufsleben
        Field currentField = boardService.getPlayerField(playerId);
        assertEquals(5, currentField.getIndex());
        assertEquals("STOP_FAMILY", currentField.getType());
    }
}
