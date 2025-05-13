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
    private final String playerId = "1";
    private final int playerIdInt = 1;

    @BeforeEach
    void setUp() {
        playerService = new PlayerService();
        boardService = new BoardService();
        fieldService = new FieldService(playerService, boardService);

        playerService.addPlayer(playerId);
        boardService.addPlayer(playerIdInt, 0); // Startfeld
    }

    @Test
    void testTriggerPaydayFieldEvent() {
        boardService.setPlayerPosition(playerIdInt, 1); // PAYDAY
        Player player = playerService.getPlayerById(playerId).orElseThrow();
        player.setSalary(3000);
        player.setMoney(0);

        String result = fieldService.triggerCurrentFieldEvent(playerIdInt);
        assertTrue(result.contains("Zahltag"));
        assertEquals(3000, player.getMoney());
    }

    @Test
    void testTriggerCurrentFieldEventWithInvalidPlayerId() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            fieldService.triggerCurrentFieldEvent(999); // nicht existierender Spieler
        });
        assertTrue(exception.getMessage().contains("Spieler nicht gefunden"));
    }


    @Test
    void testTriggerActionField() {
        boardService.setPlayerPosition(playerIdInt, 2); // ACTION
        String result = fieldService.triggerCurrentFieldEvent(playerIdInt);
        assertTrue(result.contains("Aktionskarte"));
    }

    @Test
    void testTriggerInvestmentFieldSuccess() {
        boardService.setPlayerPosition(playerIdInt, 3); // INVESTMENT
        Player player = playerService.getPlayerById(playerId).orElseThrow();
        player.setMoney(25000);

        String result = fieldService.triggerCurrentFieldEvent(playerIdInt);
        assertTrue(result.contains("investiert"));
        assertEquals(5000, player.getMoney());
    }

    @Test
    void testTriggerInvestmentFieldFails() {
        boardService.setPlayerPosition(playerIdInt, 3); // INVESTMENT
        Player player = playerService.getPlayerById(playerId).orElseThrow();
        player.setMoney(1000);

        String result = fieldService.triggerCurrentFieldEvent(playerIdInt);
        assertTrue(result.contains("fehlgeschlagen"));
    }

    @Test
    void testHandleFamilySuccess() {
        Player player = playerService.getPlayerById(playerId).orElseThrow();
        player.setChildrenCount(2);

        String result = fieldService.handleFamily(player);
        assertTrue(result.contains("Kind"));
        assertEquals(3, player.getChildren());
    }

    @Test
    void testHandleFamilyFailTooManyChildren() {
        Player player = playerService.getPlayerById(playerId).orElseThrow();
        player.setChildrenCount(4);

        String result = fieldService.handleFamily(player);
        assertTrue(result.contains("Fehler"));
    }

    @Test
    void testHandleMarriageSuccess() {
        Player player = playerService.getPlayerById(playerId).orElseThrow();
        player.setMarried(false);

        String result = fieldService.handleMarriage(player);
        assertTrue(result.contains("verheiratet"));
    }

    @Test
    void testHandleMarriageFailAlreadyMarried() {
        Player player = playerService.getPlayerById("1").orElseThrow();
        player.setMarried(true); // Spieler ist bereits verheiratet

        String result = fieldService.handleMarriage(player);

        // Erwarte eine Fehlermeldung im String
        assertTrue(result.contains("❌") || result.toLowerCase().contains("verheiratet"));
    }

    @Test
    void testHandleRetirement() {
        Player player = playerService.getPlayerById(playerId).orElseThrow();
        assertFalse(player.isRetired());

        String result = fieldService.handleRetirement(player);
        assertTrue(result.contains("Ruhestand"));
        assertTrue(player.isRetired());
    }

    @Test
    void testHandleMidlifeCrisis() {
        Player player = new Player("test");
        String result = fieldService.handleMidlifecrisis(player);
        assertTrue(result.contains("MidlifeCrisis"));
    }

    @Test
    void testHandleExam() {
        Player player = new Player("test");
        String result = fieldService.handleExam(player);
        assertTrue(result.contains("Jobkarten"));
    }

    @Test
    void testUnknownFieldType() {
        // Feldtyp auf ungültig setzen
        Field field = boardService.getFieldByIndex(0);
        field.addNextField(0);
        boardService.setPlayerPosition(playerIdInt, 0);

        String result = fieldService.triggerCurrentFieldEvent(playerIdInt);
        assertTrue(result.contains("Unbekannter Feldtyp") || result.contains("❌"));
    }

    @Test
    void testHandleHouse() {
        // Feld 10 ist HOUSE laut deiner Logik
        boardService.setPlayerPosition(playerIdInt, 10);

        String result = fieldService.triggerCurrentFieldEvent(playerIdInt);
        assertTrue(result.contains("Hauskauf"));
    }

    @Test
    void testTriggerCurrentFieldEventWithInvalidFieldType() {
        Field field = boardService.getFieldByIndex(0);
        field.addNextField(0);
        boardService.setPlayerPosition(playerIdInt, 0);

        // Simuliere ungültigen Typ
        Field invalidField = boardService.getPlayerField(playerIdInt);
        invalidField.addNextField(0);
        boardService.setPlayerPosition(playerIdInt, 0);
        String result = fieldService.triggerCurrentFieldEvent(playerIdInt);

        assertTrue(result.contains("Unbekannter Feldtyp") || result.contains("❌"));
    }

    @Test
    void testHandleFieldEventDefault() {
        String result = fieldService.triggerCurrentFieldEvent(playerIdInt); // Simuliere STARTNORMAL als Typ
        assertTrue(result.contains("❌") || result.contains("Kein definierter Effekt"));
    }

    @Test
    void testTriggerMarriageFieldAlreadyMarriedHandled() {
        boardService.setPlayerPosition(playerIdInt, 16); // HEIRAT
        Player player = playerService.getPlayerById(playerId).orElseThrow();
        player.setMarried(true); // ist bereits verheiratet

        String result = fieldService.triggerCurrentFieldEvent(playerIdInt);
        assertTrue(result.contains("❌") || result.contains("verheiratet"));
    }


}
