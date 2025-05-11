package Game;

import at.aau.serg.websocketserver.Player.Player;

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
}


