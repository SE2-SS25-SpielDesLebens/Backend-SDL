package at.aau.serg.websocketserver.fieldlogic;

import java.util.List;

public class Field {
    private final int index;
    private final FieldType type;
    private final List<Integer> nextFields;

    public Field(int index, FieldType type, List<Integer> nextFields) {
        this.index = index;
        this.type = type;
        this.nextFields = nextFields;
    }

    public int getIndex() { return index; }
    public FieldType getType() { return type; }
    public List<Integer> getNextFields() { return nextFields; }
}
