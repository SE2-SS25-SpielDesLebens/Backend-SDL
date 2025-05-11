package at.aau.serg.websocketserver.lobby;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//Klasse speichert Spieler in einer Lobby, reine Datenverwaltung
public class Lobby {
    @Getter
    @Setter
    private String id;

    private final ArrayList<Player> players = new ArrayList<>();
    public static final int maxPlayers = 4;

    @Setter
    @Getter
    boolean isStarted = false;

    public Lobby(String id, Player player){
        this.id = id;
        players.add(player);
    }

    public synchronized boolean addPlayer(Player player){
        if(players.size() >= maxPlayers){
            return false;
        }
        return players.add(player);
    }

    public synchronized boolean removePlayer(Player player){
        return players.remove(player);
    }

    //gibt nicht veränderbare Liste zurück, safety feature
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
