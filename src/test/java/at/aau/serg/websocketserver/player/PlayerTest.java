package at.aau.serg.websocketserver.player;

import at.aau.serg.websocketserver.session.job.Job;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    private Player player;

    @BeforeEach
    void setup() {
        player = new Player("Tester");
    }

    @Test
    void shouldAddAndRemoveMoney() {
        player.addMoney(1000);
        assertEquals(1000, player.getMoney());

        player.removeMoney(500);
        assertEquals(500, player.getMoney());
    }

    @Test
    void shouldHandleLoanCorrectly() {
        player.takeLoan();
        assertEquals(1, player.getDebts());
        assertEquals(20000, player.getMoney());
    }

    @Test
    void shouldRepayLoan() {
        player.takeLoan();
        player.addMoney(1000);
        player.repayLoan(); // sollte nichts abziehen, weil costPerLoan = 0
        assertEquals(0, player.getDebts());
    }

    @Test
    void shouldHandleMarriageCorrectly() {
        player.marry();
        assertTrue(player.isMarried());

        Exception ex = assertThrows(IllegalStateException.class, player::marry);
        assertTrue(ex.getMessage().contains("bereits verheiratet"));
    }

    @Test
    void shouldAddChildWithCarCheck() {
        player.addChildrenWithCarCheck(2);
        assertEquals(2, player.getChildren());
        assertEquals(2, player.getAutoPassengers());
    }

    @Test
    void shouldThrowWhenTooManyChildren() {
        player.addChildrenWithCarCheck(4);
        Exception ex = assertThrows(IllegalStateException.class, () ->
                player.addChildrenWithCarCheck(1));
        assertTrue(ex.getMessage().contains("Nicht genug Platz"));
    }

    @Test
    void shouldAssignAndClearJob() {
        Job job = new Job(1, "Tester", 30000, 0, false);
        player.assignJob(job);

        assertTrue(player.hasJob());
        assertEquals(job, player.getJobId());

        player.clearJob();
        assertFalse(player.hasJob());
    }

    @Test
    void shouldHandleEventKind() {
        player.handleEvent("kind");
        assertEquals(1, player.getChildren());
    }

    @Test
    void handleUnknownEvent_ShouldThrow() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                player.handleEvent("unbekannt"));
        assertTrue(ex.getMessage().contains("Unbekanntes Ereignis"));
    }
}
