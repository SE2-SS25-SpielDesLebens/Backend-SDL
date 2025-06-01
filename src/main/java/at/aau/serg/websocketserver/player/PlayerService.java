package at.aau.serg.websocketserver.player;

import lombok.Getter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Getter
public class PlayerService {

    private static PlayerService instance;

    private final Map<String, Player> players = new HashMap<>();

    // 🧍 privater Konstruktor – verhindert direkte Instanziierung
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
     * Alternative Methode: Holt bestehenden Spieler oder erstellt ihn neu.
     */
    public Player createPlayerIfNotExists(String id) {
        return addPlayer(id);
    }

    /**
     * Gibt einen Spieler anhand der ID zurück.
     */
    public Player getPlayerById(String id) {
        return players.get(id);
    }

    /**
     * Gibt eine Liste aller registrierten Spieler zurück.
     */
    public Collection<Player> getAllPlayers() {
        return players.values();
    }

    /**
     * Entfernt einen Spieler aus dem Service – zB. beim Verlassen der Lobby.
     */
    public void removePlayer(String playerId) {
        players.remove(playerId);
        System.out.println("🚪 Spieler entfernt: " + playerId);
    }

    /**
     * Leert alle Spieler – zB. beim Neustart des Servers.
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

    /**
     * Aktualisiert einen existierenden Spieler vollständig.
     */
    public boolean updatePlayer(String id, Player updatedPlayer) {
        if (!players.containsKey(id)) {
            return false;
        }
        players.put(id, updatedPlayer);
        return true;
    }
}
