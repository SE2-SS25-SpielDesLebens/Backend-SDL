package at.aau.serg.websocketserver.player;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class PlayerService {

    private static PlayerService instance;

    private final Map<String, Player> players = new HashMap<>();

    // privater Konstruktor – verhindert direkte Instanziierung
    private PlayerService() {
    }

    /**
     * Zugriff auf die Singleton-Instanz.
     */
    public static synchronized PlayerService getInstance() {
        if (instance == null) {
            instance = new PlayerService();
        }
        return instance;
    }

    /**
     * Fügt einen neuen Spieler hinzu, wenn er noch nicht existiert.
     */
    public Player addPlayer(String id) {
        return players.computeIfAbsent(id, pid -> {
            Player newPlayer = new Player(pid);
            System.out.println("🧍 Neuer Spieler registriert: " + pid);
            return newPlayer;
        });
    }

    /**
     * Gibt einen Spieler anhand der ID zurück.
     */
    public Player getPlayerById(String id) {
        return players.get(id);
    }

    /**
     * Ereignis (Kind, Heirat, Haustier...) an einen Spieler weiterleiten.
     */
    public boolean incrementCounterForPlayer(String playerId, String eventType) {
        Player player = getPlayerById(playerId);
        if (player == null) {
            throw new IllegalArgumentException("Spieler mit ID " + playerId + " nicht gefunden.");
        }
        player.handleEvent(eventType);
        return true;
    }

    /**
     * Entfernt einen Spieler aus dem Service – zb. beim Verlassen der Lobby.
     */
    public void removePlayer(String playerId) {
        players.remove(playerId);
        System.out.println("🚪 Spieler entfernt: " + playerId);
    }

    /**
     * Leert alle Spieler – zb. beim Neustart des Servers.
     */
    public void clearAll() {
        players.clear();
        System.out.println("🧹 Alle Spieler wurden entfernt.");
    }

    /**
     * Prüft, ob ein Spieler bereits registriert ist.
     */
    public boolean isPlayerRegistered(String playerId) {
        return players.containsKey(playerId);
    }
}
