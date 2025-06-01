package at.aau.serg.websocketserver.player;

import lombok.Getter;

import java.util.*;

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
     * Erstellt einen neuen Spieler, falls noch nicht vorhanden.
     * Gibt immer die aktuelle Instanz zurück.
     */
    public Player createPlayerIfNotExists(String id) {
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
     * Gibt alle registrierten Spieler zurück.
     */
    public List<Player> getAllPlayers() {
        return new ArrayList<>(players.values());
    }

    /**
     * Zählt, wie viele Spieler aktuell registriert sind.
     */
    public int getRegisteredPlayerCount() {
        return players.size();
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

    /**
     * Gibt zurück, ob ein Spieler aktiv ist (falls gesetzt).
     */
    public boolean isPlayerActive(String playerId) {
        Player p = players.get(playerId);
        return p != null && p.isActive();
    }

}
