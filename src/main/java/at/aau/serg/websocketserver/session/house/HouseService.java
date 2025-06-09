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
                messages.add(mapToDto(h, gameId, false));
            }
        } else {
            List<House> owned = repo.getHousesForPlayer(playerId);
            for (House h : owned) {
                messages.add(mapToDto(h, gameId, false));
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
                                            HouseMessage houseMessage) {

        HouseRepository repo = getOrCreateRepository(gameId);
        House house = repo.findHouseById(houseMessage.getHouseId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Haus nicht gefunden: " + houseMessage.getHouseId()));

        House result;
        if (house.isTaken() && playerName.equals(house.getAssignedToPlayerName())) {
            // isSellPrice == true → Schwarzer Verkaufspreis verwenden
            int sellValue = houseMessage.isSellPrice()
                    ? house.getVerkaufspreisSchwarz()
                    : house.getVerkaufspreisRot();

            result = sellHouse(gameId, playerName, house.getHouseId(), sellValue);

        } else {
            result = buyHouse(gameId, playerName, house.getHouseId());
        }

        return mapToDto(result, gameId, houseMessage.isSellPrice());
    }



    /** Domänen-Logik: Haus kaufen */
    public House buyHouse(int gameId, String playerName, int houseId) {
        HouseRepository repo = getOrCreateRepository(gameId);
        House house = repo.findHouseById(houseId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Haus nicht gefunden: " + houseId));
        if (house.isTaken()) {
            throw new IllegalStateException("Haus bereits vergeben: " + houseId);
        }
        // Abbuchung vom Spieler
        // playerService.removeMoneyFromPlayer(playerName, price);

        repo.assignHouseToPlayer(playerName, house);
        return house;
    }

    public House sellHouse(int gameId, String playerName, int houseId, int sellPrice) {
        HouseRepository repo = getOrCreateRepository(gameId);

        // Explizites Haus anhand seiner ID holen
        House house = repo.findHouseById(houseId)
                .orElseThrow(() -> new IllegalStateException(
                        "Haus nicht gefunden: " + houseId));

        // Sicherstellen, dass der Spieler tatsächlich dieses Haus besitzt
        if (!playerName.equals(house.getAssignedToPlayerName())) {
            throw new IllegalStateException(
                    "Spieler " + playerName + " besitzt nicht das Haus-ID " + houseId);
        }

        // Auszahlung an den Spieler (bezogen auf PlayerService)
        //playerService.addMoneyToPlayer(playerName, sellPrice);

        // Haus freigeben
        repo.releaseHouse(house);

        return house;
    }


    /** Helfer: konvertiert Domain → DTO */
    private HouseMessage mapToDto(House house, int gameId, boolean sellPrice) {
        return new HouseMessage(
                house.getHouseId(),
                house.getBezeichnung(),
                house.getKaufpreis(),
                house.getVerkaufspreisRot(),
                house.getVerkaufspreisSchwarz(),
                house.isTaken(),
                house.getAssignedToPlayerName(),
                gameId,
                sellPrice
        );
    }

}
