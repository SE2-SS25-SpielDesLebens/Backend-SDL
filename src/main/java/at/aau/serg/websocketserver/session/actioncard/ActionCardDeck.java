package at.aau.serg.websocketserver.session.actioncard;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ActionCardDeck {
    private final List<ActionCard> allCards;
    private List<ActionCard> deck;
    private int currentIndex;

    /**
     * Constructs the deck by loading cards from a JSON resource and shuffling them.
     */
    public ActionCardDeck() {
        this.allCards = loadCardsFromJson();
        resetDeck();
    }

    /**
     * Pulls the next card from the deck. Resets and reshuffles when all cards have been drawn.
     * @return the next ActionCard
     */
    public ActionCard pull() {
        if (currentIndex >= deck.size()) {
            resetDeck();
        }
        return deck.get(currentIndex++);
    }

    /**
     * Resets the deck to contain all cards in random order and resets the draw pointer.
     */
    private void resetDeck() {
        // Copy all cards and shuffle
        deck = new ArrayList<>(allCards);
        Collections.shuffle(deck);
        currentIndex = 0;
    }

    /**
     * Loads the list of ActionCards from the actionCards.json resource file.
     * @return list of all ActionCards
     */
    private List<ActionCard> loadCardsFromJson() {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<ActionCard>>() {}.getType();
        InputStream is = getClass().getResourceAsStream("/actionCards.json");
        if (is == null) {
            throw new IllegalStateException("actionCards.json resource not found in /actioncard/");
        }
        try (InputStreamReader reader = new InputStreamReader(is)) {
            return gson.fromJson(reader, listType);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load or parse actionCards.json", e);
        }
    }

    /**
     * Returns the number of cards remaining before a reset.
     * @return remaining cards count
     */
    public int remaining() {
        return deck.size() - currentIndex;
    }

    /**
     * Returns the total number of cards in the deck.
     * @return total card count
     */
    public int size() {
        return allCards.size();
    }
}
