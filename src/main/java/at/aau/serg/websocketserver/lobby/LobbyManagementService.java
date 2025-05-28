package at.aau.serg.websocketserver.lobby;

import at.aau.serg.websocketserver.messaging.dtos.LobbyRequestMessage;
import at.aau.serg.websocketserver.messaging.dtos.LobbyResponseMessage;
import at.aau.serg.websocketserver.messaging.dtos.OutputMessage;
import at.aau.serg.websocketserver.player.Player;
import at.aau.serg.websocketserver.player.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;

/**
 * Service zur Verwaltung der Lobby-Funktionalitäten.
 * Enthält Logik, die zuvor in WebSocketBrokerController war.
 */
@Service
public class LobbyManagementService {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final PlayerService playerService;
    private final LobbyService lobbyService;
    
    @Autowired
    public LobbyManagementService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
        this.playerService = PlayerService.getInstance();
        this.lobbyService = LobbyService.getInstance();
    }
    
    /**
     * Verarbeitet eine generelle Lobby-Anfrage.
     *
     * @param action Die auszuführende Aktion
     * @param gameId Die ID des Spiels
     * @param playerName Der Name des Spielers
     * @return Die Nachricht, die gesendet werden soll
     */
    public OutputMessage handleLobbyAction(String action, String gameId, String playerName) {
        String content;

        if (action == null) {
            content = "❌ Keine Aktion angegeben.";
        } else {
            switch (action) {
                case "createLobby":
                    content = "🆕 Lobby [" + gameId + "] von " + playerName + " erstellt.";
                    break;
                case "joinLobby":
                    content = "✅ " + playerName + " ist Lobby [" + gameId + "] beigetreten.";
                    break;
                default:
                    content = "Unbekannte Lobby-Aktion.";
                    break;
            }
        }

        System.out.println("[LOBBY] [" + gameId + "] " + playerName + ": " + content);
        
        return new OutputMessage(playerName, content, LocalDateTime.now().toString());
    }
    
    /**
     * Erstellt eine neue Lobby.
     *
     * @param request Die Anfrage mit den Lobby-Daten
     * @param principal Der aktuelle Principal (wird für die Authentifizierung verwendet)
     * @return Die erstellte Lobby-Antwort
     */
    public LobbyResponseMessage createLobby(LobbyRequestMessage request, Principal principal) {
        //Spieler sollte schon in PlayerService enthalten sein
        Lobby lobby = lobbyService.createLobby(playerService.getPlayerById(request.getPlayerName()));
        System.out.println("Lobbyid: " + lobby.getId() + " " + request.getPlayerName() + " " + principal.getName());
        
        return new LobbyResponseMessage(lobby.getId(), request.getPlayerName(), true, null);
    }
    
    /**
     * Lässt einen Spieler einer Lobby beitreten.
     *
     * @param lobbyId Die ID der Lobby
     * @param request Die Beitrittsanfrage
     * @return Die Antwort auf die Beitrittsanfrage
     */
    public LobbyResponseMessage joinLobby(String lobbyId, LobbyRequestMessage request) {
        try {
            Lobby lobby = lobbyService.getLobby(lobbyId);
            lobby.addPlayer(playerService.getPlayerById(request.getPlayerName()));
            System.out.println("Spieler" + request.getPlayerName() + " ist beigetreten");
            
            return new LobbyResponseMessage(lobbyId, request.getPlayerName(), true, 
                "Spieler " + request.getPlayerName() + " ist erfolgreich beigetreten");
        } catch (Exception e) {
            return new LobbyResponseMessage(lobbyId, request.getPlayerName(), false, e.getMessage());
        }
    }
    
    /**
     * Lässt einen Spieler eine Lobby verlassen.
     *
     * @param lobbyId Die ID der Lobby
     * @param request Die Austrittsanfrage
     */
    public void leaveLobby(String lobbyId, LobbyRequestMessage request) {
        Lobby lobby = lobbyService.getLobby(lobbyId);
        Player player = playerService.getPlayerById(request.getPlayerName());
        
        if (lobby != null && player != null) {
            lobby.removePlayer(player);
            System.out.println("Spieler " + request.getPlayerName() + " hat Lobby " + lobbyId + " verlassen");
        }
    }
}
