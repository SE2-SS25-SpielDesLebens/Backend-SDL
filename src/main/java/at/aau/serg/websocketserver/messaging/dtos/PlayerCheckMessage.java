package at.aau.serg.websocketserver.messaging.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerCheckMessage {
    private String playerName;
    private boolean wasSuccessful;
}
