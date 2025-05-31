package at.aau.serg.websocketserver.session.board;

import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Standardimplementierung des BoardDataProvider-Interfaces.
 * Diese Klasse enthält die statischen Daten des Spielbretts.
 */
@Component
public class BoardData implements BoardDataProvider {

    private static final List<Field> BOARD = Arrays.asList(
        // x größer ist rechts und y rauf ist kleiner
            new Field(1, 0.1, 0.64, Arrays.asList(1,2,3,4,5,6,7,8,9,20), FieldType.STARTNORMAL),
            new Field(2, 0.145, 0.6205, Arrays.asList(2,3,4,5,6,7,8,9,20,21), FieldType.ZAHLTAG),
            new Field(3, 0.18, 0.6, Collections.singletonList(3), FieldType.AKTION),
            new Field(4, 0.2105, 0.571, Collections.singletonList(4), FieldType.ANLAGE),
            new Field(5, 0.243, 0.598, Collections.singletonList(5), FieldType.AKTION),
            new Field(6, 0.278, 0.57, Collections.singletonList(20), FieldType.AKTION),
            new Field(7, 0.308, 0.548, Collections.singletonList(7), FieldType.AKTION),
            new Field(8, 0.339, 0.57, Collections.singletonList(8), FieldType.BERUF),
            new Field(9, 0.3715, 0.599, Collections.singletonList(9), FieldType.ZAHLTAG),

// Uni-Start-Feld
            new Field(10, 0.1, 0.8, Collections.singletonList(18), FieldType.STARTUNI),
            new Field(11, 0.145, 0.785, Collections.singletonList(19), FieldType.AKTION),
            new Field(12, 0.178, 0.806, Collections.singletonList(6), FieldType.FREUND),
            new Field(13, 0.2102, 0.788, Collections.singletonList(20), FieldType.AKTION),
            new Field(14, 0.243, 0.7605, Collections.singletonList(6), FieldType.FREUND),
            new Field(15, 0.275, 0.735, Collections.singletonList(20), FieldType.AKTION),
            new Field(16, 0.308, 0.712, Collections.singletonList(20), FieldType.AKTION),
            new Field(17, 0.339, 0.738, Collections.singletonList(6), FieldType.FREUND),
            new Field(18, 0.372, 0.71, Collections.singletonList(20), FieldType.AKTION),
            new Field(19, 0.405, 0.68, Collections.singletonList(5), FieldType.EXAMEN),

            new Field(20, 0.405, 0.618, Collections.singletonList(10), FieldType.AKTION),
            new Field(21, 0.55, 0.31, Collections.singletonList(11), FieldType.HAUS),
            new Field(22, 0.60, 0.27, Collections.singletonList(12), FieldType.AKTION),
            new Field(23, 0.65, 0.23, Collections.singletonList(13), FieldType.ZAHLTAG),
            new Field(24, 0.70, 0.19, Collections.singletonList(14), FieldType.AKTION),
            new Field(25, 0.75, 0.15, Collections.singletonList(15), FieldType.FREUND),
            new Field(26, 0.10, 0.11, Collections.singletonList(16), FieldType.AKTION),
            new Field(27, 0.85, 0.07, Collections.singletonList(16), FieldType.HEIRAT)

            );

    @Override
    public List<Field> getBoard() {
        return Collections.unmodifiableList(BOARD);
    }

    @Override
    public Field getFieldByIndex(int index) {
        for (Field field : BOARD) {
            if (field.getIndex() == index) {
                return field;
            }
        }
        return null;
    }
    
    /**
     * Statische Methode für Abwärtskompatibilität, sollte in neuen Code nicht verwendet werden.
     * Verwenden Sie stattdessen die Instanzmethode.
     *
     * @return Eine unveränderbare Liste aller Felder
     * @deprecated Verwende die Instanz über Dependency Injection
     */
    @Deprecated
    public static List<Field> getBoardStatic() {
        return Collections.unmodifiableList(BOARD);
    }
    
    /**
     * Statische Methode für Abwärtskompatibilität, sollte in neuen Code nicht verwendet werden.
     * Verwenden Sie stattdessen die Instanzmethode.
     *
     * @param index Der Index des gesuchten Felds
     * @return Das gefundene Feld oder null, wenn kein Feld mit diesem Index existiert
     * @deprecated Verwende die Instanz über Dependency Injection
     */
    @Deprecated
    public static Field getFieldByIndexStatic(int index) {
        for (Field field : BOARD) {
            if (field.getIndex() == index) {
                return field;
            }
        }
        return null;
    }
}
