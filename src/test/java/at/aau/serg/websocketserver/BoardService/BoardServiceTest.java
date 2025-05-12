package at.aau.serg.websocketserver.BoardService;

import at.aau.serg.websocketserver.Player.Player;
import at.aau.serg.websocketserver.Player.PlayerService;
import at.aau.serg.websocketserver.fieldlogic.BoardService;
import at.aau.serg.websocketserver.fieldlogic.FieldType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BoardServiceTest {

    private PlayerService playerService;
    private BoardService boardService;
    private Player player;

    @BeforeEach
    void setUp() {
        playerService = new PlayerService();
        boardService = new BoardService(playerService);

        // Spieler hinzufügen
        playerService.addPlayer("testPlayer");
        player = playerService.getPlayerById("testPlayer").orElseThrow();
        player.setSalary(3000);
        player.setMoney(25000);
    }

    @Test
    void testHandlePayday() {
        String result = boardService.handleFieldEvent("testPlayer", FieldType.PAYDAY);
        assertEquals("💵 Zahltag! Gehalt von 3000€ erhalten.", result);
        assertEquals(28000, player.getMoney());
    }

    @Test
    void testHandleInvestmentSuccess() {
        String result = boardService.handleFieldEvent("testPlayer", FieldType.INVESTMENT);
        assertEquals("📈 20.000€ investiert.", result);
        assertEquals(5000, player.getMoney());
        assertEquals(20000, player.getInvestments());
    }

    @Test
    void testHandleInvestmentFailsWithLowMoney() {
        player.setMoney(1000);
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                boardService.handleFieldEvent("testPlayer", FieldType.INVESTMENT));
        assertTrue(ex.getMessage().contains("Nicht genug Geld"));
    }

    @Test
    void testHandleFamily() {
        player.setChildrenCount(0);
        String result = boardService.handleFieldEvent("testPlayer", FieldType.FAMILY);
        assertEquals("👶 Ein Kind wurde zur Familie hinzugefügt!", result);
        assertEquals(1, player.getChildren());
    }

    @Test
    void testHandleMarriage() {
        String result = boardService.handleFieldEvent("testPlayer", FieldType.STOP_MARRIAGE);
        assertEquals("💍 Spieler ist jetzt verheiratet.", result);
        assertTrue(player.isMarried());
    }

    @Test
    void testHandleRetirement() {
        String result = boardService.handleFieldEvent("testPlayer", FieldType.RETIREMENT);
        assertEquals("🪑 Spieler ist nun im Ruhestand.", result);
        assertTrue(player.isRetired());
        assertFalse(player.isActive());
    }

    @Test
    void testHandleUnknownField() {
        String result = boardService.handleFieldEvent("testPlayer", FieldType.NEUTRAL);
        assertEquals("Kein spezieller Effekt für dieses Feld.", result);
    }
}
