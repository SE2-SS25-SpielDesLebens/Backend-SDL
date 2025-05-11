package Game;

import at.aau.serg.websocketserver.Player.Player;

public class PlayerTurnManager {

    private final GameLogic gameLogic;

    public PlayerTurnManager(GameLogic gameLogic) {
        this.gameLogic = gameLogic;
    }

    // Startet den Spielzug und behandelt z. B. Kapitalanlage
    public void startTurn(String playerId, boolean kauftKapitalanlage) {
        Player player = gameLogic.getCurrentPlayer();
        if (!player.getId().equals(playerId)) {
            System.out.println("[BLOCKIERT] Spieler " + playerId + " ist nicht am Zug.");
            return;
        }

        if (kauftKapitalanlage) {
            // TODO: Kapitalanlage-System integrieren
            System.out.println("[KAPITALANLAGE] " + playerId + " kauft eine Kapitalanlage-Karte.");
        }

        System.out.println("[BEREIT] " + playerId + " wartet auf Drehrad-Ergebnis und Feldaktion...");
    }

    // Spieler beginnt mit Berufseinstieg (ohne Studium)
    public void startWithCareer(String playerId, int gameId) {
        boolean chooseUniversity = false;
        gameLogic.handleGameStartChoice(gameId, playerId, chooseUniversity);
        System.out.println("[START] " + playerId + " beginnt direkt mit einem Beruf.");
    }

    // Spieler beginnt mit Studium (Universität)
    public void startWithUniversity(String playerId, int gameId) {
        boolean chooseUniversity = true;
        gameLogic.handleGameStartChoice(gameId, playerId, chooseUniversity);
        System.out.println("[START] " + playerId + " entscheidet sich für das Studium.");
    }

    // Spieler nimmt während seines Zugs einen Kredit auf
    public void takeLoan(String playerId) {
        Player player = gameLogic.getCurrentPlayer();
        if (!player.getId().equals(playerId)) {
            System.out.println("[BLOCKIERT] Kreditaufnahme nur im eigenen Zug erlaubt.");
            return;
        }

        player.takeLoan();
        System.out.println("[KREDIT] " + playerId + " nimmt einen Kredit auf (+20.000 €). Schulden: " + player.getDebts());
    }

    // Spieler zahlt während seines Zugs einen Kredit zurück
    public void repayLoan(String playerId) {
        Player player = gameLogic.getCurrentPlayer();
        if (!player.getId().equals(playerId)) {
            System.out.println("[BLOCKIERT] Rückzahlung nur im eigenen Zug erlaubt.");
            return;
        }

        if (player.getDebts() == 0) {
            System.out.println("[INFO] " + playerId + " hat keine offenen Kredite.");
            return;
        }

        if (player.getMoney() < 25000) {
            System.out.println("[FEHLER] " + playerId + " hat nicht genug Geld für die Rückzahlung.");
            return;
        }

        player.repayLoan();
        System.out.println("[RÜCKZAHLUNG] " + playerId + " zahlt einen Kredit zurück (–25.000 €). Verbleibende Schulden: " + player.getDebts());
    }

    // Beendet den Spielzug, sobald das Drehrad und Feldaktion durch andere Systeme abgeschlossen sind
    public void completeTurn(String playerId, int spinResult) {
        Player player = gameLogic.getCurrentPlayer();
        if (!player.getId().equals(playerId)) {
            System.out.println("[IGNORIERT] Nur der aktive Spieler darf den Zug abschließen.");
            return;
        }

        System.out.println("[DREH] " + playerId + " hat " + spinResult + " gedreht.");

        // TODO: Feldlogik wird von externen Modulen übernommen

        gameLogic.performTurn(player, spinResult);
    }
}




