package at.aau.serg.websocketdemoserver.messaging.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor // Wichtig für Deserialisierung durch Jackson!
public class StompMessage {
        private String playerName;
        private String action;
        private String messageText;
}
