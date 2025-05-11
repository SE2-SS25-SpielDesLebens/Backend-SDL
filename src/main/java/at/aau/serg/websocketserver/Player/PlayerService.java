package at.aau.serg.websocketserver.Player;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PlayerService {
    private final List<Player> players = new ArrayList<>();
    private int nextId = 3;

    public PlayerService() {
        players.add(new Player("Hans", 1, 10000, 0, 0, 0, "Bachelor", "Single", "Kellner", 0, 0, 0));
        players.add(new Player("Eva", 2, 15000, 0, 0, 1, "Master", "Verheiratet", "Koch", 0, 0, 0));
    }

    public List<Player> getAllPlayers() {
        return players;
    }

    public Optional<Player> getPlayerById(int id) {
        return players.stream().filter(player -> player.getId() == id).findFirst();
    }

    public boolean updatePlayer(int id, Player updatedPlayer) {
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getId() == id) {
                players.set(i, updatedPlayer);
                return true;
            }
        }
        return false;
    }

    public void addPlayer(Player player) {
        Player newPlayer = new Player(
                player.getName(),
                nextId++,
                player.getMoney(),
                player.getInvestments(),
                player.getSalary(),
                player.getChildren(),
                player.getEducation(),
                player.getRelationship(),
                player.getCareer(),
                player.getJobId(),
                player.getHouseId(),
                player.getFieldID()
        );
        players.add(newPlayer);
        System.out.println("Neuer Spieler hinzugefügt: " + newPlayer.getName() + " mit ID " + newPlayer.getId());
    }

    public boolean addChildToPlayer(int playerId) {
        Optional<Player> optionalPlayer = getPlayerById(playerId);

        if (optionalPlayer.isEmpty()) {
            throw new IllegalArgumentException("Spieler mit ID " + playerId + " nicht gefunden.");
        }

        Player player = optionalPlayer.get();

        if (player.getChildren() >= 4) {
            throw new IllegalArgumentException("Ein Spieler darf maximal 4 Kinder haben.");
        }

        Player updatedPlayer = new Player(
                player.getName(),
                player.getId(),
                player.getMoney(),
                player.getInvestments(),
                player.getSalary(),
                player.getChildren() + 1,
                player.getEducation(),
                player.getRelationship(),
                player.getCareer(),
                player.getJobId(),
                player.getHouseId(),
                player.getFieldID()
        );

        updatePlayer(player.getId(), updatedPlayer);
        System.out.println("👶 Spieler " + player.getName() + " hat nun " + updatedPlayer.getChildren() + " Kind(er).");
        return true;
    }

    public boolean marryPlayer(int playerId) {
        Optional<Player> optionalPlayer = getPlayerById(playerId);

        if (optionalPlayer.isEmpty()) {
            throw new IllegalArgumentException("Spieler mit ID " + playerId + " nicht gefunden.");
        }

        Player player = optionalPlayer.get();

        if ("Verheiratet".equalsIgnoreCase(player.getRelationship())) {
            throw new IllegalArgumentException("Spieler ist bereits verheiratet.");
        }

        Player updatedPlayer = new Player(
                player.getName(),
                player.getId(),
                player.getMoney(),
                player.getInvestments(),
                player.getSalary(),
                player.getChildren(),
                player.getEducation(),
                "Verheiratet",
                player.getCareer(),
                player.getJobId(),
                player.getHouseId(),
                player.getFieldID()
        );

        updatePlayer(player.getId(), updatedPlayer);
        System.out.println("💍 Spieler " + player.getName() + " ist jetzt verheiratet.");
        return true;
    }

    public boolean investForPlayer(int playerId) {
        Optional<Player> optionalPlayer = getPlayerById(playerId);
        if (optionalPlayer.isEmpty()) {
            throw new IllegalArgumentException("Spieler nicht gefunden.");
        }

        Player player = optionalPlayer.get();

        int investAmount = 20000;
        if (player.getMoney() < investAmount) {
            throw new IllegalArgumentException("Nicht genug Geld für eine Investition.");
        }

        Player updatedPlayer = new Player(
                player.getName(),
                player.getId(),
                player.getMoney() - investAmount,
                player.getInvestments() + investAmount,
                player.getSalary(),
                player.getChildren(),
                player.getEducation(),
                player.getRelationship(),
                player.getCareer(),
                player.getJobId(),
                player.getHouseId(),
                player.getFieldID()
        );

        updatePlayer(player.getId(), updatedPlayer);
        System.out.println("📈 Spieler " + player.getName() + " hat 20.000€ investiert.");
        return true;
    }

}

