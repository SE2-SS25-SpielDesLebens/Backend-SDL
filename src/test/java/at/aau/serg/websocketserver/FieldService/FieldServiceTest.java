package at.aau.serg.websocketserver.FieldService;

import at.aau.serg.websocketserver.Player.Player;
import at.aau.serg.websocketserver.Player.PlayerService;
import at.aau.serg.websocketserver.board.BoardService;
import at.aau.serg.websocketserver.board.Field;
import at.aau.serg.websocketserver.fieldlogic.FieldService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FieldServiceTest {

    private FieldService fieldService;
    private PlayerService playerService;
    private BoardService boardService;

    @BeforeEach
    void setUp() {
        playerService = new PlayerService();
        boardService = new BoardService();
        fieldService = new FieldService(playerService, boardService);

        // Setup: Spieler mit ID "1" auf Feld 0 setzen
        playerService.addPlayer("1");
        boardService.addPlayer(1, 0);
    }

    @Test
    void testTriggerPaydayFieldEvent() {
        boardService.setPlayerPosition(1, 1); // ZAHLTAG
        Player player = playerService.getPlayerById("1").orElseThrow();
        player.setSalary(3000);
        String result = fieldService.triggerCurrentFieldEvent(1);
        assertTrue(result.contains("Zahltag"));
        assertEquals(3000, player.getMoney());
    }

    @Test
    void testTriggerActionField() {
        boardService.setPlayerPosition(1, 2); // AKTION
        String result = fieldService.triggerCurrentFieldEvent(1);
        assertTrue(result.contains("Aktionskarte"));
    }

    @Test
    void testTriggerInvestmentFieldSuccess() {
        boardService.setPlayerPosition(1, 3); // ANLAGE
        Player player = playerService.getPlayerById("1").orElseThrow();
        player.setMoney(10000);
        String result = fieldService.triggerCurrentFieldEvent(1);
        assertTrue(result.contains("investiert"));
        assertEquals(10000, player.getMoney());
    }

    @Test
    void testTriggerInvestmentFieldFails() {
        boardService.setPlayerPosition(1, 3); // ANLAGE
        Player player = playerService.getPlayerById("1").orElseThrow();
        player.setMoney(0);
        String result = fieldService.triggerCurrentFieldEvent(1);
        assertTrue(result.contains("fehlgeschlagen"));
    }

    @Test
    void testTriggerFamilyField() {
        boardService.setPlayerPosition(1, 5); // FREUND → STOP_FAMILY
        Player player = playerService.getPlayerById("1").orElseThrow();
        player.setChildrenCount(2);
        Field friendField = boardService.getFieldByIndex(5);
        friendField.addNextField(5); // Damit der Feldtyp erhalten bleibt
        friendField.addNextField(5);
        friendField.addNextField(5);
        friendField.addNextField(5);
        String result = fieldService.handleFamily(player);
        assertTrue(result.contains("Kind"));
    }

    @Test
    void testTriggerMarriageField() {
        boardService.setPlayerPosition(1, 16); // HEIRAT
        Player player = playerService.getPlayerById("1").orElseThrow();
        player.setMarried(true);
        String result = fieldService.triggerCurrentFieldEvent(1);
        assertTrue(result.contains("verheiratet"));
    }

    @Test
    void testTriggerRetirementField() {
        boardService.setPlayerPosition(1, 8); // beliebiges Feld
        Player player = playerService.getPlayerById("1").orElseThrow();
        String result = fieldService.handleRetirement(player);
        assertTrue(result.contains("Ruhestand"));
        assertTrue(player.isRetired());
    }

    @Test
    void testTriggerMidlifeCrisis() {
        String result = fieldService.handleMidlifecrisis(new Player("test"));
        assertTrue(result.contains("MidlifeCrisis"));
    }

    @Test
    void testTriggerExam() {
        String result = fieldService.handleExam(new Player("test"));
        assertTrue(result.contains("Jobkarten"));
    }

    @Test
    void testUnknownFieldType() {
        boardService.setPlayerPosition(1, 0); // Feldtyp: STARTNORMAL
        String result = fieldService.triggerCurrentFieldEvent(1);
        assertTrue(result.contains("Unbekannter Feldtyp") || result.contains("❌"));
    }
}
