package at.aau.serg.websocketserver.game;

import java.util.List;

public class Field {
    private int index;
    private float x;
    private float y;
    private List<Integer> nextFields;
    private FieldType type;
    
    // Konstruktor
    public Field(int index, float x, float y, List<Integer> nextFields, FieldType type) {
        this.index = index;
        this.x = x;
        this.y = y;
        this.nextFields = nextFields;
        this.type = type;
    }
    
    // Getter und Setter
    public int getIndex() {
        return index;
    }
    
    public float getX() {
        return x;
    }
    
    public float getY() {
        return y;
    }
    
    public List<Integer> getNextFields() {
        return nextFields;
    }
    
    public FieldType getType() {
        return type;
    }
}