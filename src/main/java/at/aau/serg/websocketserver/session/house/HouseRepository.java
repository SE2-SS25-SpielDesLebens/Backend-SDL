package at.aau.serg.websocketserver.session.house;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Lädt Häuser aus house.json und verwaltet Zuordnungen pro Spieler.
 */
@Repository
public class HouseRepository {

    private final List<House> houses = new ArrayList<>();
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Lädt alle Häuser aus house.json.
     */
    public void loadHouses() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("house.json");
        if (inputStream == null) {
            throw new IllegalStateException("house.json nicht gefunden!");
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(inputStream);
        JsonNode housesNode = rootNode.get("houses");
        if (housesNode == null || !housesNode.isArray()) {
            throw new IllegalStateException("houses-Array nicht gefunden!");
        }

        houses.clear();
        for (JsonNode houseNode : housesNode) {
            House house = mapper.treeToValue(houseNode, House.class);
            houses.add(house);
        }
    }

    /**
     * Gibt alle Häuser eines Spielers zurück (falls vorhanden).
     */
    public List<House> getHousesForPlayer(String playerName) {
        return houses.stream()
                .filter(h -> playerName.equals(h.getAssignedToPlayerName()))
                .collect(Collectors.toList());
    }


    /**
     * Gibt eine bestimmte Anzahl zufälliger verfügbarer Häuser zurück.
     */
    public List<House> getRandomAvailableHouses(int count) {
        List<House> available = houses.stream()
                .filter(h -> !h.isTaken())
                .collect(Collectors.toList());

        Collections.shuffle(available, SECURE_RANDOM);
        return available.stream()
                .limit(count)
                .collect(Collectors.toList());
    }

    /**
     * Findet ein Haus anhand der ID.
     */
    public Optional<House> findHouseById(int houseId) {
        return houses.stream()
                .filter(h -> h.getHouseId() == houseId)
                .findFirst();
    }

    /**
     * Weist einem Spieler ein neues Haus zu und gibt ggf. alle alten frei.
     */
    public void assignHouseToPlayer(String playerName, House newHouse) {
        newHouse.assignHouseTo(playerName);
    }

    /**
     * Gibt ein Haus explizit frei.
     */
    public void releaseHouse(House house) {
        house.releaseHouse();
    }
}
