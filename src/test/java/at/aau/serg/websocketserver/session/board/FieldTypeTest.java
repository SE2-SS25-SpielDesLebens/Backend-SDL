package at.aau.serg.websocketserver.session.board;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testklasse für das FieldType-Enum.
 */
public class FieldTypeTest {

    @Test
    public void testEnumValues() {
        // Es sollten alle definierten Feldtypen vorhanden sein
        assertEquals(14, FieldType.values().length, "Es sollten 14 verschiedene Feldtypen definiert sein");
        
        // Prüfe einige spezifische Werte
        assertNotNull(FieldType.AKTION, "AKTION sollte ein gültiger Feldtyp sein");
        assertNotNull(FieldType.STARTNORMAL, "STARTNORMAL sollte ein gültiger Feldtyp sein");
        assertNotNull(FieldType.STARTUNI, "STARTUNI sollte ein gültiger Feldtyp sein");
        assertNotNull(FieldType.ZAHLTAG, "ZAHLTAG sollte ein gültiger Feldtyp sein");
        assertNotNull(FieldType.RUHESTAND, "RUHESTAND sollte ein gültiger Feldtyp sein");
    }
    
    @Test
    public void testValueOf() {
        // Die valueOf-Methode sollte den korrekten Enum-Wert für einen gegebenen String zurückgeben
        assertEquals(FieldType.AKTION, FieldType.valueOf("AKTION"), "valueOf sollte für AKTION funktionieren");
        assertEquals(FieldType.ZAHLTAG, FieldType.valueOf("ZAHLTAG"), "valueOf sollte für ZAHLTAG funktionieren");
        
        // Ungültiger Name sollte eine Exception werfen
        assertThrows(IllegalArgumentException.class, () -> FieldType.valueOf("UNGUELTIG"),
                "valueOf mit ungültigem Namen sollte eine Exception werfen");
    }
}
