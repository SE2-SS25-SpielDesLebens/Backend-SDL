package at.aau.serg.websocketserver.session.actioncard;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ActionCardTest {

    @Test
    void testConstructorAndGetters() {
        int id = 1;
        String handle = "event123";
        String headline = "Breaking News";
        String action = "Some action text";
        String imageName = "image.png";
        String[] reactions = {"Yes", "No"};

        ActionCard card = new ActionCard(id, handle, headline, action, imageName, reactions);

        assertEquals(id, card.getId());
        assertEquals(handle, card.getHandle());
        assertEquals(headline, card.getHeadline());
        assertEquals(action, card.getAction());
        assertEquals(imageName, card.getImageName());
        assertArrayEquals(reactions, card.getReactions());
    }

    @Test
    void testSetters() {
        ActionCard card = new ActionCard(0, "", "", "", "", new String[0]);

        card.setId(42);
        card.setHandle("updateHandle");
        card.setHeadline("New Headline");
        card.setAction("Updated action");
        card.setImageName("updated.png");
        card.setReactions(new String[]{"Like", "Dislike"});

        assertEquals(42, card.getId());
        assertEquals("updateHandle", card.getHandle());
        assertEquals("New Headline", card.getHeadline());
        assertEquals("Updated action", card.getAction());
        assertEquals("updated.png", card.getImageName());
        assertArrayEquals(new String[]{"Like", "Dislike"}, card.getReactions());
    }
}
