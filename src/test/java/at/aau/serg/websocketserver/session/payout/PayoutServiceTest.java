package at.aau.serg.websocketserver.session.payout;

import at.aau.serg.websocketserver.player.PlayerService;
import at.aau.serg.websocketserver.session.board.BoardService;
import at.aau.serg.websocketserver.session.job.JobRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PayoutServiceTest {

    private PayoutService payoutService;
    private BoardService boardService;
    private JobRepository jobRepository;
    private PlayerService playerService;

    @BeforeEach
    void setup() {
        boardService = mock(BoardService.class);
        jobRepository = mock(JobRepository.class);
        playerService = mock(PlayerService.class);
        payoutService = new PayoutService(boardService, jobRepository, playerService);
    }

    @Test
    void testSeparatePayoutListsPerPlayer() throws Exception {
        // Simulierte payouts.json-Inhalte
        String json = "{\n" +
                "  \"payouts\": [\n" +
                "    { \"payoutId\": 5, \"allowPayout\": true },\n" +
                "    { \"payoutId\": 10, \"allowPayout\": false }\n" +
                "  ]\n" +
                "}";


        // InputStream manuell injizieren (statt aus Datei zu laden)
        InputStream inputStream1 = new ByteArrayInputStream(json.getBytes());
        InputStream inputStream2 = new ByteArrayInputStream(json.getBytes());

        // JSON einlesen für zwei Spieler
        injectPayouts(payoutService, "Alice", inputStream1);
        injectPayouts(payoutService, "Bob", inputStream2);

        // Ändere nur Alice's Einträge
        payoutService.checkIfPlayerOnSpecialField("Alice"); // sollte eigentlich nichts aktivieren
        var aliceList = getInternalPayouts(payoutService, "Alice");
        aliceList.get(0).setAllowPayout(false);

        var bobList = getInternalPayouts(payoutService, "Bob");

        // Sicherstellen, dass Bob's Liste nicht betroffen ist
        assertTrue(bobList.get(0).isAllowPayout(), "Bob's Liste sollte unabhängig von Alice sein.");
        assertFalse(aliceList.get(0).isAllowPayout(), "Alice's Liste wurde geändert.");
    }

    @Test
    void testCheckAndApplyPayoutIfOnPayoutField_appliesBonus() {
        var entry = new PayoutRepository.PayoutEntry(3, true);
        payoutService.playerPayouts.put("Test", List.of(entry));
        when(boardService.getPlayerPosition("Test")).thenReturn(3);
        when(jobRepository.payoutBonusSalary("Test")).thenReturn(500);

        int bonus = payoutService.checkAndApplyPayoutIfOnPayoutField("Test");

        assertEquals(500, bonus);
        assertFalse(entry.isAllowPayout());
    }

    @Test
    void testApplyPaydayIfPassedPayoutField_appliesSalary() {
        var entry = new PayoutRepository.PayoutEntry(5, true);
        payoutService.playerPayouts.put("Test", List.of(entry));
        when(boardService.getPlayerPosition("Test")).thenReturn(6);
        when(jobRepository.payoutSalary("Test")).thenReturn(300);

        int salary = payoutService.applyPaydayIfPassedPayoutField("Test");

        assertEquals(300, salary);
        assertFalse(entry.isAllowPayout());
    }

    @Test
    void testHandlePayoutAfterMovement_combinedLogic() {
        var entry = new PayoutRepository.PayoutEntry(4, true);
        payoutService.playerPayouts.put("Max", List.of(entry));
        when(boardService.getPlayerPosition("Max")).thenReturn(4);
        when(jobRepository.payoutBonusSalary("Max")).thenReturn(200);
        when(jobRepository.payoutSalary("Max")).thenReturn(300);

        payoutService.handlePayoutAfterMovement("Max");

        verify(playerService).addMoneyToPlayer("Max", 200); // Bonus nur aktiv
    }

    // ---------- Hilfsfunktionen ----------

    private void injectPayouts(PayoutService service, String playerName, InputStream stream) throws Exception {
        // Reflektiert exakt die Logik der Originalmethode – vereinfacht für Test
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(stream);
        List<PayoutRepository.PayoutEntry> list = new java.util.ArrayList<>();
        for (JsonNode node : root.get("payouts")) {
            list.add(new PayoutRepository.PayoutEntry(
                    node.get("payoutId").asInt(),
                    node.get("allowPayout").asBoolean()
            ));
        }
        service.playerPayouts.put(playerName, list);
    }

    private List<PayoutRepository.PayoutEntry> getInternalPayouts(PayoutService service, String playerName) {
        return service.playerPayouts.get(playerName);
    }
}
