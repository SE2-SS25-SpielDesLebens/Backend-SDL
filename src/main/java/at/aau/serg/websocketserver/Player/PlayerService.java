package at.aau.serg.websocketserver.Player;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PlayerService {
    private final List<Player> players = new ArrayList<>();

    public PlayerService() {
        players.add(new Player("Player1"));
        players.add(new Player("Player2"));
    }

    public List<Player> getAllPlayers() {
        return players;
    }

    public Optional<Player> getPlayerById(String id) {
        return players.stream().filter(player -> player.getId().equals(id)).findFirst();
    }

    public boolean updatePlayer(String id, Player updatedPlayer) {
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getId().equals(id)) {
                players.set(i, updatedPlayer);
                return true;
            }
        }
        return false;
    }

    public Player addPlayer(String name) {
        Player newPlayer = new Player(name); // Name als ID (besser: UUID später)
        players.add(newPlayer);
        System.out.println("Neuer Spieler hinzugefügt: " + newPlayer.getId());
        return newPlayer;
    }


    public boolean addChildToPlayer(String playerId) {
        Optional<Player> optionalPlayer = getPlayerById(playerId);

        if (optionalPlayer.isEmpty()) {
            throw new IllegalArgumentException("Spieler mit ID " + playerId + " nicht gefunden.");
        }

        Player player = optionalPlayer.get();

        if (player.getChildren() >= 4) {
            throw new IllegalArgumentException("Ein Spieler darf maximal 4 Kinder haben.");
        }

        player.setChildrenCount(player.getChildren()+1);

        updatePlayer(player.getId(), player);
        System.out.println("👶 Spieler " + player.getId() + " hat nun " + player.getChildren() + " Kind(er).");
        return true;
    }

    public boolean marryPlayer(String playerId) {
        Optional<Player> optionalPlayer = getPlayerById(playerId);

        if (optionalPlayer.isEmpty()) {
            throw new IllegalArgumentException("Spieler mit ID " + playerId + " nicht gefunden.");
        }

        Player player = optionalPlayer.get();

        if (player.isMarried()) {
            throw new IllegalArgumentException("Spieler ist bereits verheiratet.");
        }


        player.setMarried(true);

        updatePlayer(player.getId(), player);
        System.out.println("💍 Spieler " + player.getId() + " ist jetzt verheiratet.");
        return true;
    }

    public boolean investForPlayer(String playerId) {
        Optional<Player> optionalPlayer = getPlayerById(playerId);
        if (optionalPlayer.isEmpty()) {
            throw new IllegalArgumentException("Spieler nicht gefunden.");
        }

        Player player = optionalPlayer.get();

        int investAmount = 20000;
        if (player.getMoney() < investAmount) {
            throw new IllegalArgumentException("Nicht genug Geld für eine Investition.");
        }


        player.setMoney(player.getMoney() - investAmount);
        player.setInvestments(player.getInvestments() + investAmount);

        updatePlayer(player.getId(), player);
        System.out.println("📈 Spieler " + player.getId() + " hat 20.000€ investiert.");
        return true;
    }

}

