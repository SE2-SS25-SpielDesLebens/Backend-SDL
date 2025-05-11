package at.aau.serg.websocketserver.board;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data // erzeugt Getter, Setter, toString, equals, hashCode
@NoArgsConstructor // erzeugt einen no-args Konstruktor
@AllArgsConstructor // erzeugt einen Konstruktor mit allen Feldern
public class Field {
    private int index;
    private float x;
    private float y;
    private List<Integer> nextFields;
    private String type;
}
