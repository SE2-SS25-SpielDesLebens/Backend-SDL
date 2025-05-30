package at.aau.serg.websocketserver.messaging.dtos;

import at.aau.serg.websocketserver.board.FieldType;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testet die MoveMessage-Klasse, die für die Übertragung von Bewegungsdaten über WebSockets verwendet wird.
 */
public class MoveMessageTest {
    
    @Test
    void testConstructorWithoutNextPossibleFields() {
        // Testdaten
        String playerName = "Player1";
        int index = 5;
        FieldType type = FieldType.AKTION;
        String timestamp = "2023-05-13T15:30:00";
        
        // Erstelle MoveMessage-Objekt
        MoveMessage message = new MoveMessage(playerName, index, type, timestamp);
        
        // Überprüfe, ob alle Attribute korrekt gesetzt wurden
        assertEquals(playerName, message.getPlayerName());
        assertEquals(index, message.getIndex());
        assertEquals(type, message.getType());
        assertEquals(timestamp, message.getTimestamp());
        assertEquals(0, message.getNextPossibleFields().size()); // Keine nextPossibleFields
    }
    
    @Test
    void testConstructorWithNextPossibleFields() {
        // Testdaten
        String playerName = "Player1";
        int index = 5;
        FieldType type = FieldType.AKTION;
        String timestamp = "2023-05-13T15:30:00";
        List<Integer> nextPossibleFields = Arrays.asList(6, 7);
        
        // Erstelle MoveMessage-Objekt
        MoveMessage message = new MoveMessage(playerName, index, type, timestamp, nextPossibleFields);
        
        // Überprüfe, ob alle Attribute korrekt gesetzt wurden
        assertEquals(playerName, message.getPlayerName());
        assertEquals(index, message.getIndex());
        assertEquals(type, message.getType());
        assertEquals(timestamp, message.getTimestamp());
        
        // Überprüfe nextPossibleFields
        List<Integer> retrievedFields = message.getNextPossibleFields();
        assertEquals(2, retrievedFields.size());
        assertTrue(retrievedFields.contains(6));
        assertTrue(retrievedFields.contains(7));
    }
    
    @Test
    void testGetNextPossibleFieldsReturnsDefensiveCopy() {
        // Testdaten
        String playerName = "Player1";
        int index = 5;
        FieldType type = FieldType.AKTION;
        String timestamp = "2023-05-13T15:30:00";
        List<Integer> nextPossibleFields = Arrays.asList(6, 7);
        
        // Erstelle MoveMessage-Objekt
        MoveMessage message = new MoveMessage(playerName, index, type, timestamp, nextPossibleFields);
        
        // Hole nextPossibleFields und versuche, sie zu ändern
        List<Integer> retrievedFields = message.getNextPossibleFields();
        retrievedFields.add(8); // Diese Änderung sollte das Original nicht beeinflussen
        
        // Überprüfe, dass das Original nicht verändert wurde
        List<Integer> originalFields = message.getNextPossibleFields();
        assertEquals(2, originalFields.size());
        assertTrue(originalFields.contains(6));
        assertTrue(originalFields.contains(7));
        assertFalse(originalFields.contains(8));
    }
    
 
}
