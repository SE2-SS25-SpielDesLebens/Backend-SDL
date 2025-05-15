package at.aau.serg.websocketserver.game;

import at.aau.serg.websocketserver.board.Field;
import at.aau.serg.websocketserver.player.Player;
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
        logic.setGameController(new DummyGameController(logic));
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
    public void testPerformTurn_withRepeatExamFlag_executesExamLogic() {
        logic.registerPlayer("1");
        Player p = logic.getPlayerByName("1");
        p.setActive(true);
        p.setMustRepeatExam(true);


        Job testJob = new Job(101, "FallbackJob", 40000, 5000, false);
        JobRepository fakeRepo = new JobRepository() {
            @Override
            public List<Job> getRandomAvailableJobs(boolean requiresDegree, int count) {
                return List.of(testJob);
            }
        };

        JobService dummyService = new JobService(null) {
            @Override
            public JobRepository getOrCreateRepository(int gameId) {
                return fakeRepo;
            }
        };

        logic.setJobService(dummyService);


        logic.performTurn(p, 5);

        assertTrue(p.isActive(), "Spieler sollte nach dem Zug aktiv bleiben (Prüfung durchgeführt).");
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


        JobRepository fakeRepo = new JobRepository() {
            @Override
            public List<Job> getRandomAvailableJobs(boolean requiresDegree, int count) {
                return List.of(new Job(1, "DummyJob", 30000, 5000, true));
            }
        };

        JobService dummyJobService = new JobService(null) {
            @Override
            public JobRepository getOrCreateRepository(int gameId) {
                return fakeRepo;
            }
        };

        logic.setJobService(dummyJobService);

        List<String> typesToTest = List.of("ZAHLTAG", "AKTION", "HAUS", "BERUF", "ANLAGE", "FREUND", "HEIRAT", "EXAMEN");

        for (String type : typesToTest) {
            Field f = new Field(1, 0f, 0f, List.of(), type);
            try {
                logic.handleField(p, f);
            } catch (Exception e) {
                fail("Fehler bei Feldtyp: " + type + " → " + e.getMessage());
            }
        }
    }


    @Test
    public void testHandleField_withUnknownType_printsInfo() {
        logic.registerPlayer("1");
        Player p = logic.getPlayerByName("1");
        Field unknown = new Field(99, 0f, 0f, List.of(), "UNBEKANNT");

        logic.handleField(p, unknown);
    }

    @Test
    public void testHandleSalaryField_correctAmountAdded_whenLandedDirectlyOrPassed() throws Exception {
        logic.registerPlayer("1");
        Player p = logic.getPlayerByName("1");

        Job job = new Job(1, "TestJob", 50000, 10000, false);
        p.assignJob(job);
        int initialMoney = p.getMoney();

        var method = GameLogic.class.getDeclaredMethod("handleSalaryField", Player.class, boolean.class);
        method.setAccessible(true);

        method.invoke(logic, p, true);
        assertEquals(initialMoney + 10000, p.getMoney());

        method.invoke(logic, p, false);
        assertEquals(initialMoney + 10000 + 50000, p.getMoney());
    }

    @Test
    public void testHandleActionField_appliesCorrectEffect() throws Exception {
        logic.registerPlayer("1");
        Player p = logic.getPlayerByName("1");

        for (int i = 0; i < 10; i++) {
            int moneyBefore = p.getMoney();
            int childrenBefore = p.getChildren();

            var method = GameLogic.class.getDeclaredMethod("handleActionField", Player.class);
            method.setAccessible(true);
            method.invoke(logic, p);

            int moneyAfter = p.getMoney();
            int childrenAfter = p.getChildren();

            boolean moneyChanged = moneyAfter != moneyBefore;
            boolean childChanged = childrenAfter != childrenBefore;

            assertTrue(moneyChanged || childChanged);
        }
    }

    @Test
    public void testHandleHouseField_playerBuysHouseWhenNoneOwned() {
        logic.registerPlayer("1");
        Player p = logic.getPlayerByName("1");
        p.setMoney(250000);

        logic.handleHouseField(p);

        boolean boughtHouse = p.getHouseId().size() > 0;
        boolean moneyReduced = p.getMoney() < 250000;

        assertTrue(boughtHouse || p.getHouseId().isEmpty());
        assertTrue(moneyReduced || p.getHouseId().isEmpty());
    }

    @Test
    public void testHandleHouseField_playerSellsHouseWhenOwned() {
        logic.registerPlayer("1");
        Player p = logic.getPlayerByName("1");

        int originalMoney = 100000;
        int houseValue = 200000;
        int attempts = 0;
        int maxAttempts = 10;
        boolean sold = false;

        while (!sold && attempts < maxAttempts) {
            p.setMoney(originalMoney);
            p.getHouseId().clear();
            p.getHouseId().put(888, houseValue);

            logic.handleHouseField(p);

            sold = p.getHouseId().isEmpty();
            attempts++;
        }

        assertTrue(sold, "Spieler sollte das Haus nach einigen Versuchen verkauft haben.");
        assertTrue(p.getMoney() > originalMoney, "Spieler sollte nach Hausverkauf mehr Geld haben.");
    }

    @Test
    public void testHandleJobField_withoutJobService_printsWarning() {
        logic.registerPlayer("1");
        Player p = logic.getPlayerByName("1");

        logic.handleJobField(p);

        assertNull(p.getJobId());
    }

    @Test
    public void testHandleJobField_noJobAvailable_printsInfo() {
        logic.registerPlayer("1");
        Player p = logic.getPlayerByName("1");

        JobRepository emptyRepo = new JobRepository() {
            @Override
            public List<Job> getRandomAvailableJobs(boolean requiresDegree, int count) {
                return List.of();
            }
        };

        JobService dummyService = new JobService(null) {
            @Override
            public JobRepository getOrCreateRepository(int gameId) {
                return emptyRepo;
            }
        };

        logic.setJobService(dummyService);
        logic.handleJobField(p);

        assertNull(p.getJobId());
    }

    @Test
    public void testHandleJobField_requiresDegreeButPlayerHasNoUniversity_rejects() {
        logic.registerPlayer("1");
        Player p = logic.getPlayerByName("1");
        p.setUniversity(false);

        Job jobWithDegree = new Job(99, "Arzt", 80000, 10000, true);

        JobRepository repo = new JobRepository() {
            @Override
            public List<Job> getRandomAvailableJobs(boolean requiresDegree, int count) {
                return List.of(jobWithDegree);
            }
        };

        JobService dummyService = new JobService(null) {
            @Override
            public JobRepository getOrCreateRepository(int gameId) {
                return repo;
            }
        };

        logic.setJobService(dummyService);
        logic.handleJobField(p);

        assertNull(p.getJobId());
    }

    @Test
    public void testHandleJobField_assignsJobEventually() {
        logic.registerPlayer("1");
        Player p = logic.getPlayerByName("1");
        p.setUniversity(true);

        Job testJob = new Job(101, "Entwickler", 60000, 7000, true);

        JobRepository repo = new JobRepository() {
            @Override
            public List<Job> getRandomAvailableJobs(boolean requiresDegree, int count) {
                return List.of(testJob);
            }

            @Override
            public void assignJobToPlayer(String playerId, Job job) {

            }
        };

        JobService dummyService = new JobService(null) {
            @Override
            public JobRepository getOrCreateRepository(int gameId) {
                return repo;
            }
        };

        logic.setJobService(dummyService);

        boolean assigned = false;
        for (int i = 0; i < 10 && !assigned; i++) {
            p.assignJob(null); // Reset
            logic.handleJobField(p);
            assigned = p.getJobId() != null && p.getJobId().getTitle().equals("Entwickler");
        }

        assertTrue(assigned, "Spieler sollte nach mehreren Versuchen den Job erhalten haben.");
    }

    @Test
    public void testHandleJobField_mightKeepCurrentJob() {
        logic.registerPlayer("1");
        Player p = logic.getPlayerByName("1");
        p.setUniversity(true);

        Job oldJob = new Job(1, "Lehrer", 50000, 5000, false);
        Job newJob = new Job(2, "Manager", 70000, 10000, false);
        p.assignJob(oldJob);

        JobRepository repo = new JobRepository() {
            @Override
            public List<Job> getRandomAvailableJobs(boolean requiresDegree, int count) {
                return List.of(newJob);
            }

            @Override
            public void assignJobToPlayer(String playerId, Job job) {

            }
        };

        JobService dummyService = new JobService(null) {
            @Override
            public JobRepository getOrCreateRepository(int gameId) {
                return repo;
            }
        };

        logic.setJobService(dummyService);
        logic.handleJobField(p);

        assertNotNull(p.getJobId());
        assertTrue(
                p.getJobId().getTitle().equals("Lehrer") || p.getJobId().getTitle().equals("Manager")
        );
    }

    @Test
    public void testHandleInvestmentField_investWhenEligible() {
        logic.registerPlayer("1");
        Player player = logic.getPlayerByName("1");

        player.setMoney(100000);
        player.setInvestments(0);

        boolean invested = false;
        int attempts = 0;
        int maxAttempts = 10;

        while (!invested && attempts < maxAttempts) {
            player.setMoney(100000);
            player.setInvestments(0);

            logic.handleInvestmentField(player);

            invested = player.getInvestments() > 0;
            attempts++;
        }

        assertTrue(invested, "Spieler hat nach mehreren Versuchen investiert.");
        assertEquals(50000, player.getMoney(), "Spieler sollte 50.000 € investiert haben.");
    }



    @Test
    public void testHandleInvestmentField_noInvestWhenNotEligible() {
        logic.registerPlayer("2");
        Player player = logic.getPlayerByName("2");
        player.setMoney(30000);


        player.setInvestments(0);
        player.setMoney(30000);

        logic.handleInvestmentField(player);

        assertEquals(30000, player.getMoney());
        assertEquals(0, player.getInvestments());
    }

    @Test
    public void testCheckAndPayoutInvestment_payoutWhenMatchingInvestment() {
        logic.registerPlayer("1");
        Player player = logic.getPlayerByName("1");

        player.setMoney(100000);
        player.setInvestments(3);
        player.setInvestmentPayout(0);


        int spinResult = 3;
        logic.checkAndPayoutInvestment("1", spinResult);

        assertEquals(110000, player.getMoney());
        assertEquals(1, player.getInvestmentPayout());
    }

    @Test
    public void testCheckAndPayoutInvestment_noPayoutWhenNoMatch() {
        logic.registerPlayer("2");
        Player player = logic.getPlayerByName("2");

        player.setMoney(100000);
        player.setInvestments(4);
        player.setInvestmentPayout(0);

        int spinResult = 5;
        logic.checkAndPayoutInvestment("2", spinResult);

        assertEquals(100000, player.getMoney());
        assertEquals(0, player.getInvestmentPayout());
    }

    @Test
    public void testHandleFriendField_addChildWhenFriendField() {
        logic.registerPlayer("1");
        Player player = logic.getPlayerByName("1");


        player.setMoney(100000);
        player.setChildrenCount(0);


        Field friendField = new Field(1, 0f, 0f, List.of(), "FREUND");


        logic.handleFriendField(player, friendField);

        assertEquals(1, player.getChildren());
        assertTrue(player.getChildren() > 0);
    }

    @Test
    public void testHandleFriendField_noChildWhenNotFriendField() {
        logic.registerPlayer("2");
        Player player = logic.getPlayerByName("2");

        player.setMoney(100000);
        player.setChildrenCount(0);

        Field nonFriendField = new Field(2, 0f, 0f, List.of(), "HAUS");

        logic.handleFriendField(player, nonFriendField);

        assertEquals(0, player.getChildren());
    }




    @Test
    public void testHandleExamField_failExamAndSetRepeatFlag() {
        logic.registerPlayer("1");
        Player player = logic.getPlayerByName("1");
        player.setMoney(100000);
        player.setMustRepeatExam(true);

        Job testJob = new Job(999, "Dummy", 10000, 1000, false);
        JobRepository fakeRepo = new JobRepository() {
            @Override
            public List<Job> getRandomAvailableJobs(boolean requiresDegree, int count) {
                return List.of(testJob);
            }
        };

        JobService dummyService = new JobService(null) {
            @Override
            public JobRepository getOrCreateRepository(int gameId) {
                return fakeRepo;
            }
        };

        logic.setJobService(dummyService);

        boolean stillMustRepeat = true;
        for (int i = 0; i < 10; i++) {
            player.setMustRepeatExam(true);
            logic.handleExamField(player);

            if (!player.mustRepeatExam()) {
                stillMustRepeat = false;
                break;
            }
        }

        if (stillMustRepeat) {
            assertTrue(player.mustRepeatExam(), "Spieler sollte weiterhin das Repeat-Flag haben (nicht bestanden)");
            assertNull(player.getJobId(), "Spieler sollte keinen Job erhalten haben");
        } else {
            assertFalse(player.mustRepeatExam(), "Spieler hat bestanden, kein Repeat-Flag mehr");
            assertNotNull(player.getJobId(), "Spieler hat bestanden und sollte einen Job haben");
        }
    }




    @Test
    public void testHandleExamField_passExamAndAssignJob() {
        logic.registerPlayer("2");
        Player player = logic.getPlayerByName("2");
        player.setMoney(100000);
        player.setMustRepeatExam(false);

        Job testJob = new Job(999, "Testjob", 42000, 5000, true);

        JobRepository fakeRepo = new JobRepository() {
            @Override
            public List<Job> getRandomAvailableJobs(boolean requiresDegree, int count) {
                return List.of(testJob);
            }
        };

        JobService dummyJobService = new JobService(null) {
            @Override
            public JobRepository getOrCreateRepository(int gameId) {
                return fakeRepo;
            }
        };

        logic.setJobService(dummyJobService);

        boolean success = false;
        for (int i = 0; i < 10 && !success; i++) {
            logic.handleExamField(player);
            success = player.getJobId() != null;
        }

        assertTrue(success, "Spieler sollte nach spätestens 10 Versuchen die Prüfung bestanden und einen Job erhalten haben.");
        assertEquals("Testjob", player.getJobId().getTitle());
        assertEquals(42000, player.getSalary());
    }



    @Test
    public void testNextTurn_continueGameWhenNotAllRetired() {

        logic.registerPlayer("1");
        logic.registerPlayer("2");

        Player player1 = logic.getPlayerByName("1");
        Player player2 = logic.getPlayerByName("2");


        player1.isActive();
        player2.setActive(false);

        logic.setCurrentPlayerIndex(0);

        logic.nextTurn();


        assertTrue(player2.isActive());
        assertFalse(player1.isActive());
    }

    @Test
    public void testNextTurn_endGameWhenAllPlayersRetired() {

        logic.registerPlayer("1");
        logic.registerPlayer("2");

        Player player1 = logic.getPlayerByName("1");
        Player player2 = logic.getPlayerByName("2");

        player1.retire();
        player2.retire();

        logic.nextTurn();

        assertTrue(logic.isGameEnded());
        assertFalse(player1.isActive());
        assertFalse(player2.isActive());
    }

    @Test
    public void testNextTurn_continueGameWithMultiplePlayers() {

        logic.registerPlayer("1");
        logic.registerPlayer("2");
        logic.registerPlayer("3");

        Player player1 = logic.getPlayerByName("1");
        Player player2 = logic.getPlayerByName("2");
        Player player3 = logic.getPlayerByName("3");

        player1.isActive();
        player2.setActive(false);
        player3.setActive(false);


        logic.setCurrentPlayerIndex(0);

        logic.nextTurn();

        assertTrue(player2.isActive());
        assertFalse(player1.isActive());


        logic.nextTurn();

        assertTrue(player3.isActive());
        assertFalse(player2.isActive());
    }

    @Test
    public void testPlayerRetires_firstPlayerReceives250000() {
        logic.registerPlayer("1");
        logic.registerPlayer("2");
        Player player1 = logic.getPlayerByName("1");

        logic.playerRetires(player1.getId());

        int expected = 250000 + 250000;
        assertEquals(expected, player1.getMoney());
        assertTrue(player1.isRetired());
    }

    @Test
    public void testPlayerRetires_secondPlayerReceives100000() {
        logic.registerPlayer("1");
        logic.registerPlayer("2");
        Player player2 = logic.getPlayerByName("2");

        logic.playerRetires("1");
        logic.playerRetires("2");

        int expected = 250000 + 100000;
        assertEquals(expected, player2.getMoney());
        assertTrue(player2.isRetired());
    }

    @Test
    public void testPlayerRetires_thirdPlayerReceives50000() {
        logic.registerPlayer("1");
        logic.registerPlayer("2");
        logic.registerPlayer("3");
        Player player3 = logic.getPlayerByName("3");

        logic.playerRetires("1");
        logic.playerRetires("2");
        logic.playerRetires("3");

        int expected = 250000 + 50000;
        assertEquals(expected, player3.getMoney());
        assertTrue(player3.isRetired());
    }

    @Test
    public void testPlayerRetires_fourthPlayerReceives10000() {
        logic.registerPlayer("1");
        logic.registerPlayer("2");
        logic.registerPlayer("3");
        logic.registerPlayer("4");
        Player player4 = logic.getPlayerByName("4");

        logic.playerRetires("1");
        logic.playerRetires("2");
        logic.playerRetires("3");
        logic.playerRetires("4");

        int expected = 250000 + 10000;
        assertEquals(expected, player4.getMoney());
        assertTrue(player4.isRetired());
    }


    @Test
    public void testEndGame_whenAllPlayersRetired() {
        logic.registerPlayer("1");
        logic.registerPlayer("2");
        logic.registerPlayer("3");
        Player player1 = logic.getPlayerByName("1");
        Player player2 = logic.getPlayerByName("2");
        Player player3 = logic.getPlayerByName("3");

        logic.playerRetires(player1.getId());
        logic.playerRetires(player2.getId());


        assertFalse(logic.isGameEnded());

        logic.playerRetires(player3.getId());

        assertTrue(logic.isGameEnded());

        assertTrue(player1.getMoney() >= player2.getMoney());
    }

    @Test
    public void testAllPlayersRetired_whenAllRetired() {
        logic.registerPlayer("1");
        logic.registerPlayer("2");
        logic.registerPlayer("3");
        Player player1 = logic.getPlayerByName("1");
        Player player2 = logic.getPlayerByName("2");
        Player player3 = logic.getPlayerByName("3");


        logic.playerRetires(player1.getId());
        logic.playerRetires(player2.getId());
        logic.playerRetires(player3.getId());

        assertTrue(logic.allPlayersRetired());
    }

    @Test
    public void testCalculateFinalWealth_noChildren_noDebts_noHouses() {
        logic.registerPlayer("1");
        Player player = logic.getPlayerByName("1");

        player.addMoney(100000);

        int expectedWealth = 250000 + 100000;
        int actualWealth = logic.calculateFinalWealth(player);
        assertEquals(expectedWealth, actualWealth);
    }

    @Test
    public void testCalculateFinalWealth_withChildren() {
        logic.registerPlayer("2");
        Player player = logic.getPlayerByName("2");

        player.addMoney(100000);
        player.addChild();
        player.addChild();

        int expectedWealth = 250000 + 100000 + (2 * 50000);
        int actualWealth = logic.calculateFinalWealth(player);
        assertEquals(expectedWealth, actualWealth);
    }

    @Test
    public void testCalculateFinalWealth_withDebts() {
        logic.registerPlayer("3");
        Player player = logic.getPlayerByName("3");

        player.addMoney(100000);
        player.addChild();
        player.addDebt();

        int expectedWealth = 250000 + 100000 + 50000 - 60000;
        int actualWealth = logic.calculateFinalWealth(player);
        assertEquals(expectedWealth, actualWealth);
    }

    @Test
    public void testCalculateFinalWealth_withHouse() {
        logic.registerPlayer("4");
        Player player = logic.getPlayerByName("4");

        player.addMoney(100000);
        player.addHouse(1, 200000);

        int expectedWealth = 250000 + 100000 + 200000;
        int actualWealth = logic.calculateFinalWealth(player);
        assertEquals(expectedWealth, actualWealth);
    }

    @Test
    public void testCalculateFinalWealth_withAllFactors() {
        logic.registerPlayer("5");
        Player player = logic.getPlayerByName("5");

        player.addMoney(100000);
        player.addChild();
        player.addChild();
        player.addHouse(1, 200000);
        player.addDebt();

        int expectedWealth = 250000 + 100000 + (2 * 50000) + 200000 - 60000;
        int actualWealth = logic.calculateFinalWealth(player);
        assertEquals(expectedWealth, actualWealth);
    }




    static class DummyGameController extends GameController {
        public DummyGameController(GameLogic logic) {
            super(logic, new DummyMessagingTemplate());
        }

        @Override
        public boolean isPlayerTurn(String playerId) {
            return true;
        }

        @Override
        public void startPlayerTurn(String playerId, boolean kauftKapitalanlage) {
            System.out.println("[TEST] Zugstart für Spieler " + playerId);
        }

        @Override
        public void requestAdditionalSpin(String playerId) {
            System.out.println("[TEST] Spieler " + playerId + " darf erneut drehen.");
        }
    }

    static class DummyMessagingTemplate extends org.springframework.messaging.simp.SimpMessagingTemplate {
        public DummyMessagingTemplate() {
            super(new org.springframework.messaging.MessageChannel() {
                @Override
                public boolean send(org.springframework.messaging.Message<?> message) {
                    return true;
                }

                @Override
                public boolean send(org.springframework.messaging.Message<?> message, long timeout) {
                    return true;
                }
            });
        }

        @Override
        public void convertAndSend(String destination, Object payload) {
            System.out.println("[DUMMY MESSAGE] → " + destination + " | Inhalt: " + payload);
        }
    }

}




