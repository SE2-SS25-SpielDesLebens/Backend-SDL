package at.aau.serg.websocketserver.messaging.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HouseMessage {
    private int houseId;
    private String bezeichnung;
    private int kaufpreis;
    private int verkaufspreisRot;
    private int verkaufspreisSchwarz;
    private boolean isTaken;
    private String assignedToPlayerName;
    private int gameId;
}
