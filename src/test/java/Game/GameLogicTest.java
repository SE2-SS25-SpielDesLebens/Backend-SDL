package Game;

import Game.GameLogic;
import Game.GameController;
import at.aau.serg.websocketserver.board.Field;
import at.aau.serg.websocketserver.Player.Player;
import at.aau.serg.websocketserver.board.BoardService;
import at.aau.serg.websocketserver.session.Job;
import at.aau.serg.websocketserver.session.JobRepository;
import at.aau.serg.websocketserver.session.JobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GameLogicTest {

    private GameLogic logic;

    @BeforeEach
    public void setup() {
        logic = new GameLogic();
        logic.setBoardService(new BoardService());
        logic.setGameController(new DummyGameController());
    }

    @Test
    public void testRegisterPlayer_success() {
        boolean result = logic.registerPlayer("1");
        assertTrue(result);

        Player p = logic.getPlayerByName("1");
        assertNotNull(p);
        assertEquals(250000, p.getMoney());
        assertEquals("Rot", p.getCarColor());
    }

    @Test
    public void testRegisterPlayer_maxLimit() {
        logic.registerPlayer("1");
        logic.registerPlayer("2");
        logic.registerPlayer("3");
        logic.registerPlayer("4");
        boolean result = logic.registerPlayer("5");
        assertFalse(result);
    }

    @Test
    public void testPrepareGameStart_setsOnlyFirstPlayerActive() {
        logic.registerPlayer("1");
        logic.registerPlayer("2");

        logic.prepareGameStart();

        Player p1 = logic.getPlayerByName("1");
        Player p2 = logic.getPlayerByName("2");

        assertTrue(p1.isActive());
        assertFalse(p2.isActive());
    }

    @Test
    public void testHandleGameStartChoice_withUniversity() {
        logic.registerPlayer("1");
        Player p = logic.getPlayerByName("1");
        int initialMoney = p.getMoney();

        logic.handleGameStartChoice(1, "1", true);

        assertEquals(initialMoney - 100000, p.getMoney());
        assertTrue(p.getEducation());
    }

    @Test
    public void testHandleGameStartChoice_withoutUniversity_assignsJob_withInjectedJobService() {
        logic.registerPlayer("2");
        Player p = logic.getPlayerByName("2");

        Job testJob = new Job(101, "FallbackJob", 40000, 5000, false);
        JobRepository fakeRepo = new JobRepository() {
            @Override
            public List<Job> getRandomAvailableJobs(boolean requiresDegree, int count) {
                return List.of(testJob);
            }
        };

        JobService injectedJobService = new JobService(null) {
            @Override
            public JobRepository getOrCreateRepository(int gameId) {
                return fakeRepo;
            }
        };

        logic.setJobService(injectedJobService);
        logic.handleGameStartChoice(99, "2", false);

        assertNotNull(p.getJobId());
        assertEquals("FallbackJob", p.getJobId().getTitle());
        assertEquals(40000, p.getSalary());
    }

    @Test
    public void testRequestLoan_whenActive_returnsTrueAndAddsDebtAndMoney() {
        logic.registerPlayer("1");
        Player p = logic.getPlayerByName("1");
        p.setActive(true);

        boolean result = logic.requestLoan("1");

        assertTrue(result);
        assertEquals(1, p.getDebts());
        assertEquals(250000 + 50000, p.getMoney());
    }

    @Test
    public void testRequestLoan_whenInactive_returnsFalse() {
        logic.registerPlayer("1");
        Player p = logic.getPlayerByName("1");
        p.setActive(false);

        boolean result = logic.requestLoan("1");

        assertFalse(result);
        assertEquals(0, p.getDebts());
        assertEquals(250000, p.getMoney());
    }

    @Test
    public void testRepayLoan_successful() {
        logic.registerPlayer("1");
        Player p = logic.getPlayerByName("1");
        p.setActive(true);
        p.setMoney(70000);
        p.setDebts(1);

        logic.repayLoan("1");

        assertEquals(10000, p.getMoney());
        assertEquals(0, p.getDebts());
    }

    @Test
    public void testRepayLoan_noDebt_doesNothing() {
        logic.registerPlayer("1");
        Player p = logic.getPlayerByName("1");
        p.setActive(true);
        p.setMoney(70000);

        logic.repayLoan("1");

        assertEquals(70000, p.getMoney());
        assertEquals(0, p.getDebts());
    }

    @Test
    public void testRepayLoan_insufficientFunds_doesNothing() {
        logic.registerPlayer("1");
        Player p = logic.getPlayerByName("1");
        p.setActive(true);
        p.setMoney(50000);
        p.setDebts(1);

        logic.repayLoan("1");

        assertEquals(50000, p.getMoney());
        assertEquals(1, p.getDebts());
    }

    @Test
    public void testRepayLoan_inactivePlayer_doesNothing() {
        logic.registerPlayer("1");
        Player p = logic.getPlayerByName("1");
        p.setActive(false);
        p.setMoney(100000);
        p.setDebts(1);

        logic.repayLoan("1");

        assertEquals(100000, p.getMoney());
        assertEquals(1, p.getDebts());
    }

    @Test
    public void testPerformTurn_withRepeatExamFlag_callsHandleExamField() {
        logic.registerPlayer("1");
        Player p = logic.getPlayerByName("1");
        p.setActive(true);
        p.setMustRepeatExam(true);

        logic.performTurn(p, 5);

        assertFalse(p.mustRepeatExam()); // Flag sollte zurückgesetzt worden sein
    }

    @Test
    public void testPerformTurn_regularMove_invokesBoardAndInvestmentLogic() {
        logic.registerPlayer("1");
        Player p = logic.getPlayerByName("1");
        p.setActive(true);
        p.assignJob(new Job(1, "TestJob", 50000, 10000, false));

        BoardService fakeBoardService = new BoardService() {
            @Override
            public Field getPlayerField(int id) {
                return new Field(1, 0f, 0f, List.of(), "ZAHLTAG");
            }

            @Override
            public void movePlayer(int id, int steps) {
                // nichts tun, simulieren
            }

            @Override
            public Field getFieldByIndex(int index) {
                return new Field(index, 0f, 0f, List.of(), "ZAHLTAG");
            }
        };

        logic.setBoardService(fakeBoardService);
        logic.performTurn(p, 1);

        assertTrue(p.getMoney() >= 250000); // Sollte Zahltag-Geld enthalten
    }

    @Test
    public void testHandleField_dispatchesCorrectLogicForKnownTypes() {
        logic.registerPlayer("1");
        Player p = logic.getPlayerByName("1");

        List<String> typesToTest = List.of("ZAHLTAG", "AKTION", "HAUS", "BERUF", "ANLAGE", "FREUND", "HEIRAT", "EXAMEN");

        for (String type : typesToTest) {
            Field f = new Field(1, 0f, 0f, List.of(), type);
            try {
                logic.handleField(p, f); // Nur prüfen, dass kein Fehler geworfen wird
            } catch (Exception e) {
                fail("Fehler bei Feldtyp: " + type);
            }
        }
    }

    @Test
    public void testHandleField_withUnknownType_printsInfo() {
        logic.registerPlayer("1");
        Player p = logic.getPlayerByName("1");
        Field unknown = new Field(99, 0f, 0f, List.of(), "UNBEKANNT");

        logic.handleField(p, unknown); // Erwartet: keine Exception, Ausgabe ins Log
    }

    // Dummy GameController zum Abfangen von Spin-Requests
    static class DummyGameController extends GameController {
        public DummyGameController() {
            super(null, null);
        }

        @Override
        public void requestAdditionalSpin(String playerId) {
            System.out.println("[TEST] Spieler " + playerId + " darf erneut drehen.");
        }
    }
}




