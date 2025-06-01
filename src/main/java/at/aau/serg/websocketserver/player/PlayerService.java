package at.aau.serg.websocketserver.player;

import lombok.Getter;

import java.util.*;

/**
 * 💼 Zentrale Spielerverwaltung als Singleton.
 * Hält alle registrierten Spieler im Speicher.
 */
@Getter
public class PlayerService {

    // 🔒 Singleton-Instanz
    private static PlayerService instance;

    // 🗃️ Spieler-Map (Key = Spieler-ID)
    private final Map<String, Player> players = new HashMap<>();

    // ⛔ Privater Konstruktor
    private PlayerService() {}

    /**
     * 🧍 Zugriff auf die Singleton-Instanz
     */
    public static synchronized PlayerService getInstance() {
        if (instance == null) {
            instance = new PlayerService();
        }
        return instance;
    }

    // ───────────────────────────────────────────────
    // 🎮 SPIELER-MANAGEMENT
    // ───────────────────────────────────────────────

    /**
     * 🔁 Erstellt und registriert einen Spieler, falls noch nicht vorhanden.
     * Sollte **immer** zur Erstellung genutzt werden.
     */
    public Player createPlayerIfNotExists(String id) {
        return players.computeIfAbsent(id, pid -> {
            Player newPlayer = new Player(pid);
            System.out.println("🧍 Neuer Spieler registriert: " + pid);
            return newPlayer;
        });
    }

    /**
     * ❌ Entfernt einen Spieler anhand der ID.
     */
    public void removePlayer(String playerId) {
        players.remove(playerId);
        System.out.println("🚪 Spieler entfernt: " + playerId);
    }

    /**
     * 🧹 Entfernt **alle** registrierten Spieler.
     */
    public void clearAll() {
        players.clear();
        System.out.println("🧹 Alle Spieler wurden entfernt.");
    }

    /**
     * 🔎 Prüft, ob ein Spieler registriert ist.
     */
    public boolean isPlayerRegistered(String playerId) {
        return players.containsKey(playerId);
    }

    /**
     * 🔄 Aktualisiert einen existierenden Spieler vollständig.
     * Gibt true zurück, wenn erfolgreich.
     */
    public boolean updatePlayer(String id, Player updatedPlayer) {
        if (!players.containsKey(id)) return false;
        players.put(id, updatedPlayer);
        return true;
    }

    // ───────────────────────────────────────────────
    // 📊 ABFRAGEN / LISTEN
    // ───────────────────────────────────────────────

    /**
     * 📦 Gibt einen Spieler anhand der ID zurück.
     */
    public Player getPlayerById(String id) {
        return players.get(id);
    }

    /**
     * 📋 Gibt eine Liste aller registrierten Spieler zurück.
     */
    public List<Player> getAllPlayers() {
        return new ArrayList<>(players.values());
    }

    /**
     * 🧮 Gibt die Anzahl registrierter Spieler zurück.
     */
    public int getRegisteredPlayerCount() {
        return players.size();
    }

    /**
     * ✅ Prüft, ob ein Spieler aktiv ist.
     */
    public boolean isPlayerActive(String playerId) {
        Player player = players.get(playerId);
        return player != null && player.isActive();
    }
}
