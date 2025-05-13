package at.aau.serg.websocketserver.fieldlogic;

import at.aau.serg.websocketserver.Player.Player;
import at.aau.serg.websocketserver.Player.PlayerService;
import at.aau.serg.websocketserver.board.BoardService;
import at.aau.serg.websocketserver.board.Field;
import org.springframework.stereotype.Service;

@Service
public class FieldService {

    private final PlayerService playerService;
    private final BoardService boardService;

    public FieldService(PlayerService playerService, BoardService boardService) {
        this.playerService = playerService;
        this.boardService = boardService;
    }

    /**
     * Reagiere auf ein Feldereignis basierend auf der aktuellen Spielerposition
     */
    public String triggerCurrentFieldEvent(int playerId) {
        // Spieler ermitteln
        Player player = playerService.getPlayerById(String.valueOf(playerId)).orElseThrow(() ->
                new IllegalArgumentException("Spieler nicht gefunden."));

        // Aktuelles Feld vom BoardService holen
        Field field = boardService.getPlayerField(playerId);

        // Feldtyp interpretieren
        FieldType fieldType;
        try {
            fieldType = FieldType.valueOf(field.getType());
        } catch (IllegalArgumentException e) {
            return "❌ Unbekannter Feldtyp: " + field.getType();
        }

        // Je nach Typ entsprechende Aktion ausführen
        return handleFieldEvent(player, fieldType);
    }

    private String handleFieldEvent(Player player, FieldType fieldType) {
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
            default -> "❌ Kein definierter Effekt für dieses Feld.";
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

    private String handleMidlifecrisis(Player player) {
        return "😵 Spieler befindet sich jetzt in der MidlifeCrisis!";
    }

    private String handleExam(Player player) {
        return "🎓 Jobkarten müssen noch implementiert werden.";
    }
}
