package at.aau.serg.websocketserver.session.board;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testklasse für die Field-Klasse.
 */
 class FieldTest {

    @Test
     void testFieldCreation() {
        // Arrangement
        int index = 5;
        double x = 0.25;
        double y = 0.55;
        List<Integer> nextFields = Collections.singletonList(6);
        FieldType type = FieldType.AKTION;
        
        // Action
        Field field = new Field(index, x, y, nextFields, type);
        
        // Assertion
        assertEquals(index, field.getIndex(), "Index sollte korrekt gesetzt sein");
        assertEquals(x, field.getX(), 0.001, "X-Koordinate sollte korrekt gesetzt sein");
        assertEquals(y, field.getY(), 0.001, "Y-Koordinate sollte korrekt gesetzt sein");
        assertEquals(nextFields, field.getNextFields(), "Nächste Felder sollten korrekt gesetzt sein");
        assertEquals(type, field.getType(), "Feldtyp sollte korrekt gesetzt sein");
    }
    

    
    @Test
     void testSetters() {
        // Arrangement
        Field field = new Field(1, 0.1, 0.1, Collections.singletonList(2), FieldType.AKTION);
        
        // Action
        field.setIndex(10);
        field.setX(0.5);
        field.setY(0.6);
        field.setNextFields(Arrays.asList(20, 30));
        field.setType(FieldType.HAUS);
        
        // Assertion
        assertEquals(10, field.getIndex(), "Index sollte geändert sein");
        assertEquals(0.5, field.getX(), 0.001, "X-Koordinate sollte geändert sein");
        assertEquals(0.6, field.getY(), 0.001, "Y-Koordinate sollte geändert sein");
        assertEquals(Arrays.asList(20, 30), field.getNextFields(), "Nächste Felder sollten geändert sein");
        assertEquals(FieldType.HAUS, field.getType(), "Feldtyp sollte geändert sein");
    }
    

}
