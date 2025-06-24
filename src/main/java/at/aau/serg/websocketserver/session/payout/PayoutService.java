package at.aau.serg.websocketserver.session.payout;

import at.aau.serg.websocketserver.player.PlayerService;
import at.aau.serg.websocketserver.session.board.BoardService;
import at.aau.serg.websocketserver.session.job.JobRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PayoutService {

    private final BoardService boardService;
    private final JobRepository jobRepository;
    private final PlayerService playerService;

    // Spielername → individuelle Payout-Liste
    final Map<String, List<PayoutRepository.PayoutEntry>> playerPayouts = new HashMap<>();

    /**
     * Lädt die Payout-Liste aus payouts.json für einen bestimmten Spieler.
     * Muss am Spielstart pro Spieler aufgerufen werden.
     */
    public void loadPayoutForPlayer(String playerName) throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("payouts.json");
        if (inputStream == null) {
            throw new IllegalStateException("payouts.json nicht gefunden!");
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(inputStream);
        JsonNode payoutsNode = rootNode.get("payouts");

        List<PayoutRepository.PayoutEntry> entries = new ArrayList<>();

        if (payoutsNode != null && payoutsNode.isArray()) {
            for (JsonNode payoutNode : payoutsNode) {
                PayoutRepository.PayoutEntry entry = new PayoutRepository.PayoutEntry(
                        payoutNode.get("payoutId").asInt(),
                        payoutNode.get("allowPayout").asBoolean()
                );
                entries.add(entry);
            }
        }

        playerPayouts.put(playerName, entries);
    }

    public int checkAndApplyPayoutIfOnPayoutField(String playerName) {
        int playerFieldIndex = boardService.getPlayerPosition(playerName);
        List<PayoutRepository.PayoutEntry> entries = playerPayouts.getOrDefault(playerName, List.of());

        for (PayoutRepository.PayoutEntry entry : entries) {
            if (entry.getPayoutId() == playerFieldIndex && entry.isAllowPayout()) {
                int bonus = jobRepository.payoutBonusSalary(playerName);
                entry.setAllowPayout(false);
                return bonus;
            }
        }
        return 0;
    }

    public int applyPaydayIfPassedPayoutField(String playerName) {
        int currentPosition = boardService.getPlayerPosition(playerName);
        List<PayoutRepository.PayoutEntry> entries = playerPayouts.getOrDefault(playerName, List.of());

        for (PayoutRepository.PayoutEntry entry : entries) {
            if (entry.isAllowPayout() && currentPosition > entry.getPayoutId()) {
                entry.setAllowPayout(false);
                return jobRepository.payoutSalary(playerName);
            }
        }
        return 0;
    }

    public void checkIfPlayerOnSpecialField(String playerName) {
        int index = boardService.getPlayerPosition(playerName);

        Map<Integer, List<Integer>> bonusRules = Map.of(
                1, List.of(2),
                29, List.of(33),
                38, List.of(41),
                48, List.of(52),
                62, List.of(66),
                84, List.of(91),
                96, List.of(98),
                116, List.of(118),
                120, List.of(126, 132)
        );

        if (bonusRules.containsKey(index)) {
            List<Integer> targets = bonusRules.get(index);
            List<PayoutRepository.PayoutEntry> entries = playerPayouts.getOrDefault(playerName, List.of());

            for (PayoutRepository.PayoutEntry entry : entries) {
                if (targets.contains(entry.getPayoutId())) {
                    entry.setAllowPayout(true);
                }
            }
        }
    }

    /**
     * Diese Methode muss nach jeder Spielerbewegung aufgerufen werden.
     * Sie prüft:
     * 1. Ob Payouts aktiviert werden sollen.
     * 2. Ob der Spieler auf einem aktiven Payout-Feld steht.
     * 3. Ob ein aktives Zahltag-Feld überschritten wurde.
     */
    public void handlePayoutAfterMovement(String playerName) {
        int totalPayout = 0;

        // 1. Spezialfelder aktivieren (z. B. Feld 1 aktiviert 2 usw.)
        checkIfPlayerOnSpecialField(playerName);

        // 3. Bonusgehalt wenn Spieler direkt auf aktivem Payout-Feld steht
        totalPayout += checkAndApplyPayoutIfOnPayoutField(playerName);

        // 2. Normales Gehalt (Zahltag) wenn Spieler über ein aktives Feld hinweg ist
        totalPayout += applyPaydayIfPassedPayoutField(playerName);

        if (totalPayout > 0) {
            playerService.addMoneyToPlayer(playerName, totalPayout);
        }
    }
}
