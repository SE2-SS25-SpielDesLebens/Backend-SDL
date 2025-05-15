package at.aau.serg.websocketserver.game;

import at.aau.serg.websocketserver.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerTurnManagerTest {

    private DummyGameLogic logic;
    private PlayerTurnManager manager;

    @BeforeEach
    public void setup() {
        logic = new DummyGameLogic();
        manager = new PlayerTurnManager(logic);
    }

    @Test
    public void testStartTurn_correctPlayer_noError() {
        Player player = new Player("1");
        logic.setCurrentPlayer(player);

        assertDoesNotThrow(() -> manager.startTurn("1", false));
    }

    @Test
    public void testStartTurn_wrongPlayer_printsBlocked() {
        Player correct = new Player("1");
        Player wrong = new Player("2");
        logic.setCurrentPlayer(correct);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        manager.startTurn(wrong.getId(), false);

        String output = outContent.toString();
        assertTrue(output.contains("[BLOCKIERT] Spieler 2 ist nicht am Zug."));

        System.setOut(System.out);
    }

    @Test
    public void testStartWithCareer_triggersLogicCorrectly() {
        manager.startWithCareer("1", 10);
        assertEquals("career", logic.getStartType());
    }

    @Test
    public void testStartWithUniversity_triggersLogicCorrectly() {
        manager.startWithUniversity("1", 10);
        assertEquals("university", logic.getStartType());
    }

    @Test
    public void testTakeLoan_successfulLoan() {
        logic.setLoanPossible(true);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        manager.takeLoan("1");

        String output = outContent.toString();
        assertFalse(output.contains("fehlgeschlagen"));

        System.setOut(System.out);
    }

    @Test
    public void testTakeLoan_failedLoan() {
        logic.setLoanPossible(false);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        manager.takeLoan("1");

        String output = outContent.toString();
        assertTrue(output.contains("fehlgeschlagen"));

        System.setOut(System.out);
    }

    @Test
    public void testRepayLoan_triggersRepayment() {
        manager.repayLoan("1");
        assertEquals("1", logic.getRepaymentPlayer());
    }

    @Test
    public void testCompleteTurn_correctPlayer_callsSpin() {
        Player player = new Player("1");
        logic.setCurrentPlayer(player);

        manager.completeTurn("1", 6);

        assertEquals(6, logic.getLastSpinResult());
    }

    @Test
    public void testCompleteTurn_wrongPlayer_doesNothing() {
        Player correct = new Player("1");
        Player wrong = new Player("2");
        logic.setCurrentPlayer(correct);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        manager.completeTurn(wrong.getId(), 5);

        String output = outContent.toString();
        assertTrue(output.contains("[IGNORIERT] Nur der aktive Spieler darf den Zug abschließen."));
        assertEquals(-1, logic.getLastSpinResult());

        System.setOut(System.out);
    }

    static class DummyGameLogic extends GameLogic {
        private Player currentPlayer;
        private boolean loanPossible = true;
        private String startType = "";
        private String repaymentPlayer = "";
        private int lastSpinResult = -1;

        public void setCurrentPlayer(Player player) {
            this.currentPlayer = player;
        }

        public void setLoanPossible(boolean possible) {
            this.loanPossible = possible;
        }

        public String getStartType() {
            return startType;
        }

        public String getRepaymentPlayer() {
            return repaymentPlayer;
        }

        public int getLastSpinResult() {
            return lastSpinResult;
        }

        @Override
        public Player getCurrentPlayer() {
            return currentPlayer;
        }

        @Override
        public boolean requestLoan(String playerId) {
            return loanPossible;
        }

        @Override
        public void repayLoan(String playerId) {
            this.repaymentPlayer = playerId;
        }

        @Override
        public void handleGameStartChoice(int gameId, String playerId, boolean university) {
            this.startType = university ? "university" : "career";
        }

        @Override
        public void performTurn(Player player, int result) {
            this.lastSpinResult = result;
        }
    }
}



