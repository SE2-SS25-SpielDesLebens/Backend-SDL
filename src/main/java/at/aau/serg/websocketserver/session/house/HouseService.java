package at.aau.serg.websocketserver.session.house;

import at.aau.serg.websocketserver.messaging.dtos.HouseMessage;
import at.aau.serg.websocketserver.player.PlayerService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.NoSuchElementException;

@Service
public class HouseService {

    private final Map<Integer, HouseRepository> repositories = new ConcurrentHashMap<>();
    private final ObjectProvider<HouseRepository> houseRepoProvider;
    private final PlayerService playerService;

    public HouseService(ObjectProvider<HouseRepository> houseRepoProvider,
                        PlayerService playerService) {
        this.houseRepoProvider = houseRepoProvider;
        this.playerService = playerService;
    }

    /**
     * Holt oder legt das Repository für eine gegebene Game-ID an.
     */
    public HouseRepository getOrCreateRepository(int gameId) {
        return repositories.computeIfAbsent(gameId, id -> {
            HouseRepository repo = houseRepoProvider.getObject();
            try {
                repo.loadHouses();
            } catch (Exception e) {
                throw new IllegalStateException(
                        "Fehler beim Laden der house.json für Spiel " + id, e
                );
            }
            return repo;
        });
    }

    /**
     * Entfernt das Repository, z. B. beim Ende einer Session.
     */
    public void removeRepository(int gameId) {
        repositories.remove(gameId);
    }

    /**
     * Liefert je nach buyElseSell-Flag:
     *  - true: bis zu 2 zufällige, freie Häuser
     *  - false: alle dem Spieler aktuell zugewiesenen Häuser
     */
    public List<HouseMessage> handleHouseAction(int gameId,
                                                String playerId,
                                                boolean buyElseSell) {
        HouseRepository repo = getOrCreateRepository(gameId);
        List<HouseMessage> messages = new ArrayList<>();

        if (buyElseSell) {
            List<House> available = repo.getRandomAvailableHouses(2);
            for (House h : available) {
                messages.add(mapToDto(h, gameId));
            }
        } else {
            repo.getCurrentHouseForPlayer(playerId)
                    .ifPresent(h -> messages.add(mapToDto(h, gameId)));
        }

        return messages;
    }

    /**
     * Finalisiert den Kauf oder Verkauf:
     *   - erzeugt intern einen colorValue (1–6)
     *   - verkauft, wenn Spieler das Haus besitzt; andernfalls kauft er es
     */
    public HouseMessage finalizeHouseAction(int gameId,
                                            String playerId,
                                            int houseId) {
        HouseRepository repo = getOrCreateRepository(gameId);
        House house = repo.findHouseById(houseId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Haus nicht gefunden: " + houseId));

        House result;

        if (house.isTaken() && playerId.equals(house.getAssignedToPlayerName())) {
            //ToDo: Methodenaufruf für Wheel of Fortune
            //int colorValue = ???
            result = sellHouse(gameId, playerId, /*colorValue*/ 1 );
        } else {
            result = buyHouse(gameId, playerId, houseId);
        }

        return mapToDto(result, gameId);
    }

    /** Domänen-Logik: Haus kaufen */
    public House buyHouse(int gameId, String playerId, int houseId) {
        HouseRepository repo = getOrCreateRepository(gameId);
        House house = repo.findHouseById(houseId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Haus nicht gefunden: " + houseId));
        if (house.isTaken()) {
            throw new IllegalStateException("Haus bereits vergeben: " + houseId);
        }
        repo.assignHouseToPlayer(playerId, house);
        return house;
    }

    /** Domänen-Logik: Haus verkaufen */
    public House sellHouse(int gameId, String playerId, int colorValue) {
        HouseRepository repo = getOrCreateRepository(gameId);
        House house = repo.getCurrentHouseForPlayer(playerId)
                .orElseThrow(() -> new IllegalStateException(
                        "Kein Haus zu verkaufen für Spieler " + playerId));
        boolean isBlack = (colorValue % 2 != 0);
        int price = isBlack
                ? house.getVerkaufspreisSchwarz()
                : house.getVerkaufspreisRot();
        playerService.addMoneyToPlayer(playerId, price);
        repo.releaseHouse(house);
        return house;
    }


    /** Helfer: konvertiert Domain → DTO */
    private HouseMessage mapToDto(House house, int gameId) {
        return new HouseMessage(
                house.getHouseId(),
                house.getBezeichnung(),
                house.getKaufpreis(),
                house.getVerkaufspreisRot(),
                house.getVerkaufspreisSchwarz(),
                house.isTaken(),
                house.getAssignedToPlayerName(),
                gameId
        );
    }
}
