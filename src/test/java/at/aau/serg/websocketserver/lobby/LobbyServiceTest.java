package at.aau.serg.websocketserver.lobby;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import at.aau.serg.websocketserver.player.*;
import static org.junit.jupiter.api.Assertions.*;

public class LobbyServiceTest {
    private LobbyService service;

    @BeforeEach
    void setUp(){
        service = LobbyService.getInstance();
    }

    @Test
    void testCreateLobby(){
        Player host = new Player("player1");
        Lobby lobby = service.createLobby(host);

        assertNotNull(lobby);
        assertEquals(1, lobby.getPlayers().size());
        assertEquals(host, lobby.getPlayers().get(0));
    }

    @Test
    void testJoinLobby(){
        Player host = new Player("player1");
        Lobby lobby = service.createLobby(host);

        Player p2 = new Player("player2");
        service.joinLobby(lobby.getId(), p2);

        assertEquals(2, lobby.getPlayers().size());
        assertTrue(lobby.getPlayers().contains(p2));
    }

    @Test
    void testJoinLobby2(){
        Player host = new Player("player1");
        Lobby lobby = service.createLobby(host);
        for(int i = 0; i<3; i++) {
            service.joinLobby(lobby.getId(), new Player("player " + i + " "));
        }
        assertThrows(IllegalStateException.class, () -> service.joinLobby(lobby.getId(), new Player("player")));
    }

    @Test
    void testLeaveLobby(){
        Player host = new Player("player1");
        Lobby lobby = service.createLobby(host);
        service.leaveLobby(lobby.getId(), host);

        assertEquals(0, lobby.getPlayers().size());
    }

    @Test
    void testLeaveLobbyException(){
        Player host = new Player("player1");
        Lobby lobby = service.createLobby(host);
        assertThrows(IllegalArgumentException.class, ()-> service.leaveLobby("invalidID", host));
        assertThrows(IllegalStateException.class, ()-> service.leaveLobby(lobby.getId(), new Player("player2")));
    }

    @Test
    void testDeleteLobby(){
        Player host = new Player("player1");
        Lobby lobby = service.createLobby(host);
        String id = lobby.getId();

        service.deleteLobby(id);
        assertThrows(IllegalArgumentException.class, ()-> service.joinLobby(id, new Player("player2")));
    }

    @Test
    void testUniqueIdGeneration(){
        Player host1 = new Player("player1");
        Player host2 = new Player("player2");

        Lobby l1 = service.createLobby(host1);
        Lobby l2 = service.createLobby(host2);

        assertNotEquals(l1.getId(), l2.getId());
    }

    @Test
    void testCleanUpLobby(){
        Player host = new Player("player1");
        Lobby lobby = service.createLobby(host);
        String id = lobby.getId();

        assertNotNull(service.getLobby(id));
        service.cleanupLobby(id);
        assertNull(service.getLobby(id));
    }

    @AfterEach
    void tearDown(){

    }
}
