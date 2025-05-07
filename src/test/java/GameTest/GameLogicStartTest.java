package GameTest;

import Game.GameLogic;
import Game.Player;
import at.aau.serg.websocketserver.session.Job;
import at.aau.serg.websocketserver.session.JobRepository;
import at.aau.serg.websocketserver.session.JobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class GameLogicStartTest {

    private GameLogicForTesting gameLogic;

    @BeforeEach
    void setUp() {
        gameLogic = new GameLogicForTesting();

        // JobService-Mock
        MockJobService mockService = new MockJobService();
        mockService.setMockJobs(List.of(
                new Job(1, "Programmierer", 50000, 10000, false)
        ));
        gameLogic.setJobService(mockService);

        gameLogic.registerPlayer("p1");
        gameLogic.prepareGameStart(List.of("rot"));
    }

    @Test
    void prepareGameStart_shouldAssignCarAndMoney() {
        Player p = gameLogic.getCurrentPlayer();
        assertEquals(10000, p.getMoney());
        assertEquals("rot", p.getCarColor());
    }

    @Test
    void determineFirstPlayer_shouldPickHighestSpinner() {
        gameLogic.registerPlayer("p2");
        Map<String, Integer> spins = Map.of("p1", 4, "p2", 9);
        gameLogic.determineFirstPlayer(spins);

        assertEquals("p2", gameLogic.getCurrentPlayer().getId());
    }

    @Test
    void handleGameStartChoice_university_shouldAddDebts() {
        gameLogic.handleGameStartChoice(0, "p1", true);
        assertEquals(5, gameLogic.getCurrentPlayer().getDebts());
    }

    @Test
    void handleGameStartChoice_job_shouldAssignJob() {
        // Arrange
        int gameId = 99;
        GameLogic logic = new GameLogic();

        // Job vorbereiten
        Job job = new Job(1, "Programmierer", 30000, 10000, false);
        MockJobService jobService = new MockJobService();
        jobService.setMockJobs(List.of(job));
        logic.setJobService(jobService);

        // Einen Spieler registrieren
        logic.registerPlayer("pX");
        logic.prepareGameStart(List.of("grün")); // Nur 1 Spieler → 1 Farbe

        // Act
        logic.handleGameStartChoice(gameId, "pX", false);

        // Assert
        JobRepository repo = jobService.getOrCreateRepository(gameId);
        Optional<Job> assigned = repo.getCurrentJobForPlayer("pX");

        assertTrue(assigned.isPresent(), "Job sollte zugewiesen sein.");
        assertEquals("Programmierer", assigned.get().getTitle());
    }
    @Test
    void nextTurn_ShouldSkipRetiredPlayer() {
        GameLogic logic = new GameLogic();
        logic.registerPlayer("A");
        logic.registerPlayer("B");
        logic.prepareGameStart(List.of("rot", "blau"));

        Player playerA = logic.getPlayers().get(0);
        playerA.retire(); // A wird übersprungen

        Player before = logic.getCurrentPlayer(); // A
        logic.nextTurn();
        Player after = logic.getCurrentPlayer();  // B

        assertNotEquals(before.getId(), after.getId());
        assertEquals("B", after.getId());
    }
    @Test
    void calculateWealth_ShouldSubtractDebtsAndAddLifeCards() {
        Player p = new Player("X");
        p.addMoney(50000);
        p.addDebt();
        p.addDebt(); // 2x 25000
        p.addLifeCard("Card1");
        p.addLifeCard("Card2");

        class TestGameLogic extends GameLogic {
            public int exposedCalculateWealth(Player player) {
                return calculatePlayerWealth(player);
            }
        }

        int result = new TestGameLogic().exposedCalculateWealth(p);
        assertEquals(20000, result); // 50000 - 50000 + 20000
    }

    @Test
    void performTurn_ShouldAdvanceAndCallNextTurn() {
        class TestGameLogic extends GameLogic {
            boolean called = false;

            @Override
            public void nextTurn() {
                called = true;
            }
        }

        TestGameLogic logic = new TestGameLogic();
        logic.registerPlayer("p1");
        logic.prepareGameStart(List.of("rot"));
        Player p = logic.getCurrentPlayer();

        logic.performTurn(p, 5);

        assertTrue(logic.called, "nextTurn() sollte aufgerufen worden sein.");
    }

    @Test
    void playerRetires_ShouldTriggerEndGameWhenAllRetired() {
        GameLogic logic = new GameLogic();
        logic.registerPlayer("A");
        logic.prepareGameStart(List.of("rot"));
        assertFalse(logic.isGameEnded());

        logic.playerRetires("A");
        assertTrue(logic.isGameEnded());
    }
    @Test
    void startChoice_ShouldHandleUniversityAndDirectJob() {
        GameLogic logic = new GameLogic();
        logic.setJobService(new MockJobService());
        logic.registerPlayer("A");
        logic.prepareGameStart(List.of("blau"));

        logic.handleGameStartChoice(1, "A", true);  // Uni → 5 Schulden

        int actualDebts = logic.getPlayers().stream()
                .filter(p -> p.getId().equals("A"))
                .findFirst()
                .orElseThrow()
                .getDebts();

        assertEquals(5, actualDebts);
    }

    @Test
    void determineFirstPlayer_ShouldPickHighestValue() {
        GameLogic logic = new GameLogic();
        logic.registerPlayer("A");
        logic.registerPlayer("B");

        Map<String, Integer> spins = Map.of("A", 3, "B", 9);
        logic.determineFirstPlayer(spins);

        assertEquals("B", logic.getCurrentPlayer().getId());
    }





    @Test
    void playerRetires_shouldResetDebtsClearJobAndAddBonus() {
        Player p = gameLogic.getCurrentPlayer();
        p.setJob("Arzt");
        p.addDebt();
        p.setHouse("Villa");
        p.setHouseValue(80000);
        p.addChild();
        p.addChild();
        gameLogic.playerRetires("p1");

        assertTrue(p.isRetired());
        assertNull(p.getJob());
        assertEquals(0, p.getDebts());
        assertNull(p.getHouse());
        assertEquals(10000 + 80000 - 25000 + (2 * 10000), p.getMoney()); // Startgeld + Haus - Schulden + Kinderbonus
    }

    // 🧪 Hilfsklassen und Methoden

    static class GameLogicForTesting extends GameLogic {
        @Override
        public int calculatePlayerWealth(Player player) {
            return super.calculatePlayerWealth(player);
        }
    }

    static class MockJobService extends JobService {
        private final Map<Integer, JobRepository> repos = new HashMap<>();
        private List<Job> mockJobs = new ArrayList<>();

        public MockJobService() {
            super(new ObjectProvider<JobRepository>() {
                @Override
                public JobRepository getObject(Object... args) {
                    return new JobRepository();
                }

                @Override
                public JobRepository getObject() {
                    return new JobRepository();
                }

                @Override
                public JobRepository getIfAvailable() {
                    return new JobRepository();
                }

                @Override
                public JobRepository getIfUnique() {
                    return new JobRepository();
                }
            });
// Ein einfacher Fallback-Provider für den Test
        }

        public void setMockJobs(List<Job> jobs) {
            this.mockJobs = jobs;
        }

        @Override
        public JobRepository getOrCreateRepository(int gameId) {
            return repos.computeIfAbsent(gameId, id -> new MockJobRepository(mockJobs));
        }

        @Override
        public void removeRepository(int gameId) {
            repos.remove(gameId);
        }
    }

    static class MockJobRepository extends JobRepository {
        private final List<Job> jobs;

        public MockJobRepository(List<Job> jobs) {
            this.jobs = new ArrayList<>(jobs);
        }

        @Override
        public List<Job> getRandomAvailableJobs(boolean hasDegree, int count) {
            return jobs.subList(0, Math.min(count, jobs.size()));
        }

        @Override
        public void assignJobToPlayer(String playerName, Job job) {
            job.assignJobTo(playerName);
        }

        @Override
        public Optional<Job> getCurrentJobForPlayer(String playerName) {
            return jobs.stream().filter(j -> playerName.equals(j.getAssignedToPlayerName())).findFirst();
        }
    }
}



