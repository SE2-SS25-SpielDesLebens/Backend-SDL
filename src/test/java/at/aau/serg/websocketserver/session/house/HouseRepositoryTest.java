package at.aau.serg.websocketserver.session.house;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class HouseRepositoryTest {

    private HouseRepository houseRepository;

    @BeforeEach
    void setUp() throws Exception {
        houseRepository = new HouseRepository();
        houseRepository.loadHouses();
    }

    @Test
    void testLoadHouses() throws Exception {
        // already covered in @BeforeEach, but we assert it:
        List<House> houses = houseRepository.getRandomAvailableHouses(100);
        assertFalse(houses.isEmpty());
    }

    @Test
    void testGetHousesForPlayer_noHousesAssigned() {
        List<House> houses = houseRepository.getHousesForPlayer("UnknownPlayer");
        assertTrue(houses.isEmpty());
    }

    @Test
    void testAssignHouseToPlayer_andGetHousesForPlayer() {
        House house = houseRepository.getRandomAvailableHouses(1).get(0);
        houseRepository.assignHouseToPlayer("Player1", house);

        List<House> housesForPlayer = houseRepository.getHousesForPlayer("Player1");
        assertEquals(1, housesForPlayer.size());
        assertEquals(house.getHouseId(), housesForPlayer.get(0).getHouseId());
    }

    @Test
    void testGetRandomAvailableHouses() {
        List<House> availableHouses = houseRepository.getRandomAvailableHouses(3);
        assertNotNull(availableHouses);
        assertTrue(availableHouses.size() <= 3);
        for (House house : availableHouses) {
            assertFalse(house.isTaken());
        }
    }

    @Test
    void testFindHouseById_found() {
        // Take an existing house id (assume houseId 1 exists in house.json)
        Optional<House> houseOpt = houseRepository.findHouseById(1);
        assertTrue(houseOpt.isPresent());
        assertEquals(1, houseOpt.get().getHouseId());
    }

    @Test
    void testFindHouseById_notFound() {
        Optional<House> houseOpt = houseRepository.findHouseById(-999);
        assertFalse(houseOpt.isPresent());
    }

    @Test
    void testReleaseHouse() {
        House house = houseRepository.getRandomAvailableHouses(1).get(0);
        houseRepository.assignHouseToPlayer("Player1", house);

        // Verify assigned
        assertTrue(house.isTaken());
        assertEquals("Player1", house.getAssignedToPlayerName());

        // Release and verify
        houseRepository.releaseHouse(house);
        assertFalse(house.isTaken());
        assertNull(house.getAssignedToPlayerName());
    }
}
