package at.aau.serg.websocketserver.messaging.dtos;

public class StompMessage {

        private String playerName;
        private String action;
        private String messageText;
        private String gameId; // ➕ NEU

        public StompMessage() {
        }

        public StompMessage(String playerName, String action, String messageText, String gameId) {
                this.playerName = playerName;
                this.action = action;
                this.messageText = messageText;
                this.gameId = gameId;
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

        public String getGameId() {
                return gameId;
        }

        public void setGameId(String gameId) {
                this.gameId = gameId;
        }
}
