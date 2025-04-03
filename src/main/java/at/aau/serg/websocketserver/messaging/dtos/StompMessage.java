package at.aau.serg.websocketserver.messaging.dtos;

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

        public String getAction() {
                return action;
        }

        public void setAction(String action) {
                this.action = action;
        }

        public String getPlayerName() {
                return playerName;
        }

        public void setPlayerName(String playerName) {
                this.playerName = playerName;
        }

        public String getMessageText() {
                return messageText;
        }

        public void setMessageText(String messageText) {
                this.messageText = messageText;
        }
}
