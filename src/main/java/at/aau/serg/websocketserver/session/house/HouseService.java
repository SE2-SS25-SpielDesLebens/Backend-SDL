package at.aau.serg.websocketserver.session.house;

import at.aau.serg.websocketserver.player.PlayerService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;


import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.NoSuchElementException;

/**
 * Verwaltet pro Spiel‐ID (int) genau eine HouseRepository‐Instanz und erlaubt Kauf/Verkauf von Häusern.
 */
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
     * Holt das vorhandene Repository zur gameId oder legt ein neues an.
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
     * Entfernt das Repository, z. B. beim Beenden einer Spiel‐Session.
     */
    public void removeRepository(int gameId) {
        repositories.remove(gameId);
    }

    /**
     * Ermöglicht einem Spieler, ein Haus zu kaufen.
     *
     * @param gameId      Spiel-ID
     * @param playerId    ID des Spielers (aus Player.getId())
     * @param houseId     ID des zu kaufenden Hauses
     * @return das erworbene Haus
     */
    public House buyHouse(int gameId, String playerId, int houseId) {
        HouseRepository repo = getOrCreateRepository(gameId);
        House house = repo.findHouseById(houseId)
                .orElseThrow(() -> new NoSuchElementException("Haus nicht gefunden: " + houseId));
        if (house.isTaken()) {
            throw new IllegalStateException("Haus bereits vergeben: " + houseId);
        }
        repo.assignHouseToPlayer(playerId, house);
        return house;
    }

    /**
     * Ermöglicht einem Spieler, sein aktuelles Haus zu verkaufen, bestimmt den Verkaufspreis und bucht das Geld gut.
     * Ungerade colorValue führt zum schwarzen Preis, gerade zum roten Preis.
     *
     * @param gameId      Spiel-ID
     * @param playerId    ID des Spielers (aus Player.getId())
     * @param colorValue  Integer-Wert zur Bestimmung der Preisfarbe (ungerade = schwarz, gerade = rot)
     * @return das Haus nach Verkaufsfreigabe
     */
    public House sellHouse(int gameId, String playerId, int colorValue) {
        HouseRepository repo = getOrCreateRepository(gameId);
        Optional<House> optionalHouse = repo.getCurrentHouseForPlayer(playerId);
        House house = optionalHouse
                .orElseThrow(() -> new IllegalStateException("Kein Haus zu verkaufen für Spieler " + playerId));
        // Bestimme Verkaufspreis anhand Lombok-Getter
        boolean isBlack = (colorValue % 2 != 0);
        int salePrice = isBlack ? house.getVerkaufspreisSchwarz() : house.getVerkaufspreisRot();
        // Gutschrift beim Spieler
        playerService.addMoneyToPlayer(playerId, salePrice);
        // Haus freigeben
        repo.releaseHouse(house);
        return house;
    }
}
