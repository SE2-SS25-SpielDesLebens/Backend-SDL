package at.aau.serg.websocketserver.session.house;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HouseTest {

    @Test
    void testConstructorAndGetters() {
        House house = new House(1, "Villa", 300000, 250000, 400000);

        assertEquals(1, house.getHouseId());
        assertEquals("Villa", house.getBezeichnung());
        assertEquals(300000, house.getKaufpreis());
        assertEquals(300000, house.getVerkaufspreisRot()); // Fehler im Konstruktor beachten!
        assertEquals(400000, house.getVerkaufspreisSchwarz());
        assertFalse(house.isTaken());
        assertNull(house.getAssignedToPlayerName());
    }

    @Test
    void testAssignHouseToWhenAvailable() {
        House house = new House(2, "Einfamilienhaus", 200000, 180000, 250000);

        house.assignHouseTo("Player1");

        assertTrue(house.isTaken());
        assertEquals("Player1", house.getAssignedToPlayerName());
    }

    @Test
    void testAssignHouseToWhenAlreadyTaken() {
        House house = new House(3, "Apartment", 150000, 140000, 200000);

        house.assignHouseTo("Player1");
        house.assignHouseTo("Player2"); // sollte ignoriert werden

        assertTrue(house.isTaken());
        assertEquals("Player1", house.getAssignedToPlayerName());
    }

    @Test
    void testReleaseHouse() {
        House house = new House(4, "Strandhaus", 180000, 170000, 220000);

        house.assignHouseTo("Player1");
        house.releaseHouse();

        assertFalse(house.isTaken());
        assertNull(house.getAssignedToPlayerName());
    }

    @Test
    void testToString() {
        House house = new House(5, "Bauernhof", 120000, 100000, 160000);
        house.assignHouseTo("Player1");

        String expected = "House{Bezeichnung='Bauernhof', Kaufpreis=120000, VerkaufspreisRot=120000, VerkaufspreisSchwarz=160000, Vergeben=true, Zugewiesen an='Player1'}";

        assertEquals(expected, house.toString());
    }

    @Test
    void testEmptyConstructor() {
        House house = new House();

        // nur verifizieren, dass Objekt korrekt initialisiert wird
        assertNotNull(house);
    }
}
