package at.aau.serg.websocketserver.player;

import at.aau.serg.websocketserver.session.Job;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player("P1");
    }

    @Test
    void initialState_ShouldBeCorrect() {
        assertEquals("P1", player.getId());
        assertEquals(0, player.getMoney());
        assertEquals(0, player.getDebts());
        assertEquals(0, player.getSalary());
        assertEquals(0, player.getInvestments());
        assertFalse(player.getEducation());
        assertFalse(player.isMarried());
        assertFalse(player.isRetired());
        assertFalse(player.isActive());
        assertFalse(player.isHost());
        assertNull(player.getJobId());
        assertEquals(0, player.getChildren());
        assertEquals(0, player.getFieldID());
        assertTrue(player.getHouseId().isEmpty());
    }

    @Test
    void addAndRemoveMoney_ShouldUpdateBalance() {
        player.addMoney(10000);
        assertEquals(10000, player.getMoney());

        player.removeMoney(3000);
        assertEquals(7000, player.getMoney());
    }

    @Test
    void debtManagement_ShouldWorkCorrectly() {
        player.addDebt();
        player.addDebt();
        assertEquals(2, player.getDebts());

        player.resetDebts();
        assertEquals(0, player.getDebts());
    }

    @Test
    void takeLoan_ShouldAddMoneyAndDebt() {
        player.takeLoan();
        assertEquals(20000, player.getMoney());
        assertEquals(1, player.getDebts());
    }

    @Test
    void repayLoan_ShouldReduceDebtAndMoney_WhenSufficientFunds() {
        player.takeLoan(); // +20000 +1 debt
        player.addMoney(10000); // total 30000

        player.repayLoan(); // -25000, -1 debt

        assertEquals(5000, player.getMoney());
        assertEquals(0, player.getDebts());
    }

    @Test
    void repayLoan_ShouldDoNothing_WhenNotEnoughMoney() {
        player.takeLoan();
        player.removeMoney(15000); // now 5000
        player.repayLoan();

        assertEquals(5000, player.getMoney());
        assertEquals(1, player.getDebts());
    }

    @Test
    void jobAssignmentAndClear_ShouldWork() {
        Job job = new Job(1, "Entwickler", 50000, 10000, false);
        player.assignJob(job);

        assertEquals(job, player.getJobId());

        player.clearJob();
        assertNull(player.getJobId());
    }

    @Test
    void houseManagement_ShouldAddAndRemove() {
        player.getHouseId().put(101, 1);
        player.getHouseId().put(102, 1);
        assertEquals(2, player.getHouseId().size());

        player.removeHouse(101);
        assertEquals(1, player.getHouseId().size());
        assertFalse(player.getHouseId().containsKey(101));
    }

    @Test
    void statusChanges_ShouldWorkCorrectly() {
        player.marry();
        assertTrue(player.getRelationship());

        player.addChild();
        player.addChild();
        assertEquals(2, player.getChildren());

        player.retire();
        assertTrue(player.isRetired());
        assertFalse(player.isActive());
    }

    @Test
    void carColorAndHostStatus_SetAndGetCorrectly() {
        player.setCarColor("gelb");
        assertEquals("gelb", player.getCarColor());

        player.setHost(true);
        assertTrue(player.isHost());
    }

    @Test
    void fieldId_ShouldBeSetAndReturned() {
        player.setFieldId(42);
        assertEquals(42, player.getFieldID());
    }
}


