package Game;

import java.util.Timer;
import java.util.TimerTask;

public class GameController {

    private final GameLogic gameLogic;
    private Timer turnTimer;
    private static final long TURN_TIMEOUT_MS = 2 * 60 * 1000; // 2 Minuten

    public GameController(GameLogic gameLogic) {
        this.gameLogic = gameLogic;
    }

    public boolean isPlayerTurn(String playerId) {
        Player current = gameLogic.getCurrentPlayer();
        return current.getId().equals(playerId);
    }

    public boolean canPlayerAct(String playerId) {
        return isPlayerTurn(playerId) && !gameLogic.isGameEnded();
    }

    public void advanceToNextTurnIfValid(String playerId) {
        if (!isPlayerTurn(playerId)) {
            System.out.println("[VERWEIGERT] Spieler " + playerId + " ist nicht am Zug.");
            return;
        }
        cancelTurnTimer();
        gameLogic.nextTurn();
    }

    public void startPlayerTurn(String playerId, boolean kauftKapitalanlage) {
        if (!isPlayerTurn(playerId)) {
            System.out.println("[BLOCKIERT] Spieler " + playerId + " ist nicht dran.");
            return;
        }

        if (kauftKapitalanlage) {
            System.out.println("[AKTION] " + playerId + " kauft eine Kapitalanlage.");
        }

        System.out.println("[INFO] " + playerId + " startet seinen Zug. Bitte Drehrad und Feldaktionen ausführen...");
        startTurnTimeout(playerId);
    }

    private void startTurnTimeout(String playerId) {
        cancelTurnTimer();

        turnTimer = new Timer();
        turnTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("[ZEITABLAUF] Spieler " + playerId + " war zu langsam. Nächster ist dran.");
                gameLogic.nextTurn();
            }
        }, TURN_TIMEOUT_MS);
    }

    private void cancelTurnTimer() {
        if (turnTimer != null) {
            turnTimer.cancel();
            turnTimer = null;
        }
    }

    public void completePlayerTurn(String playerId, int spinResult) {
        if (!isPlayerTurn(playerId)) {
            System.out.println("[IGNORIERT] " + playerId + " ist nicht aktiv.");
            return;
        }

        cancelTurnTimer();

        Player player = gameLogic.getCurrentPlayer();
        System.out.println("[DREH] Spieler " + playerId + " hat " + spinResult + " gewürfelt.");

        gameLogic.performTurn(player, spinResult);
    }

    public void enforceSpeedingFine(String policeId, String offenderId) {
        Player police = gameLogic.getPlayers().stream()
                .filter(p -> p.getId().equals(policeId) && p.hasPoliceCard())
                .findFirst()
                .orElse(null);

        Player offender = gameLogic.getPlayers().stream()
                .filter(p -> p.getId().equals(offenderId))
                .findFirst()
                .orElse(null);

        if (police == null || offender == null) {
            System.out.println("[FEHLER] Ungültige Spieler-ID oder keine Polizistenrolle vorhanden.");
            return;
        }

        if (offender.getMoney() >= 5000) {
            offender.removeMoney(5000);
            police.addMoney(5000);
            System.out.println("[STRAFE] " + offenderId + " zahlt 5.000 € an " + policeId + ".");
        } else {
            System.out.println("[FEHLER] " + offenderId + " hat nicht genug Geld für die Strafe.");
        }
    }

    public boolean attemptStealLifeCard(String playerId) {
        if (!gameLogic.canStealLifeCard(playerId)) {
            System.out.println("[DIEBSTAHL] Nicht erlaubt für " + playerId);
            return false;
        }
        return gameLogic.stealLifeCard(playerId);
    }
}

