package at.aau.serg.websocketserver.session.board;



import java.util.ArrayList;
import java.util.List;

public class Field {
    private int index;
    private double x;
    private double y;
    private List<Integer> nextFields;
    private FieldType type;

    public Field(int index, double x, double y, List<Integer> nextFields, FieldType type) {
        this.index = index;
        this.x = x;
        this.y = y;
        this.nextFields = new ArrayList<>(nextFields);
        this.type = type;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public List<Integer> getNextFields() {
        return nextFields;
    }

    public void setNextFields(List<Integer> nextFields) {
        this.nextFields = new ArrayList<>(nextFields);
    }

    public FieldType getType() {
        return type;
    }

    public void setType(FieldType type) {
        this.type = type;
    }
}
