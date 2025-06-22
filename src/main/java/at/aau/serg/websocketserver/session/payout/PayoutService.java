package at.aau.serg.websocketserver.session.payout;

import at.aau.serg.websocketserver.player.PlayerService;
import at.aau.serg.websocketserver.session.board.BoardService;
import at.aau.serg.websocketserver.session.job.JobRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PayoutService {

    private final BoardService boardService;
    private final JobRepository jobRepository;
    private final PlayerService playerService;


    // Nur eine globale Liste von PayoutEntries → KEINE MAP → einfach eine ArrayList
    private static final List<PayoutRepository.PayoutEntry> payoutEntries = new ArrayList<>();

    /**
     * Lädt die Payout-Liste aus payouts.json für einen bestimmten Spieler. Am Start des Spiels auszuführen
     */
    public void loadPayoutForPlayer(String playerName) throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("payouts.json");
        if (inputStream == null) {
            throw new IllegalStateException("payouts.json nicht gefunden!");
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(inputStream);
        JsonNode payoutsNode = rootNode.get("payouts");

        payoutEntries.clear(); // WICHTIG → alte Einträge löschen

        if (payoutsNode != null && payoutsNode.isArray()) {
            for (JsonNode payoutNode : payoutsNode) {
                PayoutRepository.PayoutEntry entry = new PayoutRepository.PayoutEntry(
                        payoutNode.get("payoutId").asInt(),
                        payoutNode.get("allowPayout").asBoolean()
                );
                payoutEntries.add(entry);
            }
        }
    }


    public int checkAndApplyPayoutIfOnPayoutField(String playerName) {
        int playerFieldIndex = boardService.getPlayerPosition(playerName);

        for (PayoutRepository.PayoutEntry entry : payoutEntries) {
            if (entry.getPayoutId() == playerFieldIndex && entry.isAllowPayout()) {
                int bonus = jobRepository.payoutBonusSalary(playerName);
                entry.setAllowPayout(false); // Deaktivieren, damit es nur einmal auszahlt
                return bonus;
            }
        }
        return 0; // Kein Payout
    }

    public int applyPaydayIfPassedPayoutField(String playerName) {
        int currentPosition = boardService.getPlayerPosition(playerName);

        for (PayoutRepository.PayoutEntry entry : payoutEntries) {
            if (entry.isAllowPayout() && currentPosition > entry.getPayoutId()) {
                entry.setAllowPayout(false); // Auszahlung erfolgt → zurücksetzen
                return jobRepository.payoutSalary(playerName); // normales Gehalt
            }
        }
        return 0; // Kein Zahltag ausgelöst
    }

    public void checkIfPlayerOnSpecialField(String playerName) {
        int index = boardService.getPlayerPosition(playerName);

        // Spezialregeln: Spielerposition → Liste der payoutIds, die aktiviert werden sollen
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

        // Falls für das aktuelle Feld Bonusregel definiert ist:
        if (bonusRules.containsKey(index)) {
            List<Integer> targets = bonusRules.get(index);
            for (PayoutRepository.PayoutEntry entry : payoutEntries) {
                if (targets.contains(entry.getPayoutId())) {
                    entry.setAllowPayout(true);
                }
            }
        }
    }
    /**
     * Diese Methode muss nach jeder Spielerbewegung aufgerufen werden.
     * Sie prüft:
     * 1. Ob durch das aktuelle Spielfeld Spezial-Payouts aktiviert werden sollen.
     * 2. Ob der Spieler direkt auf einem aktiven Payout-Feld steht und zahlt ggf. Bonusgehalt aus.
     * 3. Ob der Spieler ein aktives Zahltag-Feld (Payday) überquert hat und zahlt ggf. normales Gehalt aus.
     */
    /**
     * Prüft, ob ein bestimmtes Feld derzeit als aktives Zahltag-Feld gilt.
     */
    public static boolean isPaydayField(int fieldIndex) {
        return payoutEntries.stream()
                .anyMatch(entry -> entry.getPayoutId() == fieldIndex && entry.isAllowPayout());
    }


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
