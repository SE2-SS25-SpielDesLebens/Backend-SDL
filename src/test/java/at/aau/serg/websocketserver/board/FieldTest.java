package at.aau.serg.websocketserver.board;

import at.aau.serg.websocketserver.session.board.Field;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testet die Field-Klasse, die ein Feld auf dem Spielbrett repräsentiert.
 */
public class FieldTest {

    @Test
    void testFieldConstructorWithoutNextFields() {
        // Testdaten
        int index = 5;
        float x = 0.3f;
        float y = 0.5f;
        String type = "AKTION";
        
        // Erstelle ein Field-Objekt
        Field field = new Field(index, x, y, type);
        
        // Überprüfe, ob alle Attribute korrekt gesetzt wurden
        assertEquals(index, field.getIndex());
        assertEquals(x, field.getX(), 0.001f); // Verwende Delta für float-Vergleich
        assertEquals(y, field.getY(), 0.001f);
        assertEquals(type, field.getType());
        assertEquals(0, field.getNextFields().size()); // Keine nextFields bei der Erstellung
    }
    
    @Test
    void testFieldConstructorWithNextFields() {
        // Testdaten
        int index = 5;
        float x = 0.3f;
        float y = 0.5f;
        String type = "AKTION";
        List<Integer> nextFields = Arrays.asList(6, 7);
        
        // Erstelle ein Field-Objekt
        Field field = new Field(index, x, y, nextFields, type);
        
        // Überprüfe, ob alle Attribute korrekt gesetzt wurden
        assertEquals(index, field.getIndex());
        assertEquals(x, field.getX(), 0.001f);
        assertEquals(y, field.getY(), 0.001f);
        assertEquals(type, field.getType());
        
        // Überprüfe nextFields
        List<Integer> retrievedNextFields = field.getNextFields();
        assertEquals(2, retrievedNextFields.size());
        assertTrue(retrievedNextFields.contains(6));
        assertTrue(retrievedNextFields.contains(7));
    }
    
    @Test
    void testAddNextField() {
        // Erstelle ein Field-Objekt ohne nextFields
        Field field = new Field(1, 0.1f, 0.1f, "AKTION");
        
        // Füge nextField hinzu und überprüfe
        field.addNextField(2);
        List<Integer> nextFields = field.getNextFields();
        assertEquals(1, nextFields.size());
        assertTrue(nextFields.contains(2));
        
        // Füge weiteres nextField hinzu und überprüfe
        field.addNextField(3);
        nextFields = field.getNextFields();
        assertEquals(2, nextFields.size());
        assertTrue(nextFields.contains(2));
        assertTrue(nextFields.contains(3));
        
        // Stelle sicher, dass Duplikate nicht hinzugefügt werden
        field.addNextField(2); // Versuche, eine bereits vorhandene ID hinzuzufügen
        nextFields = field.getNextFields();
        assertEquals(2, nextFields.size()); // Sollte immer noch 2 sein
    }
    
    @Test
    void testGetNextFieldsReturnsDefensiveCopy() {
        // Erstelle ein Field-Objekt
        Field field = new Field(1, 0.1f, 0.1f, "AKTION");
        field.addNextField(2);
        
        // Hole nextFields und versuche, sie zu ändern
        List<Integer> nextFields = field.getNextFields();
        nextFields.add(3); // Diese Änderung sollte das Original nicht beeinflussen
        
        // Überprüfe, dass das Original nicht verändert wurde
        List<Integer> originalNextFields = field.getNextFields();
        assertEquals(1, originalNextFields.size());
        assertTrue(originalNextFields.contains(2));
        assertFalse(originalNextFields.contains(3));
    }
    
    @Test
    void testConstructorWithNextFieldsCreatesDefensiveCopy() {
        // Erstelle eine Liste von nextFields
        List<Integer> nextFields = new ArrayList<>();
        nextFields.add(2);
        nextFields.add(3);
        
        // Erstelle ein Field-Objekt mit diesen nextFields
        Field field = new Field(1, 0.1f, 0.1f, nextFields, "AKTION");
        
        // Ändere die ursprüngliche Liste
        nextFields.add(4);
        
        // Überprüfe, dass das Field-Objekt nicht betroffen ist
        List<Integer> fieldNextFields = field.getNextFields();
        assertEquals(2, fieldNextFields.size());
        assertTrue(fieldNextFields.contains(2));
        assertTrue(fieldNextFields.contains(3));
        assertFalse(fieldNextFields.contains(4));
    }
}
