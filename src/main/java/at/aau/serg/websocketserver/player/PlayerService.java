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

    public static synchronized PlayerService getInstance(){
        if(playerService == null){
            playerService = new PlayerService();
        }
        return playerService;
    }

    public Player getPlayerById(String id) {
        return players.get(id);
    }

    //TODO: anpassen
    public boolean updatePlayer(String id, Player updatedPlayer) {
        /*for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getId().equals(id)) {
                players.put(i, updatedPlayer);
                return true;
            }
        }*/
        return false;
    }

    public void addPlayer(String id) {
        Player newPlayer = new Player(id);
        players.put(id, newPlayer);
        System.out.println("Neuer Spieler hinzugefügt: " + newPlayer.getId() + " mit ID " + newPlayer.getId());
    }

    public boolean addChildToPlayer(String playerId) {
        Player player = getPlayerById(playerId);

        if (player == null) {
            throw new IllegalArgumentException("Spieler mit ID " + playerId + " nicht gefunden.");
        }

        if (player.getChildren() >= 4) {
            throw new IllegalArgumentException("Ein Spieler darf maximal 4 Kinder haben.");
        }

        player.setChildrenCount(player.getChildren()+1);

        //updatePlayer(player.getId(), player);
        System.out.println("👶 Spieler " + player.getId() + " hat nun " + player.getChildren() + " Kind(er).");
        return true;
    }

    public boolean marryPlayer(String playerId) {
        Player player = getPlayerById(playerId);

        if (player==null) {
            throw new IllegalArgumentException("Spieler mit ID " + playerId + " nicht gefunden.");
        }

        if (player.isMarried()) {
            throw new IllegalArgumentException("Spieler ist bereits verheiratet.");
        }

        player.setMarried(true);

        updatePlayer(player.getId(), player);
        System.out.println("💍 Spieler " + player.getId() + " ist jetzt verheiratet.");
        return true;
    }

    public boolean investForPlayer(String playerId) {
        Player player = getPlayerById(playerId);
        if (player == null) {
            throw new IllegalArgumentException("Spieler nicht gefunden.");
        }


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

