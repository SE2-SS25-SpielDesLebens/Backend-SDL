package at.aau.serg.websocketserver.lobby;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LobbyTest {
    private Player host;
    private Lobby lobby;

    @BeforeEach
    void setUp(){
        host = new Player();
        lobby = new Lobby("LOBBY1", host);
    }

    @Test
    void testLobbyInitialization(){
        assertEquals("LOBBY1", lobby.getId());
        assertEquals(1, lobby.getPlayers().size());
        assertEquals(host, lobby.getPlayers().get(0));
    }

    @Test
    void testAddPlayerSuccess(){
        Player p2 = new Player();
        boolean added = lobby.addPlayer(p2);
        assertTrue(added);
        assertEquals(2, lobby.getPlayers().size());
    }

    @Test
    void testAddPlayerFailsWhenFull(){
        for(int i = 0; i<=3; i++){
            lobby.addPlayer(new Player());
        }
        //jetzt 4 Spieler vorhanden
        boolean result = lobby.addPlayer(new Player());
        assertFalse(result);
        assertEquals(4, lobby.getPlayers().size());
    }

    @Test
    void testRemovePlayer(){
        boolean removed = lobby.removePlayer(host);
        assertTrue(removed);
        assertEquals(0, lobby.getPlayers().size());
    }

    @Test
    void testIsEmpty(){
        assertFalse(lobby.isEmpty());
        lobby.removePlayer(host);
        assertTrue(lobby.isEmpty());
    }

    @Test
    void testIsFull(){
        assertFalse(lobby.isFull());
        for(int i = 0; i<=3; i++){
            lobby.addPlayer(new Player());
        }
        assertTrue(lobby.isFull());
    }
}
