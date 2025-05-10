package at.aau.serg.websocketserver.game;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BoardData {
    public static List<Field> getInitialFields() {
        return Arrays.asList(
            new Field(0, 0.115f, 0.65f, Arrays.asList(1), FieldType.STARTNORMAL),
            new Field(1, 0.15f, 0.617f, Arrays.asList(2), FieldType.ZAHLTAG),
            new Field(2, 0.184f, 0.6f, Arrays.asList(3), FieldType.AKTION),
            new Field(3, 0.22f, 0.58f, Arrays.asList(4), FieldType.FREUND),
            new Field(4, 0.25f, 0.55f, Arrays.asList(5, 10), FieldType.BERUF),
            
            // Uni-Pfad
            new Field(5, 0.25f, 0.5f, Arrays.asList(6), FieldType.STARTUNI),
            new Field(6, 0.25f, 0.45f, Arrays.asList(7), FieldType.ZAHLTAG),
            new Field(7, 0.25f, 0.4f, Arrays.asList(8), FieldType.AKTION),
            new Field(8, 0.25f, 0.35f, Arrays.asList(9), FieldType.ZAHLTAG),
            new Field(9, 0.25f, 0.3f, Arrays.asList(10), FieldType.BERUF),
            
            // Nach Verzweigung
            new Field(10, 0.3f, 0.28f, Arrays.asList(11), FieldType.ZAHLTAG),
            new Field(11, 0.35f, 0.26f, Arrays.asList(12), FieldType.HEIRAT),
            new Field(12, 0.4f, 0.24f, Arrays.asList(13), FieldType.HAUS),
            new Field(13, 0.45f, 0.22f, Arrays.asList(14), FieldType.ZAHLTAG),
            new Field(14, 0.5f, 0.20f, Arrays.asList(15), FieldType.AKTION),
            new Field(15, 0.55f, 0.20f, Collections.emptyList(), FieldType.RUHESTAND)
        );
    }
}