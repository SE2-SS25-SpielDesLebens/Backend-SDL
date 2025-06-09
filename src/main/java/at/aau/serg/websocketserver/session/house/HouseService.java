package at.aau.serg.websocketserver.session.house;

import at.aau.serg.websocketserver.messaging.dtos.HouseMessage;
import at.aau.serg.websocketserver.player.Player;
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

    public static final String HAUS_NICHT_GEFUNDEN = "Haus nicht gefunden: ";

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
            List<House> owned = repo.getHousesForPlayer(playerId);
            for (House h : owned) {
                messages.add(mapToDto(h, gameId));
            }
        }

        return messages;
    }


    /**
     * Finalisiert den Kauf oder Verkauf:
     *   - verkauft, wenn Spieler das Haus besitzt; andernfalls kauft er es
     */
    public HouseMessage finalizeHouseAction(int gameId,
                                            String playerName,
                                            int houseId) {
        HouseRepository repo = getOrCreateRepository(gameId);
        House house = repo.findHouseById(houseId)
                .orElseThrow(() -> new NoSuchElementException(
                        HAUS_NICHT_GEFUNDEN + houseId));

        House result;
        if (house.isTaken() && playerName.equals(house.getAssignedToPlayerName())) {
            // ToDo: colorValue aus Wheel of Fortune einsetzen, hier als Platzhalter 1
            result = sellHouse(gameId, playerName, houseId, 1);
        } else {
            result = buyHouse(gameId, playerName, houseId);
        }

        return mapToDto(result, gameId);
    }

    /** Domänen-Logik: Haus kaufen */
    public House buyHouse(int gameId, String playerId, int houseId) {
        HouseRepository repo = getOrCreateRepository(gameId);
        House house = repo.findHouseById(houseId)
                .orElseThrow(() -> new NoSuchElementException(
                        HAUS_NICHT_GEFUNDEN + houseId));
        if (house.isTaken()) {
            throw new IllegalStateException("Haus bereits vergeben: " + houseId);
        }
        // Abbuchung vom Spieler
        // playerService.removeMoneyFromPlayer(playerId, price);

        repo.assignHouseToPlayer(playerId, house);
        return house;
    }

    public House sellHouse(int gameId, String playerId, int houseId, int colorValue) {
        HouseRepository repo = getOrCreateRepository(gameId);

        // Explizites Haus anhand seiner ID holen
        House house = repo.findHouseById(houseId)
                .orElseThrow(() -> new IllegalStateException(
                        HAUS_NICHT_GEFUNDEN + houseId));

        // Sicherstellen, dass der Spieler tatsächlich dieses Haus besitzt
        if (!playerId.equals(house.getAssignedToPlayerName())) {
            throw new IllegalStateException(
                    "Spieler " + playerId + " besitzt nicht das Haus-ID " + houseId);
        }

        // Verkaufspreis berechnen
        boolean isBlack = (colorValue % 2 != 0);
        int price = isBlack
                ? house.getVerkaufspreisSchwarz()
                : house.getVerkaufspreisRot();

        // Auszahlung an den Spieler (bezogen auf PlayerService)
        //playerService.addMoneyToPlayer(playerId, price);

        // Haus freigeben
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
