package at.aau.serg.websocketserver.messaging.dtos;

public class StompMessage {

        private String playerName;
        private String action;
        private String messageText;

        // No-Args-Konstruktor (für Jackson, Deserialisierung etc.)
        public StompMessage() {
        }

        // All-Args-Konstruktor
        public StompMessage(String playerName, String action, String messageText) {
                this.playerName = playerName;
                this.action = action;
                this.messageText = messageText;
        }

        public String getPlayerName() {
                return playerName;
        }

        public void setPlayerName(String playerName) {
                this.playerName = playerName;
        }

        public String getAction() {
                return action;
        }

        public void setAction(String action) {
                this.action = action;
        }

        public String getMessageText() {
                return messageText;
        }

        public void setMessageText(String messageText) {
                this.messageText = messageText;
        }
}
