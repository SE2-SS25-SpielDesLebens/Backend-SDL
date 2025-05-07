package at.aau.serg.websocketserver.websocket.broker;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PlayerService {
    private final List<Player> players = new ArrayList<>();
    private int nextId = 3;

    public PlayerService() {
        players.add(new Player("Hans", 1, 10000, 0, 0, 0, "Bachelor", "Single", "Kellner", 0, 0));
        players.add(new Player("Eva", 2, 15000, 0, 0, 1, "Master", "Verheiratet", "Koch", 0, 0));
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
                player.getHouseId()
        );
        players.add(newPlayer);
        System.out.println("Neuer Spieler hinzugefügt: " + newPlayer.getName() + " mit ID " + newPlayer.getId());
    }
}
