package at.aau.serg.websocketserver.session.house;

import at.aau.serg.websocketserver.messaging.dtos.HouseMessage;
import at.aau.serg.websocketserver.player.PlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HouseServiceTest {

    private ObjectProvider<HouseRepository> houseRepoProvider;
    private PlayerService playerService;
    private HouseService houseService;

    private static final int GAME_ID = 42;
    private static final String PLAYER_NAME = "Player1";

    @BeforeEach
    void setUp() {
        houseRepoProvider = mock(ObjectProvider.class);

        // wichtig → neues Repo für jeden Aufruf → sonst ist testRemoveRepository nicht testbar!
        when(houseRepoProvider.getObject()).thenAnswer(invocation -> {
            HouseRepository repo = new HouseRepository();
            try {
                repo.loadHouses();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return repo;
        });

        playerService = mock(PlayerService.class);

        houseService = new HouseService(houseRepoProvider, playerService);
    }

    @Test
    void testGetOrCreateRepository_createsOnce() {
        HouseRepository repo1 = houseService.getOrCreateRepository(GAME_ID);
        HouseRepository repo2 = houseService.getOrCreateRepository(GAME_ID);

        assertSame(repo1, repo2);
    }

    @Test
    void testRemoveRepositoryCreatesNewRepository() {
        HouseRepository repo1 = houseService.getOrCreateRepository(GAME_ID);
        houseService.removeRepository(GAME_ID);
        HouseRepository repo2 = houseService.getOrCreateRepository(GAME_ID);

        // jetzt wird es korrekt unterschiedlich sein:
        assertNotSame(repo1, repo2);
    }

    @Test
    void testHandleHouseAction_buy() {
        List<HouseMessage> houseMessages = houseService.handleHouseAction(GAME_ID, PLAYER_NAME, true);

        assertNotNull(houseMessages);
        assertTrue(houseMessages.size() <= 2);

        for (HouseMessage msg : houseMessages) {
            assertFalse(msg.isTaken());
            assertEquals(GAME_ID, msg.getGameId());
        }
    }

    @Test
    void testHandleHouseAction_sell_noOwnedHouses() {
        List<HouseMessage> houseMessages = houseService.handleHouseAction(GAME_ID, PLAYER_NAME, false);

        assertNotNull(houseMessages);
        assertTrue(houseMessages.isEmpty());
    }

    @Test
    void testBuyHouse_success() {
        House houseToBuy = houseService.getOrCreateRepository(GAME_ID).getRandomAvailableHouses(1).get(0);
        House boughtHouse = houseService.buyHouse(GAME_ID, PLAYER_NAME, houseToBuy.getHouseId());

        assertTrue(boughtHouse.isTaken());
        assertEquals(PLAYER_NAME, boughtHouse.getAssignedToPlayerName());
    }

    @Test
    void testBuyHouse_alreadyTaken_shouldThrow() {
        House houseToBuy = houseService.getOrCreateRepository(GAME_ID).getRandomAvailableHouses(1).get(0);
        houseService.buyHouse(GAME_ID, PLAYER_NAME, houseToBuy.getHouseId());

        int houseId = houseToBuy.getHouseId(); // Vorher berechnen!

        assertThrows(IllegalStateException.class, () -> houseService.buyHouse(GAME_ID, "AnotherPlayer", houseId));
    }


    @Test
    void testSellHouse_success() {
        House houseToBuy = houseService.getOrCreateRepository(GAME_ID).getRandomAvailableHouses(1).get(0);
        House boughtHouse = houseService.buyHouse(GAME_ID, PLAYER_NAME, houseToBuy.getHouseId());

        assertTrue(boughtHouse.isTaken());
        assertEquals(PLAYER_NAME, boughtHouse.getAssignedToPlayerName());

        House soldHouse = houseService.sellHouse(GAME_ID, PLAYER_NAME, boughtHouse.getHouseId(), boughtHouse.getVerkaufspreisRot());

        assertFalse(soldHouse.isTaken());
        assertNull(soldHouse.getAssignedToPlayerName());
    }

    @Test
    void testSellHouse_wrongPlayer_shouldThrow() {
        House houseToBuy = houseService.getOrCreateRepository(GAME_ID).getRandomAvailableHouses(1).get(0);
        House boughtHouse = houseService.buyHouse(GAME_ID, PLAYER_NAME, houseToBuy.getHouseId());

        int houseId = boughtHouse.getHouseId();
        int verkaufspreisRot = boughtHouse.getVerkaufspreisRot();

        assertThrows(IllegalStateException.class, () -> houseService.sellHouse(GAME_ID, "AnotherPlayer", houseId, verkaufspreisRot));
    }


    @Test
    void testFinalizeHouseAction_buyFlow() {
        House houseToBuy = houseService.getOrCreateRepository(GAME_ID).getRandomAvailableHouses(1).get(0);

        HouseMessage houseMessage = new HouseMessage(
                houseToBuy.getHouseId(),
                houseToBuy.getBezeichnung(),
                houseToBuy.getKaufpreis(),
                houseToBuy.getVerkaufspreisRot(),
                houseToBuy.getVerkaufspreisSchwarz(),
                houseToBuy.isTaken(),
                houseToBuy.getAssignedToPlayerName(),
                GAME_ID,
                false
        );

        HouseMessage result = houseService.finalizeHouseAction(GAME_ID, PLAYER_NAME, houseMessage);

        assertTrue(result.isTaken());
        assertEquals(PLAYER_NAME, result.getAssignedToPlayerName());
    }

    @Test
    void testFinalizeHouseAction_sellFlow() {
        House houseToBuy = houseService.getOrCreateRepository(GAME_ID).getRandomAvailableHouses(1).get(0);
        House boughtHouse = houseService.buyHouse(GAME_ID, PLAYER_NAME, houseToBuy.getHouseId());

        HouseMessage houseMessage = new HouseMessage(
                boughtHouse.getHouseId(),
                boughtHouse.getBezeichnung(),
                boughtHouse.getKaufpreis(),
                boughtHouse.getVerkaufspreisRot(),
                boughtHouse.getVerkaufspreisSchwarz(),
                true,
                PLAYER_NAME,
                GAME_ID,
                true
        );

        HouseMessage result = houseService.finalizeHouseAction(GAME_ID, PLAYER_NAME, houseMessage);

        assertFalse(result.isTaken());
        assertNull(result.getAssignedToPlayerName());
    }

    @Test
    void testFinalizeHouseAction_houseNotFound_shouldThrow() {
        HouseMessage houseMessage = new HouseMessage(
                -999, // ungültige ID
                "Fake",
                100000,
                80000,
                120000,
                false,
                null,
                GAME_ID,
                false
        );

        assertThrows(NoSuchElementException.class, () -> {
            houseService.finalizeHouseAction(GAME_ID, PLAYER_NAME, houseMessage);
        });
    }
}
