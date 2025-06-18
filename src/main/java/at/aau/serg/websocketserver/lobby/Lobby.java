package at.aau.serg.websocketserver.lobby;

import at.aau.serg.websocketserver.game.GameLogic;
import lombok.Getter;
import lombok.Setter;


import at.aau.serg.websocketserver.player.*;

import java.util.*;

@Getter
public class Lobby {
    @Setter
    private String id;

    @Setter
    private GameLogic gameLogic;

    private final ArrayList<Player> players = new ArrayList<>();
    public static final int maxPlayers = 4;
    public final Map<String, Player> playerMap = new HashMap<>();

    @Setter
    boolean isStarted = false;

    public Lobby(String id, Player player){
        this.id = id;
        playerMap.put(player.getId(), player);
        players.add(player);
    }

    public synchronized boolean addPlayer(Player player){
        if(players.size() >= maxPlayers){
            return false;
        }
        playerMap.put(player.getId(), player);
        return players.add(player);
    }

    public synchronized boolean removePlayer(Player player){
        return players.remove(player);
    }

    public synchronized List<Player> getPlayers(){
        return Collections.unmodifiableList(players);
    }

    public boolean isFull(){
        return players.size()>=maxPlayers;
    }

    public boolean isEmpty(){
        return players.isEmpty();
    }

}
