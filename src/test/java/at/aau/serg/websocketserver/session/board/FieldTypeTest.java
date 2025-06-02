package at.aau.serg.websocketserver.session.board;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testklasse für das FieldType-Enum.
 */
public class FieldTypeTest {    @Test
    public void testEnumValues() {
        // Es sollten alle definierten Feldtypen vorhanden sein
        assertEquals(25, FieldType.values().length, "Es sollten 25 verschiedene Feldtypen definiert sein");
        
        // Prüfe einige spezifische Werte
        assertNotNull(FieldType.AKTION, "AKTION sollte ein gültiger Feldtyp sein");
        assertNotNull(FieldType.STARTNORMAL, "STARTNORMAL sollte ein gültiger Feldtyp sein");
        assertNotNull(FieldType.STARTUNI, "STARTUNI sollte ein gültiger Feldtyp sein");
        assertNotNull(FieldType.ZAHLTAG, "ZAHLTAG sollte ein gültiger Feldtyp sein");
        assertNotNull(FieldType.RUHESTAND, "RUHESTAND sollte ein gültiger Feldtyp sein");
        assertNotNull(FieldType.BABY, "BABY sollte ein gültiger Feldtyp sein");
        assertNotNull(FieldType.ZWILLINGE, "ZWILLINGE sollte ein gültiger Feldtyp sein");
        assertNotNull(FieldType.TIER, "TIER sollte ein gültiger Feldtyp sein");
        assertNotNull(FieldType.HEIRAT_JA, "HEIRAT_JA sollte ein gültiger Feldtyp sein");
        assertNotNull(FieldType.HEIRAT_NEIN, "HEIRAT_NEIN sollte ein gültiger Feldtyp sein");
        assertNotNull(FieldType.KINDER_JA, "KINDER_JA sollte ein gültiger Feldtyp sein");
        assertNotNull(FieldType.KINDER_NEIN, "KINDER_NEIN sollte ein gültiger Feldtyp sein");
        assertNotNull(FieldType.MIDLIFECHRISIS_ROT, "MIDLIFECHRISIS_ROT sollte ein gültiger Feldtyp sein");
        assertNotNull(FieldType.MIDLIFECHRISIS_SCHWARZ, "MIDLIFECHRISIS_SCHWARZ sollte ein gültiger Feldtyp sein");
        assertNotNull(FieldType.FRUEHPENSION_JA, "FRUEHPENSION_JA sollte ein gültiger Feldtyp sein");
        assertNotNull(FieldType.FRUEHPENSION_NEIN, "FRUEHPENSION_NEIN sollte ein gültiger Feldtyp sein");
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
