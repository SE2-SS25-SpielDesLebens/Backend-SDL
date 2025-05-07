package GameTest;

import Game.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player("testPlayer");
    }

    @Test
    void player_ShouldBeInitializedCorrectly() {
        assertEquals("testPlayer", player.getId());
        assertEquals(0, player.getMoney());
        assertEquals(0, player.getDebts());
        assertFalse(player.isMarried());
        assertFalse(player.isRetired());
        assertEquals(0, player.getChildren());
        assertTrue(player.getLifeCards().isEmpty());
        assertTrue(player.getShareJoyCards().isEmpty());
        assertNull(player.getJob());
        assertNull(player.getAcademicJob());
        assertNull(player.getHouse());
        assertEquals(0, player.getHouseValue());
        assertNull(player.getCarColor());
    }

    @Test
    void money_ShouldBeAddedAndRemovedCorrectly() {
        player.addMoney(10000);
        assertEquals(10000, player.getMoney());

        player.removeMoney(3000);
        assertEquals(7000, player.getMoney());
    }

    @Test
    void debts_ShouldBeAddedAndResetCorrectly() {
        player.addDebt();
        player.addDebt();
        assertEquals(2, player.getDebts());

        player.resetDebts();
        assertEquals(0, player.getDebts());
    }

    @Test
    void lifeCards_ShouldBeStoredCorrectly() {
        player.addLifeCard("Reise nach Paris");
        player.addLifeCard("Hausbau");

        assertEquals(2, player.getLifeCards().size());
        assertTrue(player.getLifeCards().contains("Reise nach Paris"));
    }

    @Test
    void shareJoyCards_ShouldBeStoredCorrectly() {
        player.addShareJoyCard("Spende");
        assertEquals(1, player.getShareJoyCards().size());
        assertEquals("Spende", player.getShareJoyCards().get(0));
    }

    @Test
    void jobAssignment_ShouldWorkAndBeClearable() {
        player.setJob("Polizist");
        player.setAcademicJob("Arzt");

        assertEquals("Polizist", player.getJob());
        assertEquals("Arzt", player.getAcademicJob());

        player.clearJob();
        assertNull(player.getJob());
        assertNull(player.getAcademicJob());
    }

    @Test
    void house_ShouldBeSetAndRemovedCorrectly() {
        player.setHouse("Villa");
        player.setHouseValue(80000);

        assertEquals("Villa", player.getHouse());
        assertEquals(80000, player.getHouseValue());

        player.removeHouse();
        assertNull(player.getHouse());
        assertEquals(0, player.getHouseValue());
    }

    @Test
    void marriage_ShouldUpdateState() {
        assertFalse(player.isMarried());
        player.marry();
        assertTrue(player.isMarried());
    }

    @Test
    void children_ShouldIncreaseCorrectly() {
        assertEquals(0, player.getChildren());
        player.addChild();
        player.addChild();
        assertEquals(2, player.getChildren());
    }

    @Test
    void retirement_ShouldUpdateState() {
        assertFalse(player.isRetired());
        player.retire();
        assertTrue(player.isRetired());
    }

    @Test
    void carColor_ShouldBeSetAndRetrieved() {
        player.setCarColor("blau");
        assertEquals("blau", player.getCarColor());
    }
}

