package at.aau.serg.websocketserver.messaging.dtos;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HouseMessageTest {

    @Test
    void testAllArgsConstructorAndGetters() {
        HouseMessage house = new HouseMessage(
                1,
                "Villa",
                500000,
                300000,
                250000,
                true,
                "Alice",
                42,
                true
        );

        assertEquals(1, house.getHouseId());
        assertEquals("Villa", house.getBezeichnung());
        assertEquals(500000, house.getKaufpreis());
        assertEquals(300000, house.getVerkaufspreisRot());
        assertEquals(250000, house.getVerkaufspreisSchwarz());
        assertTrue(house.isTaken());
        assertEquals("Alice", house.getAssignedToPlayerName());
        assertEquals(42, house.getGameId());
        assertTrue(house.isSellPrice());
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        HouseMessage house = new HouseMessage();

        house.setHouseId(2);
        house.setBezeichnung("Bungalow");
        house.setKaufpreis(200000);
        house.setVerkaufspreisRot(150000);
        house.setVerkaufspreisSchwarz(120000);
        house.setTaken(false);
        house.setAssignedToPlayerName("Bob");
        house.setGameId(7);
        house.setSellPrice(false);

        assertEquals(2, house.getHouseId());
        assertEquals("Bungalow", house.getBezeichnung());
        assertEquals(200000, house.getKaufpreis());
        assertEquals(150000, house.getVerkaufspreisRot());
        assertEquals(120000, house.getVerkaufspreisSchwarz());
        assertFalse(house.isTaken());
        assertEquals("Bob", house.getAssignedToPlayerName());
        assertEquals(7, house.getGameId());
        assertFalse(house.isSellPrice());
    }

    @Test
    void testEqualsAndHashCode() {
        HouseMessage house1 = new HouseMessage(1, "Haus", 100000, 80000, 70000, false, "Tom", 1, false);
        HouseMessage house2 = new HouseMessage(1, "Haus", 100000, 80000, 70000, false, "Tom", 1, false);

        assertEquals(house1, house2);
        assertEquals(house1.hashCode(), house2.hashCode());
    }

    @Test
    void testToStringDoesNotThrow() {
        HouseMessage house = new HouseMessage(3, "Loft", 999999, 500000, 400000, true, "Zoe", 5, true);
        assertNotNull(house.toString());
        assertTrue(house.toString().contains("Loft"));
    }
}
