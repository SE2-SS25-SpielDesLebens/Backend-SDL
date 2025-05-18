package at.aau.serg.websocketserver.board;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BoardData {

    private static final List<Field> BOARD = Arrays.asList(
        // x größer ist rechts und y rauf ist kleiner
        new Field(1, 0.115, 0.65, Collections.singletonList(2), FieldType.STARTNORMAL),
        new Field(2, 0.15, 0.617, Collections.singletonList(3), FieldType.ZAHLTAG),
        new Field(3, 0.184, 0.6, Collections.singletonList(4), FieldType.AKTION),
        new Field(4, 0.21, 0.58, Collections.singletonList(5), FieldType.ANLAGE),
        new Field(5, 0.25, 0.55, Collections.singletonList(6), FieldType.AKTION),
        new Field(6, 0.30, 0.51, Collections.singletonList(7), FieldType.FREUND),
        new Field(7, 0.35, 0.47, Collections.singletonList(8), FieldType.AKTION),
        new Field(8, 0.40, 0.43, Collections.singletonList(9), FieldType.BERUF),
        new Field(9, 0.45, 0.39, Collections.singletonList(10), FieldType.ZAHLTAG),
        new Field(10, 0.50, 0.35, Collections.singletonList(11), FieldType.AKTION),
        new Field(11, 0.55, 0.31, Collections.singletonList(12), FieldType.HAUS),
        new Field(12, 0.60, 0.27, Collections.singletonList(13), FieldType.AKTION),
        new Field(13, 0.65, 0.23, Collections.singletonList(14), FieldType.ZAHLTAG),
        new Field(14, 0.70, 0.19, Collections.singletonList(15), FieldType.AKTION),
        new Field(15, 0.75, 0.15, Collections.singletonList(16), FieldType.FREUND),
        new Field(16, 0.80, 0.11, Collections.singletonList(17), FieldType.AKTION),
        new Field(17, 0.85, 0.07, Collections.singletonList(1), FieldType.HEIRAT),
        
        // Uni-Start-Feld
        new Field(18, 0.115, 0.75, Collections.singletonList(19), FieldType.STARTUNI),
        new Field(19, 0.30, 0.72, Collections.singletonList(20), FieldType.ZAHLTAG),
        new Field(20, 0.35, 0.70, Collections.singletonList(5), FieldType.EXAMEN)
    );

    public static List<Field> getBoard() {
        return Collections.unmodifiableList(BOARD);
    }

    public static Field getFieldByIndex(int index) {
        for (Field field : BOARD) {
            if (field.getIndex() == index) {
                return field;
            }
        }
        return null;
    }
}
