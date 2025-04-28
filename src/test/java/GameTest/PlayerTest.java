package GameTest;

import Game.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerTest {
    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player("testPlayer");
    }

    @Test
    void player_ShouldBeInitializedCorrectly() {
        assertAll(
                () -> assertEquals("testPlayer", player.getId()),
                () -> assertEquals(10000, player.getMoney()),
                () -> assertFalse(player.isMarried()),
                () -> assertFalse(player.isRetired()),
                () -> assertEquals(0, player.getDebts()),
                () -> assertEquals(0, player.getChildrenCount()),
                () -> assertTrue(player.getLifeCards().isEmpty()),
                () -> assertTrue(player.getShareJoyCards().isEmpty())
        );
    }

    @Test
    void addAndRemoveMoney_ShouldChangeBalance() {
        player.addMoney(5000);
        assertEquals(15000, player.getMoney());

        player.removeMoney(2000);
        assertEquals(13000, player.getMoney());
    }

    @Test
    void addDebt_ShouldIncreaseDebtsByOne() {
        player.addDebt();
        player.addDebt();
        assertEquals(2, player.getDebts());
    }

    @Test
    void marry_ShouldSetMarriedTrue() {
        player.marry();
        assertTrue(player.isMarried());
    }

    @Test
    void addChild_ShouldIncreaseChildrenCount() {
        player.addChild();
        player.addChild();
        assertEquals(2, player.getChildrenCount());
    }

    @Test
    void retire_ShouldSetRetiredTrue() {
        player.retire();
        assertTrue(player.isRetired());
    }

    @Test
    void addLifeCardAndShareJoyCard_ShouldStoreCards() {
        player.addLifeCard("Travel");
        player.addShareJoyCard("Donate");

        assertAll(
                () -> assertEquals(1, player.getLifeCards().size()),
                () -> assertEquals(1, player.getShareJoyCards().size()),
                () -> assertEquals("Travel", player.getLifeCards().get(0)),
                () -> assertEquals("Donate", player.getShareJoyCards().get(0))
        );
    }
}
