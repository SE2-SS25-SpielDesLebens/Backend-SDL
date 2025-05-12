package at.aau.serg.websocketserver.fieldlogic;

import at.aau.serg.websocketserver.Player.Player;
import at.aau.serg.websocketserver.Player.PlayerService;
import org.springframework.stereotype.Service;

@Service
public class FieldService {

    private final PlayerService playerService;

    public FieldService(PlayerService playerService) {
        this.playerService = playerService;
    }

    public String handleFieldEvent(String playerId, FieldType fieldType) {
        Player player = playerService.getPlayerById(playerId).orElseThrow(() ->
                new IllegalArgumentException("Spieler nicht gefunden."));

        return switch (fieldType) {
            case PAYDAY -> handlePayday(player);
            case ACTION -> handleAction(player);
            case HOUSE -> handleHouse(player);
            case INVESTMENT -> handleInvestment(player);
            case STOP_FAMILY -> handleFamily(player);
            case STOP_RETIREMENT -> handleRetirement(player);
            case STOP_MARRIAGE -> handleMarriage(player);
            case STOP_MIDLIFECRISIS -> handleMidlifecrisis(player);
            case STOP_EXAM -> handleExam(player);
            default -> "Kein spezieller Effekt für dieses Feld.";
        };
    }

    private String handlePayday(Player player) {
        player.addMoney(player.getSalary());
        return "💵 Zahltag! Gehalt von " + player.getSalary() + "€ erhalten.";
    }

    private String handleAction(Player player) {
        return "🎲 Aktionskarte gezogen (noch nicht implementiert).";
    }

    private String handleHouse(Player player) {
        return "🏠 Hauskauf wird hier später implementiert.";
    }

    private String handleInvestment(Player player) {
        return playerService.investForPlayer(player.getId())
                ? "📈 20.000€ investiert."
                : "❌ Investition fehlgeschlagen.";
    }

    private String handleFamily(Player player) {
        return playerService.addChildToPlayer(player.getId())
                ? "👶 Ein Kind wurde zur Familie hinzugefügt!"
                : "❌ Fehler beim Hinzufügen eines Kindes.";
    }

    private String handleMarriage(Player player) {
        return playerService.marryPlayer(player.getId())
                ? "💍 Spieler ist jetzt verheiratet."
                : "❌ Spieler konnte nicht heiraten.";
    }

    private String handleRetirement(Player player) {
        player.retire();
        return "🪑 Spieler ist nun im Ruhestand.";
    }

    private String handleMidlifecrisis (Player player){
        return " Spieler befindet sich jetzt in der MidlifeCrisis!";
    }
    private String handleExam (Player player){
        return "Jobkarten müssen noch implemetiert werden!";
    }
}
