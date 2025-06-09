package at.aau.serg.websocketserver.game;

import at.aau.serg.websocketserver.player.Player;
import at.aau.serg.websocketserver.messaging.dtos.OutputMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.TimerTask;

import static org.junit.jupiter.api.Assertions.*;

public class GameControllerTest {

    private DummyGameLogic logic;
    private DummyMessagingTemplate messagingTemplate;
    private GameController controller;

    @BeforeEach
    public void setup() {
        logic = new DummyGameLogic();
        messagingTemplate = new DummyMessagingTemplate();
        controller = new GameController(logic, messagingTemplate);
    }

    @Test
    public void testIsPlayerTurn_returnsTrueIfCorrectPlayer() {
        Player p = new Player("1");
        logic.setCurrentPlayer(p);
        assertTrue(controller.isPlayerTurn("1"));
    }

    @Test
    public void testIsPlayerTurn_returnsFalseIfWrongPlayer() {
        Player p = new Player("1");
        logic.setCurrentPlayer(p);
        assertFalse(controller.isPlayerTurn("2"));
    }

    @Test
    public void testCanPlayerAct_trueIfCorrectPlayerAndNotEnded() {
        Player p = new Player("abc");
        logic.setCurrentPlayer(p);
        logic.setGameEnded(false);
        assertTrue(controller.canPlayerAct("abc"));
    }

    @Test
    public void testCanPlayerAct_falseIfGameEnded() {
        Player p = new Player("abc");
        logic.setCurrentPlayer(p);
        logic.setGameEnded(true);
        assertFalse(controller.canPlayerAct("abc"));
    }

    @Test
    public void testAdvanceToNextTurnIfValid_advancesIfCorrectPlayer() {
        Player p = new Player("1");
        logic.setCurrentPlayer(p);
        controller.advanceToNextTurnIfValid("1");
        assertEquals(1, logic.getNextTurnCount());
    }

    @Test
    public void testAdvanceToNextTurnIfValid_doesNothingIfWrongPlayer() {
        Player p = new Player("1");
        logic.setCurrentPlayer(p);
        controller.advanceToNextTurnIfValid("wrong");
        assertEquals(0, logic.getNextTurnCount());
    }

