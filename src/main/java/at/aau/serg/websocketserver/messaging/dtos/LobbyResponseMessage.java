package at.aau.serg.websocketserver.messaging.dtos;

public class LobbyResponseMessage {
    private final String playerName;
    private final String lobbyID;

    public LobbyResponseMessage(String lobbyID, String playerName){
        this.lobbyID = lobbyID;
        this.playerName = playerName;
    }
}
