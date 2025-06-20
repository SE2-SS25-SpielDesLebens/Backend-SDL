package at.aau.serg.websocketserver.lobby;

import at.aau.serg.websocketserver.game.GameLogic;
import lombok.Getter;
import lombok.Setter;


import at.aau.serg.websocketserver.player.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//Klasse speichert Spieler in einer Lobby, reine Datenverwaltung
@Getter
public class Lobby {
    @Setter
    private String id;

    @Setter
    private GameLogic gameLogic;

    private final ArrayList<Player> players = new ArrayList<>();
    public static final int MAX_PLAYERS = 4;

    @Setter
    boolean isStarted = false;

    public Lobby(String id, Player player){
        this.id = id;
        players.add(player);
    }

    public synchronized boolean addPlayer(Player player){
        if(players.size() >= MAX_PLAYERS){
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
        return players.size()>= MAX_PLAYERS;
    }

    public boolean isEmpty(){
        return players.isEmpty();
    }

}
