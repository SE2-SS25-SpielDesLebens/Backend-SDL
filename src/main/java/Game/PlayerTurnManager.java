package Game;

import at.aau.serg.websocketserver.Player.Player;

public class PlayerTurnManager {

    private final GameLogic gameLogic;

    public PlayerTurnManager(GameLogic gameLogic) {
        this.gameLogic = gameLogic;
    }

    public void startTurn(String playerId, boolean kauftKapitalanlage) {
        Player player = gameLogic.getCurrentPlayer();
        if (!player.getId().equals(playerId)) {
            System.out.println("[BLOCKIERT] Spieler " + playerId + " ist nicht am Zug.");
            return;
        }

        if (kauftKapitalanlage) {
            System.out.println("[AKTION] " + playerId + " kauft eine Kapitalanlage.");
        }

        System.out.println("[BEREIT] " + playerId + " wartet auf Drehrad-Ergebnis und Feldaktion...");
    }

    public void startWithCareer(String playerId, int gameId) {
        boolean chooseUniversity = false;
        gameLogic.handleGameStartChoice(gameId, playerId, chooseUniversity);
        System.out.println("[START] " + playerId + " beginnt direkt mit einem Beruf.");
    }

    public void startWithUniversity(String playerId, int gameId) {
        boolean chooseUniversity = true;
        gameLogic.handleGameStartChoice(gameId, playerId, chooseUniversity);
        System.out.println("[START] " + playerId + " entscheidet sich für das Studium.");
    }

    public void takeLoan(String playerId) {
        if (!gameLogic.requestLoan(playerId)) {
            System.out.println("[FEHLER] Kreditaufnahme für Spieler " + playerId + " fehlgeschlagen.");
        }
    }

    public void repayLoan(String playerId) {
        gameLogic.repayLoan(playerId);
    }

    public void completeTurn(String playerId, int spinResult) {
        Player player = gameLogic.getCurrentPlayer();
        if (!player.getId().equals(playerId)) {
            System.out.println("[IGNORIERT] Nur der aktive Spieler darf den Zug abschließen.");
            return;
        }

        System.out.println("[DREH] " + playerId + " hat " + spinResult + " gedreht.");
        gameLogic.performTurn(player, spinResult);
    }
}





