package at.aau.serg.websocketserver.messaging.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HouseBuyElseSellMessage {
    private String playerID;
    private int gameId;
    private boolean buyElseSell;
    private int houseId;


    public boolean buyElseSell() {
        return buyElseSell;
    }
}
