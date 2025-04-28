package GameTest;

import Game.GameLogic;
import Game.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.LinkedList;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

public class GameLogicTest {
    private GameLogicForTesting gameLogic;

    @BeforeEach
    void setUp() {
        gameLogic = new GameLogicForTesting();
        gameLogic.registerPlayer("player1");
    }


    @Test
    void registerPlayer_ShouldAddPlayer() {
        Player player = gameLogic.getCurrentPlayer();
        assertEquals("player1", player.getId());
    }

    @Test
    void handleUniversityChoice_ShouldAddFiveDebtsIfChosen() {
        Player player = gameLogic.getCurrentPlayer();
        gameLogic.handleUniversityChoice(player, true);
        assertEquals(5, player.getDebts());
    }

    @Test
    void handleUniversityChoice_ShouldNotAddDebtIfNotChosen() {
        Player player = gameLogic.getCurrentPlayer();
        gameLogic.handleUniversityChoice(player, false);
        assertEquals(0, player.getDebts());
    }

    @Test
    void handleMarriage_ShouldSetMarriedAndGiveLifeCard() {
        Player player = gameLogic.getCurrentPlayer();
        gameLogic.handleMarriage(player);

        assertTrue(player.isMarried());
        assertEquals(1, player.getLifeCards().size());
    }

    @Test
    void handleBaby_ShouldIncreaseChildrenAndGiveLifeCard() {
        Player player = gameLogic.getCurrentPlayer();
        gameLogic.handleBaby(player, 2);

        assertEquals(2, player.getChildrenCount());
        assertEquals(1, player.getLifeCards().size());
    }

    @Test
    void handleKreditaufnahme_ShouldAddDebtAndMoney() {
        Player player = gameLogic.getCurrentPlayer();
        gameLogic.handleCreditApplication(player);

        assertEquals(1, player.getDebts());
        assertEquals(30000, player.getMoney());
    }

    @Test
    void handleHauskauf_ShouldSetHouseAndReduceMoney() {
        Player player = gameLogic.getCurrentPlayer();
        gameLogic.handleHouseAcquirement(player, "Villa", 5000);

        assertEquals("Villa", player.getHouse());
        assertEquals(5000, player.getMoney());
    }

    @Test
    void playerRetires_ShouldSetRetiredTrue() {
        Player player = gameLogic.getCurrentPlayer();
        gameLogic.playerRetires(player);

        assertTrue(player.isRetired());
    }

    @Test
    void allPlayersRetired_ShouldEndGame() {
        Player player = gameLogic.getCurrentPlayer();
        gameLogic.playerRetires(player);
        gameLogic.nextTurn();

        assertTrue(gameLogic.isGameEnded());
    }
    @Test
    void nextTurn_ShouldSkipRetiredPlayers() {
        gameLogic.registerPlayer("player2");
        Player player1 = gameLogic.getCurrentPlayer();
        gameLogic.playerRetires(player1);
        gameLogic.nextTurn();
        Player currentPlayer = gameLogic.getCurrentPlayer();
        assertEquals("player2", currentPlayer.getId());
    }

    @Test
    void calculatePlayerWealth_ShouldConsiderMoneyDebtAndLifeCards() {
        Player player = gameLogic.getCurrentPlayer();
        player.addMoney(50000);
        player.addDebt();
        player.addLifeCard("TestLifeCard");
        int wealth = invokeCalculatePlayerWealth(player);

        // Erwartung:
        // 10000 (Startgeld) + 50000 - 25000 (Schulden) + 10000 (LifeCard)
        assertEquals(45000, wealth);
    }

    // Helfer, weil die Methode private ist:
    private int invokeCalculatePlayerWealth(Player player) {
        return new GameLogicForTesting().calculatePlayerWealth(player);
    }

    static class GameLogicForTesting extends GameLogic {

        @Override
        public String drawLifeCard() {
            return super.drawLifeCard();
        }

        @Override
        public String drawShareJoyCard() {
            return super.drawShareJoyCard();
        }

        @Override
        public int calculatePlayerWealth(Player player) {
            return super.calculatePlayerWealth(player);
        }

        @Override
        public void setLifeCardsDeck(Queue<String> deck) {
            super.setLifeCardsDeck(deck);
        }

        @Override
        public void setShareJoyCardsDeck(Queue<String> deck) {
            super.setShareJoyCardsDeck(deck);
        }
    }

    @Test
    void drawLifeCard_ShouldReturnStandardLife_WhenDeckEmpty() {
        Player player = gameLogic.getCurrentPlayer();
        gameLogic.handleMarriage(player); // Eine Karte ziehen, damit Deck leer geprüft wird
        assertNotNull(player.getLifeCards().get(0));
    }

    @Test
    void drawLifeCard_ShouldReturnSpecificCard_WhenDeckNotEmpty() {

        gameLogic.registerPlayer("player3");

        // Trick: Kartenstapel vorbereiten
        LinkedList<String> deck = new LinkedList<>();
        deck.add("CustomLifeCard");
        gameLogic.setLifeCardsDeck(deck);

        Player player = gameLogic.getCurrentPlayer();
        gameLogic.handleMarriage(player); // Heirat = 1x LifeCard ziehen

        assertEquals("CustomLifeCard", player.getLifeCards().get(0));
    }

    @Test
    void drawShareJoyCard_ShouldReturnStandard_WhenDeckEmpty() {

        assertEquals("Standard-Freude",gameLogic.drawShareJoyCard());
    }

    @Test
    void drawShareJoyCard_ShouldReturnSpecificCard_WhenDeckNotEmpty() {

        LinkedList<String> deck = new LinkedList<>();
        deck.add("SpecialShareJoy");
        gameLogic.setShareJoyCardsDeck(deck);

        assertEquals("SpecialShareJoy", gameLogic.drawShareJoyCard());
    }
}
