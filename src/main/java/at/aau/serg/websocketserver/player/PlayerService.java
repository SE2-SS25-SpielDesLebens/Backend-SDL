package at.aau.serg.websocketserver.player;

import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Getter
public class PlayerService {
    private final Map<String, Player> players;
    private static PlayerService playerService;

    private PlayerService() {
        players = new HashMap<>();
    }

    public static synchronized PlayerService getInstance() {
        if (playerService == null) {
            playerService = new PlayerService();
        }
        return playerService;
    }

    public Player getPlayerById(String id) {
        return players.get(id);
    }

    public Player addPlayer(String id) {
        Player newPlayer = new Player(id);
        players.put(id, newPlayer);
        System.out.println("Neuer Spieler hinzugefügt: " + newPlayer.getId());
        return newPlayer;
    }

    public boolean incrementCounterForPlayer(String playerId, String eventType) {
        Player player = getPlayerById(playerId);

        if (player == null) {
            throw new IllegalArgumentException("Spieler mit ID " + playerId + " nicht gefunden.");
        }

        switch (eventType.toLowerCase()) {
            case "heirat":
                if (player.isMarried()) {
                    throw new IllegalArgumentException("💍 Spieler ist bereits verheiratet.");
                }
                player.marry();
                System.out.println("💍 Spieler " + player.getId() + " ist jetzt verheiratet.");
                break;

            case "kind":
                if (player.canAddPassengers(1)) {
                    throw new IllegalArgumentException("🚗 Kein Platz mehr im Auto für weitere Kinder.");
                }
                player.addPassenger(1);
                System.out.println("👶 Spieler " + player.getId() + " hat ein Kind. Plätze im Auto: " + player.getAutoPassengers());
                break;

            case "zwilling":
                if (player.canAddPassengers(2)) {
                    throw new IllegalArgumentException("🚗 Kein Platz mehr im Auto für Zwillinge.");
                }
                player.addPassenger(2);
                System.out.println("👶👶 Spieler " + player.getId() + " hat Zwillinge. Plätze im Auto: " + player.getAutoPassengers());
                break;

            case "freund":
                if (player.canAddPassengers(1)) {
                    throw new IllegalArgumentException("🚗 Kein Platz mehr im Auto für einen Freund.");
                }
                player.addPassenger(1);
                System.out.println("🤝 Spieler " + player.getId() + " hat einen Freund. Plätze im Auto: " + player.getAutoPassengers());
                break;

            case "tier":
                if (player.canAddPassengers(1)) {
                    throw new IllegalArgumentException("🚗 Kein Platz mehr im Auto für ein Haustier.");
                }
                player.addPassenger(1);
                System.out.println("🐶 Spieler " + player.getId() + " hat ein Haustier. Plätze im Auto: " + player.getAutoPassengers());
                break;

            default:
                throw new IllegalArgumentException("❌ Unbekanntes Ereignis: " + eventType);
        }

        return true;
    }

}
