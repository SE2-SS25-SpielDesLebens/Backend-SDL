
package at.aau.serg.websocketserver.messaging.dtos;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JobMessageTest {

    @Test
    void allArgsConstructorAndGetters() {
        JobMessage msg = new JobMessage(
                1,
                "Entwickler",
                4500,
                250,
                true,
                false,
                99
        );  // :contentReference[oaicite:0]{index=0}:contentReference[oaicite:1]{index=1}

        assertEquals(1,      msg.getJobId(),         "jobId sollte korrekt gesetzt sein");
        assertEquals("Entwickler", msg.getTitle(),    "title sollte korrekt gesetzt sein");
        assertEquals(4500,   msg.getSalary(),        "salary sollte korrekt gesetzt sein");
        assertEquals(250,    msg.getBonusSalary(),   "bonusSalary sollte korrekt gesetzt sein");
        assertTrue (msg.isRequiresDegree(),          "requiresDegree sollte true sein");
        assertFalse(msg.isTaken(),                   "taken sollte false sein");
        assertEquals(99,     msg.getGameId(),        "gameId sollte korrekt gesetzt sein");
    }

    @Test
    void noArgsConstructorAndSetters() {
        JobMessage msg = new JobMessage();
        msg.setJobId(2);
        msg.setTitle("Tester");
        msg.setSalary(3000);
        msg.setBonusSalary(150);
        msg.setRequiresDegree(false);
        msg.setTaken(true);
        msg.setGameId(7);

        assertEquals(2,      msg.getJobId());
        assertEquals("Tester", msg.getTitle());
        assertEquals(3000,   msg.getSalary());
        assertEquals(150,    msg.getBonusSalary());
        assertFalse(msg.isRequiresDegree());
        assertTrue (msg.isTaken());
        assertEquals(7,      msg.getGameId());
    }
}
