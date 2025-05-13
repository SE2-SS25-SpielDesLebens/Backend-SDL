package at.aau.serg.websocketserver.session;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JobTest {

    @Test
    void testAssignJobToWhenNotTaken() {
        Job job = new Job(1, "Developer", 5000, 1000, false);
        assertFalse(job.isTaken());
        assertNull(job.getAssignedToPlayerName());

        boolean result = job.assignJobTo("Alice");
        assertTrue(result);
        assertTrue(job.isTaken());
        assertEquals("Alice", job.getAssignedToPlayerName());
    }

    @Test
    void testAssignJobToWhenAlreadyTaken() {
        Job job = new Job(2, "Analyst", 4000, 500, true);
        job.assignJobTo("Bob");
        assertTrue(job.isTaken());

        boolean result = job.assignJobTo("Charlie");
        assertFalse(result);
        // Zustand bleibt unverändert
        assertEquals("Bob", job.getAssignedToPlayerName());
        assertTrue(job.isTaken());
    }

    @Test
    void testReleaseJob() {
        Job job = new Job(3, "Manager", 6000, 1500, true);
        job.assignJobTo("Dan");
        assertTrue(job.isTaken());

        job.releaseJob();
        assertFalse(job.isTaken());
        assertNull(job.getAssignedToPlayerName());
    }

    @Test
    void testToStringContainsFields() {
        Job job = new Job(4, "Tester", 3000, 200, false);
        String s = job.toString();
        assertTrue(s.contains("Bezeichnung='Tester'"));
        assertTrue(s.contains("Gehalt=3000"));
        assertTrue(s.contains("Bonusgehalt=200"));
        assertTrue(s.contains("Hochschulreife benötigt=false"));
        assertTrue(s.contains("Vergeben=false"));
        assertTrue(s.contains("Zugewiesen an='null'"));
    }
}
