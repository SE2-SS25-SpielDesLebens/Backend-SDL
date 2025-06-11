package at.aau.serg.websocketserver.session.payout;

import at.aau.serg.websocketserver.session.board.BoardService;
import at.aau.serg.websocketserver.session.job.JobRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PayoutService {

    private final BoardService boardService;
    private final JobRepository jobRepository;

    // Nur eine globale Liste von PayoutEntries → KEINE MAP → einfach eine ArrayList
    private final List<PayoutRepository.PayoutEntry> payoutEntries = new ArrayList<>();

    /**
     * Lädt die Payout-Liste aus payouts.json für einen bestimmten Spieler (überschreibt die globale Liste).
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

    /**
     * Prüft ob der Spieler (playerName) auf einem Payout-Feld steht und gibt BonusSalary zurück.
     * In deinem JSON gibt es kein BonusSalary → also in dieser Methode einfach 0 oder 1 zurückgeben als Beispiel.
     * Hier nehmen wir einfach 1 als Dummy-Bonus.
     */
    public int checkAndApplyPayoutIfOnPayoutField(String playerName) {
        int playerFieldIndex = boardService.getPlayerPosition(playerName);

        for (PayoutRepository.PayoutEntry entry : payoutEntries) {
            if (entry.getPayoutId() == playerFieldIndex) {
                System.out.println("Payout erlaubt auf Feld " + playerFieldIndex);
                return 1; // Dummy-Betrag → du kannst hier später echten Betrag definieren
            }
        }
        return 0; // Kein Payout
    }

    /**
     * Prüft ob der Spieler auf einem der speziellen Felder steht.
     * Felder: 1, 10, 29, 38, 48, 62, 84, 96, 116, 120
     * Liest allowPayout aus der jeweiligen PayoutEntry aus dem JSON.
     * Gibt true zurück, wenn das Feld ein Spezialfeld ist und allowPayout == true.
     */
    public boolean checkIfPlayerOnSpecialField(String playerName) {
        int index = boardService.getPlayerPosition(playerName);

        // Prüfen ob der Spieler auf einem der Spezialfelder steht
        if (index == 1 || index == 10 || index == 29 || index == 38 || index == 48 ||
                index == 62 || index == 84 || index == 96 || index == 116 || index == 120) {

            // in der Entry-Liste prüfen ob für das Feld allowPayout true gesetzt ist:
            for (PayoutRepository.PayoutEntry entry : payoutEntries) {
                if (entry.getPayoutId() == index && entry.isAllowPayout()) {
                    // Spezialfeld mit allowPayout == true
                    return true;
                }
            }
        }

        return false; // kein Spezialfeld oder allowPayout == false
    }
}
