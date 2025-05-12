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
    private final String playerId = "testPlayer";

    @BeforeEach
    void setUp() {
        playerService = new PlayerService();
        boardService = new BoardService(playerService);

        playerService.addPlayer(playerId);
        Player player = playerService.getPlayerById(playerId).orElseThrow();
        player.setSalary(3000);
        player.setMoney(25000);
    }

    @Test
    void testHandlePayday_addsSalaryToMoney() {
        Player player = playerService.getPlayerById(playerId).orElseThrow();
        int before = player.getMoney();

        String result = boardService.handleFieldEvent(playerId, FieldType.PAYDAY);

        assertEquals("💵 Zahltag! Gehalt von 3000€ erhalten.", result);
        assertEquals(before + 3000, player.getMoney());
    }

    @Test
    void testHandleInvestment_success() {
        String result = boardService.handleFieldEvent(playerId, FieldType.INVESTMENT);
        assertEquals("📈 20.000€ investiert.", result);

        Player player = playerService.getPlayerById(playerId).orElseThrow();
        assertEquals(5000, player.getMoney());
        assertEquals(20000, player.getInvestments());
    }

    @Test
    void testHandleInvestment_fails_whenTooLittleMoney() {
        Player player = playerService.getPlayerById(playerId).orElseThrow();
        player.setMoney(500);  // nicht genug für Investition

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                boardService.handleFieldEvent(playerId, FieldType.INVESTMENT));

        assertTrue(ex.getMessage().contains("Nicht genug Geld"));
    }

    @Test
    void testHandleFamily_addsChild() {
        Player player = playerService.getPlayerById(playerId).orElseThrow();
        player.setChildrenCount(0);

        String result = boardService.handleFieldEvent(playerId, FieldType.STOP_FAMILY);

        assertEquals("👶 Ein Kind wurde zur Familie hinzugefügt!", result);
        assertEquals(1, player.getChildren());
    }

    @Test
    void testHandleFamily_tooManyChildren_throws() {
        Player player = playerService.getPlayerById(playerId).orElseThrow();
        player.setChildrenCount(4); // Maximum

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                boardService.handleFieldEvent(playerId, FieldType.STOP_FAMILY));

        assertTrue(ex.getMessage().contains("maximal 4 Kinder"));
    }

    @Test
    void testHandleMarriage_success() {
        Player player = playerService.getPlayerById(playerId).orElseThrow();
        player.setMarried(false);

        String result = boardService.handleFieldEvent(playerId, FieldType.STOP_MARRIAGE);

        assertEquals("💍 Spieler ist jetzt verheiratet.", result);
        assertTrue(player.isMarried());
    }

    @Test
    void testHandleMarriage_alreadyMarried_throws() {
        Player player = playerService.getPlayerById(playerId).orElseThrow();
        player.setMarried(true);

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                boardService.handleFieldEvent(playerId, FieldType.STOP_MARRIAGE));

        assertTrue(ex.getMessage().contains("bereits verheiratet"));
    }

    @Test
    void testHandleRetirement_setsRetiredAndInactive() {
        Player player = playerService.getPlayerById(playerId).orElseThrow();
        player.setActive(true);

        String result = boardService.handleFieldEvent(playerId, FieldType.STOP_RETIREMENT);

        assertEquals("🪑 Spieler ist nun im Ruhestand.", result);
        assertTrue(player.isRetired());
        assertFalse(player.isActive());
    }

    @Test
    void testHandleUnknownField_returnsDefaultMessage() {
        String result = boardService.handleFieldEvent(playerId, FieldType.NEUTRAL);
        assertEquals("Kein spezieller Effekt für dieses Feld.", result);
    }

    @Test
    void testHandleAction_returnsPlaceholderMessage() {
        String result = boardService.handleFieldEvent(playerId, FieldType.ACTION);
        assertEquals("🎲 Aktionskarte gezogen (noch nicht implementiert).", result);
    }

    @Test
    void testHandleHouse_returnsPlaceholderMessage() {
        String result = boardService.handleFieldEvent(playerId, FieldType.HOUSE);
        assertEquals("🏠 Hauskauf wird hier später implementiert.", result);
    }

    @Test
    void testPlayerNotFound_throwsException() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                boardService.handleFieldEvent("unknown", FieldType.PAYDAY));
        assertEquals("Spieler nicht gefunden.", ex.getMessage());
    }
}
