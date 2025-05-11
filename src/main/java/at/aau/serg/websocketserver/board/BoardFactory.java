package at.aau.serg.websocketserver.board;

import java.util.List;

public class BoardFactory {

    public static List<Field> createDefaultBoard() {
        return List.of(
                new Field(0, 0.115f, 0.65f, List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), "STARTNORMAL"),
                new Field(1, 0.15f, 0.617f, List.of(2,3,4,5,6,7,8,9,10,11), "ZAHLTAG"),
                new Field(2, 0.184f, 0.6f, List.of(3,4,5,6,7,8,9,10,11,12), "AKTION"),
                new Field(3, 0.2f, 0.58f, List.of(4,5,6,7,8,9,10,11,12,13), "ANLAGE"),
                new Field(4, 0.05f, 0.20f, List.of(5,6,7,8,9,10,11,12,13,14), "AKTION"),
                new Field(5, 0.05f, 0.20f, List.of(6,7,8,9,10,11,12,13,14,15), "FREUND"),
                new Field(6, 0.05f, 0.20f, List.of(7,8,9,10,11,12,13,14,15,16), "AKTION"),
                new Field(7, 0.05f, 0.20f, List.of(8,9,10,11,12,13,14,15,16), "BERUF"),
                new Field(8, 0.165f, 0.295f, List.of(9,10,11,12,13,14,15,16), "ZAHLTAG"),
                new Field(9, 0.405f, 0.62f, List.of(10,11,12,13,14,15,16), "AKTION"),
                new Field(10, 0.05f, 1.20f, List.of(11,12,13,14,15,16), "HAUS"),
                new Field(11, 0.05f, 0.20f, List.of(12,13,14,15,16), "AKTION"),
                new Field(12, 0.05f, 0.20f, List.of(13,14,15,16), "ZAHLTAG"),
                new Field(13, 0.05f, 0.20f, List.of(14,15,16), "AKTION"),
                new Field(14, 0.05f, 0.20f, List.of(15,16), "FREUND"),
                new Field(15, 0.05f, 0.20f, List.of(16), "AKTION"),
                new Field(16, 0.05f, 0.20f, List.of(16), "HEIRAT")
        );
    }
}
