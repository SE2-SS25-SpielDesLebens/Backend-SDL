package at.aau.serg.websocketserver.messaging.dtos;

import lombok.Data;

@Data
public class StompMessage {
        private String playerName;
        private String action;
        private String messageText;
}
