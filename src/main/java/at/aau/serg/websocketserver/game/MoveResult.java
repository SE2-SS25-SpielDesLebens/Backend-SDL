package at.aau.serg.websocketserver.game;

import java.util.List;

public class MoveResult {
    private int fieldIndex;
    private boolean requiresChoice;
    private List<Integer> options;
    
    public MoveResult(int fieldIndex, boolean requiresChoice, List<Integer> options) {
        this.fieldIndex = fieldIndex;
        this.requiresChoice = requiresChoice;
        this.options = options;
    }
    
    public int getFieldIndex() { 
        return fieldIndex; 
    }
    
    public boolean isRequiresChoice() { 
        return requiresChoice; 
    }
    
    public List<Integer> getOptions() { 
        return options; 
    }
}