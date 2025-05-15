package at.aau.serg.websocketserver.board;

import java.util.List;
import java.util.ArrayList;

/**
 * Repräsentiert ein Feld auf dem Spielbrett.
 */
public class Field {
    private final int index;
    private final float x;
    private final float y;
    private final String type;
    private final List<Integer> nextFields;
    
    public Field(int index, float x, float y, String type) {
        this.index = index;
        this.x = x;
        this.y = y;
        this.type = type;
        this.nextFields = new ArrayList<>();
    }
    
    public Field(int index, float x, float y, List<Integer> nextFields, String type) {
        this.index = index;
        this.x = x;
        this.y = y;
        this.type = type;
        this.nextFields = new ArrayList<>(nextFields);
    }
    
    public int getIndex() {
        return index;
    }
    
    public float getX() {
        return x;
    }
    
    public float getY() {
        return y;
    }
    
    public String getType() {
        return type;
    }
    
    /**
     * Gibt die Liste der möglichen nächsten Felder zurück.
     */
    public List<Integer> getNextFields() {
        return new ArrayList<>(nextFields);
    }
    
    /**
     * Fügt ein mögliches nächstes Feld hinzu.
     */
    public void addNextField(int fieldIndex) {
        if (!nextFields.contains(fieldIndex)) {
            nextFields.add(fieldIndex);
        }
    }
}
