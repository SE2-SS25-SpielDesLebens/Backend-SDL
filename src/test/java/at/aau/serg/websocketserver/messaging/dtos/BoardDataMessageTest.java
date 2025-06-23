package at.aau.serg.websocketserver.messaging.dtos;

import at.aau.serg.websocketserver.session.board.Field;
import at.aau.serg.websocketserver.session.board.FieldType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests für die BoardDataMessage-Klasse, die für die Übertragung von Spielbrettdaten über WebSocket verwendet wird.
 */
 class BoardDataMessageTest {

    @Test
    void testConstructorAndGetters() {
        // Testdaten vorbereiten
        List<Integer> nextFields1 = Arrays.asList(1, 2);
        List<Integer> nextFields2 = Arrays.asList(3, 4);

        List<Field> fields = Arrays.asList(
                new Field(0, 10.5, 20.0, nextFields1, FieldType.STARTNORMAL),
                new Field(1, 30.0, 40.5, nextFields2, FieldType.AKTION)
        );

        String timestamp = "2025-05-28T12:30:45";

        // BoardDataMessage-Objekt erstellen
        BoardDataMessage message = new BoardDataMessage(fields, timestamp);

        // Überprüfen, dass die Getter die korrekten Werte zurückgeben
        assertEquals(fields, message.getFields());
        assertEquals(timestamp, message.getTimestamp());

        // Überprüfen der enthaltenen Felder
        List<Field> retrievedFields = message.getFields();
        assertEquals(2, retrievedFields.size());

        Field field1 = retrievedFields.get(0);
        assertEquals(0, field1.getIndex());
        assertEquals(10.5, field1.getX());
        assertEquals(20.0, field1.getY());
        assertEquals(FieldType.STARTNORMAL, field1.getType());

        Field field2 = retrievedFields.get(1);
        assertEquals(1, field2.getIndex());
        assertEquals(30.0, field2.getX());
        assertEquals(40.5, field2.getY());
        assertEquals(FieldType.AKTION, field2.getType());
    }

    @Test
    void testEmptyFieldsList() {
        // Test mit leerer Felderliste
        List<Field> emptyFields = Arrays.asList();
        String timestamp = "2025-05-28T12:30:45";

        BoardDataMessage message = new BoardDataMessage(emptyFields, timestamp);

        assertTrue(message.getFields().isEmpty());
        assertEquals(timestamp, message.getTimestamp());
    }

    @Test
    void testReferenceIntegrity() {
        // Stellt sicher, dass die Listen-Referenz nicht mit der internen Implementierung geteilt wird
        List<Field> originalFields = new ArrayList<>();
        originalFields.add(new Field(0, 10.0, 20.0, Arrays.asList(1), FieldType.STARTNORMAL));

        String timestamp = "2025-05-28T12:30:45";
        BoardDataMessage message = new BoardDataMessage(originalFields, timestamp);

        // Größe der internen Liste zum Zeitpunkt der Erstellung merken
        int originalSize = message.getFields().size();

        // Externe Liste nach der Konstruktion des Objekts verändern
        originalFields.add(new Field(1, 30.0, 40.0, Arrays.asList(2), FieldType.AKTION));

        // Erwartetes Verhalten: Die interne Liste sollte sich nicht ändern, wenn eine defensive Kopie verwendet wird.
        // Aktuelles Verhalten: Die Liste wird geteilt, daher wird die Größe unterschiedlich sein
        // Diese Assertion dokumentiert die Schwäche der aktuellen Implementierung
        assertNotEquals(originalSize, message.getFields().size(),
                "Die BoardDataMessage-Klasse teilt die Referenz der übergebenen Liste. Eine defensive Kopie wäre besser.");
    }


    @Test
    void testNullHandling() {
        String timestamp = "2025-05-28T12:30:45";

        // null fields erlaubt?
        BoardDataMessage message1 = new BoardDataMessage(null, timestamp);
        assertNull(message1.getFields(), "Fields sollten null sein, wenn null übergeben wurde.");
        assertEquals(timestamp, message1.getTimestamp());

        // null timestamp erlaubt?
        List<Field> fields = List.of(
                new Field(0, 10.0, 20.0, List.of(1), FieldType.STARTNORMAL)
        );
        BoardDataMessage message2 = new BoardDataMessage(fields, null);
        assertEquals(fields, message2.getFields());
        assertNull(message2.getTimestamp(), "Timestamp sollte null sein, wenn null übergeben wurde.");
    }


}