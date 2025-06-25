package at.aau.serg.websocketserver.lobby;

import lombok.Getter;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import at.aau.serg.websocketserver.player.*;

@Getter
public class LobbyService {
    private final Map<String, Lobby> lobbies;
    private static LobbyService lobbyService;

    // Zugriff auf PlayerService (Singleton verwenden)
    private final PlayerService playerService = PlayerService.getInstance();

    //private weil als Singleton implementiert, es gibt nur einen LobbyService
    private LobbyService() {
        lobbies = new ConcurrentHashMap<>();
    }

    //gibt die einzige Instanz vom LobbyService zurück
    public static synchronized LobbyService getInstance() {
        if (lobbyService == null) {
            lobbyService = new LobbyService();
        }
        return lobbyService;
    }

    //erstellt neue Lobby, erstellender Spieler als Parameter wird automatisch hinzugefügt (und ist Host)
    public Lobby createLobby(Player player) {
        String id = generateUniqueID();
        Lobby lobby = new Lobby(id, player);
        lobbies.put(id, lobby);
        return lobby;
    }

    //lobby id muss eindeutig sein, daher mit Überprüfung
    private String generateUniqueID() {
        String id;

        //Endlosschleife verhindern, daher maxAttempts
        int maxAttempts = 1000;
        int attemps = 0;

        do {
            id = lobbyService.generateLobbyID();
            attemps++;
            if (attemps >= maxAttempts) {
                throw new IllegalArgumentException("Konnte keine eindeutige Lobby-ID finden.");
            }
        } while (lobbyService.getLobby(id) != null);
        return id;
    }

    //generiert user-friendly ID
    private String generateLobbyID() {
        String charPool  = "ABCDEFGHIJKLMNPQRSTUVWXYZ123456789";
        int idLength = 6;
        SecureRandom random = new SecureRandom();

        StringBuilder id = new StringBuilder(idLength);
        for (int i = 0; i < idLength; i++) {
            int index = random.nextInt(charPool.length());
            id.append(charPool.charAt(index));
        }
        return id.toString();
    }

    public void joinLobby(String id, Player player) {
        Lobby lobby = getLobby(id);
        if (lobby == null) {
            throw new IllegalArgumentException("Lobby mit ID " + id + " existiert nicht.");
        }


        if (!lobby.addPlayer(player)) {
            throw new IllegalStateException("Player " + player + " konnte nicht hinzugefügt werden");
        }
    }

    public void leaveLobby(String id, Player player) {
        Lobby lobby = getLobby(id);
        if (lobby == null) {
            throw new IllegalArgumentException("Lobby mit ID " + id + " existiert nicht.");
        }
        if (!lobby.removePlayer(player)) {
            throw new IllegalStateException("Player " + player + " ist nicht in der Lobby.");
        }
    }

    public void deleteLobby(String id) {
        lobbies.remove(id);
    }

    //am Ende des Spiels aufrufbar, um sauber Lobby zu entfernen
    public void cleanupLobby(String id) {
        Lobby lobby = getLobby(id);
        if (lobby != null) {
            lobbies.remove(id);
        }
    }

    public Lobby getLobby(String id) {
        return lobbies.get(id);
    }

    public boolean isLobbyRegistered(String id) { return lobbies.containsKey(id); }
}
