package at.aau.serg.websocketdemoserver.game;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PlayerService {
    private final List<Player> players = new ArrayList<>();

    public PlayerService() {
        Player player1 = new Player("Hans", 1,10000,0,0,0,"Bachelore", "Single");

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
}
