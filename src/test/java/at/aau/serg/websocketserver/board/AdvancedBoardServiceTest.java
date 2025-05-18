package at.aau.serg.websocketserver.board;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Erweiterte Tests für die BoardService-Klasse, die komplexere Szenarien und Edge-Cases abdecken.
 */
public class AdvancedBoardServiceTest {
    
    private BoardService boardService;
    
    @BeforeEach
    void setUp() {
        boardService = new BoardService();
    }
    
    @Test
    void testMultiplePlayerManagement() {
        // Mehrere Spieler auf dem Brett verwalten
        boardService.addPlayer(1, 0); // Spieler 1 auf normales Startfeld
        boardService.addPlayer(2, 17); // Spieler 2 auf Uni-Startfeld
        boardService.addPlayer(3, 5); // Spieler 3 auf Freund-Feld
        
        // Überprüfen, dass alle Spieler die korrekte Startposition haben
        assertEquals(0, boardService.getPlayerField(1).getIndex());
        assertEquals(17, boardService.getPlayerField(2).getIndex());
        assertEquals(5, boardService.getPlayerField(3).getIndex());
        
        // Einen Spieler bewegen und sicherstellen, dass die anderen unverändert bleiben
        boardService.movePlayer(2, 2); // Spieler 2 zwei Felder bewegen (17 -> 18 -> 19)
        
        assertEquals(0, boardService.getPlayerField(1).getIndex()); // Unverändert
        assertEquals(19, boardService.getPlayerField(2).getIndex()); // Geändert
        assertEquals(5, boardService.getPlayerField(3).getIndex()); // Unverändert
    }
    
    @Test
    void testMovePlayerToInvalidField() {
        // Spieler hinzufügen
        int playerId = 1;
        boardService.addPlayer(playerId, 0);
        
        // Versuche, zu einem Feld zu bewegen, das nicht in nextFields vorhanden ist
        boolean success = boardService.movePlayerToField(playerId, 2); // Feld 0 -> Feld 2 (ungültig, da Feld 0 -> Feld 1)
        
        // Überprüfen, dass der Zug nicht erfolgreich war
        assertFalse(success);
        assertEquals(0, boardService.getPlayerField(playerId).getIndex());
    }
    
    @Test
    void testBoundaryMovement() {
        // Spieler am Ende des Spielbretts testen
        int playerId = 1;
        boardService.setPlayerPosition(playerId, 16); // Heirat-Feld (Endfeld)
        
        // Versuche, verschiedene Bewegungen auszuführen
        boardService.movePlayer(playerId, 1);
        assertEquals(16, boardService.getPlayerField(playerId).getIndex()); // Sollte unverändert bleiben
        
        boardService.movePlayer(playerId, 5);
        assertEquals(16, boardService.getPlayerField(playerId).getIndex()); // Sollte unverändert bleiben
        
        boardService.movePlayerToField(playerId, 0);
        assertEquals(16, boardService.getPlayerField(playerId).getIndex()); // Sollte unverändert bleiben
    }
    
    @Test
    void testUniversityPathCompleteJourney() {
        // Test für den kompletten Universitätspfad
        int playerId = 1;
        
        // Spieler auf das Uni-Startfeld setzen
        boardService.setPlayerPosition(playerId, 17);
        assertEquals("START_UNIVERSITY", boardService.getPlayerField(playerId).getType());
        
        // Einen Schritt bewegen (zu Zahltag)
        boardService.movePlayer(playerId, 1);
        assertEquals(18, boardService.getPlayerField(playerId).getIndex());
        assertEquals("PAYDAY", boardService.getPlayerField(playerId).getType());
        
        // Noch einen Schritt (zu Examen)
        boardService.movePlayer(playerId, 1);
        assertEquals(19, boardService.getPlayerField(playerId).getIndex());
        assertEquals("STOP_EXAM", boardService.getPlayerField(playerId).getType());
        
        // Nach Examen sollte der Spieler zum Freund-Feld gehen
        boardService.movePlayer(playerId, 1);
        assertEquals(5, boardService.getPlayerField(playerId).getIndex());
        assertEquals("STOP_FAMILY", boardService.getPlayerField(playerId).getType());
        
        // Von dort aus normal weitergehen
        boardService.movePlayer(playerId, 1);
        assertEquals(6, boardService.getPlayerField(playerId).getIndex());
        assertEquals("ACTION", boardService.getPlayerField(playerId).getType());
    }
    
    @Test
    void testNonExistentPlayer() {
        // Testen des Verhaltens für nicht existierende Spieler
        int nonExistentPlayerId = 999;
        
        // Position abfragen sollte das Standardfeld zurückgeben
        Field defaultField = boardService.getPlayerField(nonExistentPlayerId);
        assertEquals(0, defaultField.getIndex());
        
        // Versuche, den nicht existierenden Spieler zu bewegen
        boardService.movePlayer(nonExistentPlayerId, 3);
        
        // Position sollte jetzt aktualisiert sein, als ob der Spieler erstellt wurde
        Field updatedField = boardService.getPlayerField(nonExistentPlayerId);
        assertEquals(3, updatedField.getIndex());
    }
}
