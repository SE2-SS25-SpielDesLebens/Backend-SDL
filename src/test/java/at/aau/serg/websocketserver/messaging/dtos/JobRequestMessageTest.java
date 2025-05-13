package at.aau.serg.websocketserver.messaging.dtos;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class JobRequestMessageTest {

    @Test
    void gettersAndSettersAndHasDegreeMethod() {
        JobRequestMessage req = new JobRequestMessage();
        req.setPlayerName("Alice");
        req.setGameId(123);
        req.setHasDegree(true);
        req.setJobId(5);

        assertEquals("Alice", req.getPlayerName(), "playerName sollte korrekt zurückgegeben werden");
        assertEquals(123,     req.getGameId(),     "gameId sollte korrekt zurückgegeben werden");
        assertTrue (req.hasDegree(),               "hasDegree()-Methode sollte true zurückgeben");
        assertEquals(5,       req.getJobId(),      "jobId sollte korrekt zurückgegeben werden");
    }

    @Test
    void defaultValuesAfterNoArgsCtor() {
        JobRequestMessage req = new JobRequestMessage();
        // Standardmäßig null oder 0 bzw. false
        assertNull(req.getPlayerName(), "playerName ist ohne Setzen null");
        assertEquals(0, req.getGameId(),   "gameId ist ohne Setzen 0");
        assertFalse(req.hasDegree(),       "hasDegree() ist ohne Setzen false");
        assertEquals(0, req.getJobId(),    "jobId ist ohne Setzen 0");
    }
}