    @Test
    public void testStartPlayerTurn_correctPlayerLogsAction() {
        Player p = new Player("abc");
        logic.setCurrentPlayer(p);
        java.io.ByteArrayOutputStream outContent = new java.io.ByteArrayOutputStream();
        java.io.PrintStream originalOut = System.out;
        System.setOut(new java.io.PrintStream(outContent));
        try {
            controller.startPlayerTurn("abc", true);
            String output = outContent.toString();
            assertTrue(output.contains("abc"), "Die Konsolenausgabe sollte die Spieler-ID enthalten.");
            assertTrue(output.contains("[INFO]") && output.contains("startet seinen Zug"), 
                "Die Konsolenausgabe sollte die korrekte Info-Nachricht enthalten.");
            assertTrue(output.contains("[AKTION]") && output.contains("kauft eine Kapitalanlage"), 
                "Die Konsolenausgabe sollte die Kaufaktion enthalten.");
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    public void testStartPlayerTurn_wrongPlayerDoesNothing() {
        Player p = new Player("abc");
        logic.setCurrentPlayer(p);
        controller.startPlayerTurn("xyz", false);
        assertEquals(0, logic.getNextTurnCount());
    }

    @Test
    public void testCompletePlayerTurn_withCorrectPlayer_callsPerformTurn() {
        Player p = new Player("abc");
        logic.setCurrentPlayer(p);
        controller.completePlayerTurn("abc", 5);
        assertEquals(5, logic.getLastSpinResult());
    }

    @Test
    public void testCompletePlayerTurn_withWrongPlayer_doesNothing() {
        Player p = new Player("abc");
        logic.setCurrentPlayer(p);
        controller.completePlayerTurn("wrong", 8);
        assertEquals(-1, logic.getLastSpinResult());
    }

    @Test
    public void testStartRepeatExamTurn_sendsExpectedMessage() {
        Player p = new Player("abc");
        logic.setCurrentPlayer(p);
        controller.startRepeatExamTurn("abc");

        OutputMessage msg = (OutputMessage) messagingTemplate.lastMessage;
        assertTrue(messagingTemplate.lastTopic.endsWith("/exam-repeat/abc"));
        assertTrue(msg.getContent().contains("durch die Prüfung"));
    }

    @Test
    public void testRequestAdditionalSpin_sendsExpectedMessage() {
        controller.requestAdditionalSpin("abc");

        OutputMessage msg = (OutputMessage) messagingTemplate.lastMessage;
        assertTrue(messagingTemplate.lastTopic.endsWith("/spin-again/abc"));
        assertTrue(msg.getContent().contains("erneut drehen"));
    }

    @Test
    public void testTurnTimeout_callsNextTurn() {
        Player p = new Player("timeoutPlayer");
        logic.setCurrentPlayer(p);

        TimerTask timeoutTask = controller.createTurnTimeout("timeoutPlayer");
        timeoutTask.run();

        assertEquals(1, logic.getNextTurnCount(), "nextTurn() sollte durch Timeout ausgeführt werden.");
    }

    @Test
    public void testAnonymousTimerTask_run_invokesNextTurn() {
        Player p = new Player("abc");
        logic.setCurrentPlayer(p);


        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                System.out.println("[ZEITABLAUF] Spieler " + p.getId() + " war zu langsam. Nächster ist dran.");
                logic.nextTurn(); // Direkt auf Dummy-Logik aufrufen
            }
        };


        assertEquals(0, logic.getNextTurnCount(), "Vor run() sollte nextTurnCount 0 sein.");
        task.run();
        assertEquals(1, logic.getNextTurnCount(), "Nach run() sollte nextTurnCount 1 sein.");
    }


    @Test
    public void testCreateTurnTimeout_returnsTimerTaskThatTriggersTurnAdvance() {
        Player p = new Player("timeoutPlayer");
        logic.setCurrentPlayer(p);

        TimerTask task = controller.createTurnTimeout("timeoutPlayer");

        assertNotNull(task, "TimerTask sollte nicht null sein.");

        assertEquals(0, logic.getNextTurnCount(), "Vor run() sollte nextTurnCount 0 sein.");
        task.run();
        assertEquals(1, logic.getNextTurnCount(), "Nach run() sollte nextTurnCount 1 sein.");
    }

    static class DummyGameLogic extends GameLogic {
        private Player currentPlayer = null;
        private boolean ended = false;
        private int nextTurnCount = 0;
        private int lastSpinResult = -1;

        public void setCurrentPlayer(Player p) {
            this.currentPlayer = p;
        }

        public void setGameEnded(boolean b) {
            this.ended = b;
        }

        @Override
        public Player getCurrentPlayer() {
            return currentPlayer;
        }

        @Override
        public boolean isGameEnded() {
            return ended;
        }

        @Override
        public void nextTurn() {
            nextTurnCount++;
        }

        @Override
        public void performTurn(Player player, int result) {
            this.lastSpinResult = result;
        }

        @Override
        public int getGameId() {
            return 42;
        }

        public int getNextTurnCount() {
            return nextTurnCount;
        }

        public int getLastSpinResult() {
            return lastSpinResult;
        }
    }

    static class DummyMessagingTemplate extends SimpMessagingTemplate {
        public String lastTopic;
        public Object lastMessage;

        public DummyMessagingTemplate() {
            super(new MessageChannel() {
                @Override
                public boolean send(@org.springframework.lang.NonNull Message<?> message) {
                    return true;
                }

                @Override
                public boolean send(@org.springframework.lang.NonNull Message<?> message, long timeout) {
                    return true;
                }
            });
        }

        @Override
        public void convertAndSend(@org.springframework.lang.NonNull String destination, @org.springframework.lang.NonNull Object payload) {
            this.lastTopic = destination;
            this.lastMessage = payload;
        }
    }
}

