package at.aau.serg.websocketserver.player;

import at.aau.serg.websocketserver.session.job.Job;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player("TestPlayer");
    }

    @Test
    void testInitialState() {
        assertEquals("TestPlayer", player.getId());
        assertEquals(0, player.getMoney());
        assertEquals(0, player.getDebts());
        assertFalse(player.isMarried());
        assertFalse(player.isRetired());
        assertFalse(player.isActive());
        assertFalse(player.isHost());
        assertEquals(0, player.getChildren());
    }

    // 💰 Geld
    @Test
    void shouldAddAndRemoveMoney() {
        player.addMoney(10000);
        assertEquals(10000, player.getMoney());

        player.removeMoney(3000);
        assertEquals(7000, player.getMoney());
    }

    // 💳 Schulden
    @Test
    void shouldTakeLoan() {
        player.takeLoan();
        assertEquals(20000, player.getMoney());
        assertEquals(1, player.getDebts());
    }

    @Test
    void shouldRepayLoanIfPossible() {
        player.takeLoan(); // 1 debt, +20000
        player.repayLoan(); // repay: no cost, debt reset
        assertEquals(0, player.getDebts());
    }

    @Test
    void shouldNotRepayLoanIfMoneyTooLow() {
        player.takeLoan(); // +20000
        player.removeMoney(20000); // = 0
        player.repayLoan(); // nothing happens
        assertEquals(0, player.getDebts());
    }

    @Test
    void shouldResetDebts() {
        player.takeLoan();
        player.resetDebts();
        assertEquals(0, player.getDebts());
    }

    // 💼 Job
    @Test
    void shouldAssignAndClearJob() {
        Job job = new Job(1, "Tester", 25000, 0, false);
        player.assignJob(job);
        assertTrue(player.hasJob());
        player.clearJob();
        assertFalse(player.hasJob());
    }

    // 👶 Familie
    @Test
    void shouldMarryPlayer() {
        player.marry();
        assertTrue(player.isMarried());
    }

    @Test
    void shouldThrowIfMarriedAgain() {
        player.marry();
        assertThrows(IllegalStateException.class, player::marry);
    }

    @Test
    void shouldAddChildrenWithCarCheck() {
        player.addChildrenWithCarCheck(2);
        assertEquals(2, player.getChildren());
        assertEquals(2, player.getAutoPassengers());
    }

    @Test
    void shouldThrowWhenTooManyChildren() {
        player.addChildrenWithCarCheck(4);
        assertThrows(IllegalStateException.class, () -> player.addChildrenWithCarCheck(2));
    }

    @Test
    void canHaveMoreChildren() {
        assertTrue(player.canHaveMoreChildren(1));
        player.addChildrenWithCarCheck(4);
        assertFalse(player.canHaveMoreChildren(1));
    }

    // 🐾 Freund, Tier, Zwilling
    @Test
    void shouldAddPassengerFriend() {
        player.addPassengerWithLimit("Freund", 1);
        assertEquals(1, player.getAutoPassengers());
    }

    @Test
    void shouldThrowIfTooManyPassengers() {
        player.addPassengerWithLimit("Tier", 5);
        assertThrows(IllegalStateException.class, () -> player.addPassengerWithLimit("Freund", 1));
    }

    // 🚘 Auto
    @Test
    void canAddPassengers() {
        assertFalse(player.canAddPassengers(1)); // 0 passengers, 1 is OK
        player.addPassenger(5);
        assertTrue(player.canAddPassengers(1)); // would exceed
    }

    // 🏠 Häuser
    @Test
    void shouldAddAndRemoveHouse() {
        player.addHouse(101, 50000);
        Map<Integer, Integer> houses = player.getHouseId();
        assertEquals(1, houses.size());
        assertEquals(50000, houses.get(101));

        player.removeHouse(101);
        assertTrue(player.getHouseId().isEmpty());
    }

    // 🎓 Studium
    @Test
    void shouldHandleDegreeStatus() {
        player.setDegree(true);
        assertTrue(player.hasDegree());
    }

    @Test
    void shouldRepeatExamStatus() {
        player.setMustRepeatExam(true);
        assertTrue(player.mustRepeatExam());
    }

    // 🧓 Rente
    @Test
    void shouldRetire() {
        player.retire();
        assertTrue(player.isRetired());
        assertFalse(player.isActive());
    }

    // 🧪 Event Dispatcher
    @Test
    void handleEventKind() {
        player.handleEvent("kind");
        assertEquals(1, player.getChildren());
    }

    @Test
    void handleEventMarriage() {
        player.handleEvent("heirat");
        assertTrue(player.isMarried());
    }

    @Test
    void handleEventUnknownShouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> player.handleEvent("blabla"));
    }

    @Test
    void handleEventFriendIncreasesPassengers() {
        player.handleEvent("freund");
        assertEquals(1, player.getAutoPassengers());
    }

    @Test
    void handleEventTwinsIncreasesPassengers() {
        player.handleEvent("zwilling");
        assertEquals(2, player.getAutoPassengers());
        assertEquals(2, player.getChildren());
    }

    @Test
    void handleEventTier() {
        player.handleEvent("tier");
        assertEquals(1, player.getAutoPassengers());
    }

    // 🧪 Feldposition
    @Test
    void shouldSetAndGetFieldId() {
        player.setFieldId(42);
        assertEquals(42, player.getFieldID());
    }

}
