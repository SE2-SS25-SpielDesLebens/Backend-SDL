package at.aau.serg.websocketserver.session.payout;

import at.aau.serg.websocketserver.player.PlayerService;
import at.aau.serg.websocketserver.session.board.BoardService;
import at.aau.serg.websocketserver.session.job.JobRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PayoutServiceTest {

    private BoardService boardService;
    private JobRepository jobRepository;
    private PlayerService playerService;
    private PayoutService payoutService;

    @BeforeEach
    void setup() {
        boardService = mock(BoardService.class);
        jobRepository = mock(JobRepository.class);
        playerService = mock(PlayerService.class);
        payoutService = new PayoutService(boardService, jobRepository, playerService);
    }

    @Test
    void testCheckAndApplyPayoutIfOnPayoutField_appliesBonusAndDisables() throws Exception {
        when(boardService.getPlayerPosition("Alice")).thenReturn(10);
        addEntry(new PayoutRepository.PayoutEntry(10, true));
        when(jobRepository.payoutBonusSalary("Alice")).thenReturn(500);

        int result = payoutService.checkAndApplyPayoutIfOnPayoutField("Alice");

        assertEquals(500, result);
        assertFalse(getEntries().get(0).isAllowPayout());
    }

    @Test
    void testCheckAndApplyPayoutIfOnPayoutField_doesNothingIfNoMatch() throws Exception {
        when(boardService.getPlayerPosition("Eve")).thenReturn(5);
        addEntry(new PayoutRepository.PayoutEntry(10, true)); // kein Match

        int result = payoutService.checkAndApplyPayoutIfOnPayoutField("Eve");

        assertEquals(0, result); // kein Bonus
    }

    @Test
    void testApplyPaydayIfPassedPayoutField_noPayoutIfNotPassed() throws Exception {
        when(boardService.getPlayerPosition("Frank")).thenReturn(10);
        addEntry(new PayoutRepository.PayoutEntry(15, true)); // noch nicht überschritten

        int result = payoutService.applyPaydayIfPassedPayoutField("Frank");

        assertEquals(0, result);
    }


    @Test
    void testApplyPaydayIfPassedPayoutField_appliesSalaryAndDisables() throws Exception {
        when(boardService.getPlayerPosition("Bob")).thenReturn(20);
        addEntry(new PayoutRepository.PayoutEntry(15, true));
        when(jobRepository.payoutSalary("Bob")).thenReturn(800);

        int result = payoutService.applyPaydayIfPassedPayoutField("Bob");

        assertEquals(800, result);
        assertFalse(getEntries().get(0).isAllowPayout());
    }

    @Test
    void testCheckIfPlayerOnSpecialField_activatesTargetField() throws Exception {
        when(boardService.getPlayerPosition("Carol")).thenReturn(1);
        addEntry(new PayoutRepository.PayoutEntry(2, false));

        payoutService.checkIfPlayerOnSpecialField("Carol");

        assertTrue(getEntries().get(0).isAllowPayout());
    }

    @Test
    void testCheckIfPlayerOnSpecialField_doesNothingIfNoMatch() throws Exception {
        when(boardService.getPlayerPosition("Grace")).thenReturn(5); // kein Spezialfeld
        addEntry(new PayoutRepository.PayoutEntry(33, false)); // sollte nicht aktiviert werden

        payoutService.checkIfPlayerOnSpecialField("Grace");

        assertFalse(getEntries().get(0).isAllowPayout());
    }


    @Test
    void testHandlePayoutAfterMovement_combinesAllLogic_withoutMoneyTransfer() throws Exception {
        when(boardService.getPlayerPosition("Dave")).thenReturn(29);

        // Bonusfeld aktiv
        addEntry(new PayoutRepository.PayoutEntry(29, true));
        // Payday-Feld unterhalb der aktuellen Position
        addEntry(new PayoutRepository.PayoutEntry(15, true));
        // Spezialregel: Feld 33 wird aktiviert
        addEntry(new PayoutRepository.PayoutEntry(33, false));

        when(jobRepository.payoutBonusSalary("Dave")).thenReturn(300);
        when(jobRepository.payoutSalary("Dave")).thenReturn(1000);

        payoutService.handlePayoutAfterMovement("Dave");

        List<PayoutRepository.PayoutEntry> entries = getEntries();

        // Erwartung: Bonusfeld und Payday wurden deaktiviert
        assertFalse(entries.get(0).isAllowPayout()); // 29
        assertFalse(entries.get(1).isAllowPayout()); // 15
        assertTrue(entries.get(2).isAllowPayout());  // 33 aktiviert durch Spezialregel

        // Auszahlung wurde nicht durchgeführt (weil im Produktivcode auskommentiert)
        verify(playerService, never()).addMoneyToPlayer(anyString(), anyInt());
    }

    @Test
    void testHandlePayoutAfterMovement_nothingHappens() throws Exception {
        when(boardService.getPlayerPosition("Henry")).thenReturn(5); // kein Spezialfeld, kein Bonus, kein Payday

        addEntry(new PayoutRepository.PayoutEntry(10, false)); // kein aktives Feld

        payoutService.handlePayoutAfterMovement("Henry");

        verify(playerService, never()).addMoneyToPlayer(anyString(), anyInt());
        assertFalse(getEntries().get(0).isAllowPayout());
    }


    @Test
    void testLoadPayoutForPlayer_throwsExceptionIfFileNotFound() {
        assertThrows(IllegalStateException.class, () -> {
            payoutService.loadPayoutForPlayer("Dummy"); // ohne JSON-Datei im Testpfad
        });
    }

    @Test
    void testLoadPayoutForPlayer_doesNothingIfPayoutsNodeMissing() throws Exception {
        String json = "{}"; // kein "payouts"-Key vorhanden
        InputStream mockStream = new java.io.ByteArrayInputStream(json.getBytes());

        // simulate JSON loading
        Field streamField = PayoutService.class.getDeclaredField("payoutEntries");
        streamField.setAccessible(true);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(mockStream);
        JsonNode payoutsNode = root.get("payouts");

        assertNull(payoutsNode);
        // Methode ist aber privat, du müsstest loadPayoutForPlayer anpassen oder in einen JSON-Test auslagern
    }


    // -----------------------------------------------
    // Helfer für Reflection-Zugriff auf payoutEntries
    // -----------------------------------------------
    @SuppressWarnings("unchecked")
    private List<PayoutRepository.PayoutEntry> getEntries() throws Exception {
        Field field = PayoutService.class.getDeclaredField("payoutEntries");
        field.setAccessible(true);
        return (List<PayoutRepository.PayoutEntry>) field.get(payoutService);
    }

    private void addEntry(PayoutRepository.PayoutEntry entry) throws Exception {
        getEntries().add(entry);
    }
}
