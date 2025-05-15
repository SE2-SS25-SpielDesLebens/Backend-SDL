package at.aau.serg.websocketserver.actioncard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ActionCardDeckTest {
    private ActionCardDeck deck;

    @BeforeEach
    void setUp() {
        deck = new ActionCardDeck();
    }

    @Test
    void testSizeMatchesJson() {
        int size = deck.size();
        assertTrue(size > 0, "Deck should contain at least one card");
    }

    @Test
    void testPullReturnsNonNullAndDecreasesRemaining() {
        int initialRemaining = deck.remaining();
        ActionCard card = deck.pull();
        assertNotNull(card, "Pulled card should not be null");
        assertEquals(initialRemaining - 1, deck.remaining(), "Remaining should decrease by 1 after pull");
    }

    @Test
    void testDrawAllUniqueThenResets() {
        int total = deck.size();
        Set<Integer> ids = new HashSet<>();
        // draw all cards
        for (int i = 0; i < total; i++) {
            ActionCard c = deck.pull();
            ids.add(c.getId());
        }
        // we should have drawn exactly 'total' unique cards
        assertEquals(total, ids.size(), "Should draw each card exactly once before reset");

        // after drawing all, remaining should be 0
        assertEquals(0, deck.remaining(), "No cards should remain after drawing all");

        // next pull triggers a reset
        ActionCard next = deck.pull();
        assertNotNull(next, "After reset pull should still return a card");
        assertEquals(total - 1, deck.remaining(), "After reset and one pull, remaining should be size-1");
    }

    @Test
    void testMultipleResetsMaintainSize() {
        int total = deck.size();
        // perform multiple full cycles
        for (int cycle = 0; cycle < 3; cycle++) {
            for (int i = 0; i < total; i++) {
                deck.pull();
            }
            // after each full cycle, draw one more to reset
            ActionCard c = deck.pull();
            assertNotNull(c);
            assertEquals(total - (1 + cycle), deck.remaining(), "Remaining after cycle " + cycle + " reset should be size-(1+cycle)");
        }
    }
}