package at.aau.serg.websocketserver.websocket.broker;

import at.aau.serg.websocketserver.player.Player;
import at.aau.serg.websocketserver.player.PlayerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/players")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController() {
        this.playerService = PlayerService.getInstance(); // Singleton
    }

    // 📋 Alle Spieler abrufen
    @GetMapping
    public ResponseEntity<List<Player>> getAllPlayers() {
        List<Player> players = playerService.getAllPlayers();
        return players.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(players);
    }

    // ➕ Spieler erstellen (mit Startwerten)
    @PostMapping
    public ResponseEntity<Player> createPlayer(@RequestBody Player request) {
        Player player = playerService.createPlayerIfNotExists(request.getId());

        // Nur wenn wirklich neu erstellt (Startwerte setzen)
        if (!playerService.isPlayerRegistered(request.getId())) {
            player.setMoney(250000);
            player.setSalary(50000);
            player.setActive(true);
            System.out.println("🎮 Neuer Spieler erstellt: " + player.getId() + " mit 250k Startgeld.");
        }

        return ResponseEntity.status(201).body(player);
    }

    // 🔍 Spieler nach ID abrufen
    @GetMapping("/{id}")
    public ResponseEntity<Player> getPlayerById(@PathVariable String id) {
        Player player = playerService.getPlayerById(id);
        return player != null ? ResponseEntity.ok(player) : ResponseEntity.notFound().build();
    }

    // 🗑️ Spieler löschen
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlayer(@PathVariable String id) {
        if (!playerService.isPlayerRegistered(id)) {
            return ResponseEntity.notFound().build();
        }
        playerService.removePlayer(id);
        return ResponseEntity.noContent().build();
    }

    // 💰 Geld abrufen
    @GetMapping("/{id}/money")
    public ResponseEntity<Map<String, Integer>> getPlayerMoney(@PathVariable String id) {
        Player player = playerService.getPlayerById(id);
        return player != null
                ? ResponseEntity.ok(Map.of("money", player.getMoney()))
                : ResponseEntity.notFound().build();
    }

    // 💸 Zahltag auslösen
    @PutMapping("/{id}/salary")
    public ResponseEntity<String> receiveSalary(@PathVariable String id) {
        Player player = playerService.getPlayerById(id);
        if (player == null) return ResponseEntity.notFound().build();

        int salaryAmount = 50000;
        player.setMoney(player.getMoney() + salaryAmount);

        return ResponseEntity.ok("💰 Zahltag! +" + salaryAmount + "€");
    }

    // 👶 Kind hinzufügen
    @PutMapping("/{id}/add-child")
    public ResponseEntity<String> addChild(@PathVariable String id) {
        Player player = playerService.getPlayerById(id);
        if (player == null) return ResponseEntity.notFound().build();

        try {
            player.addChildrenWithCarCheck(1);
            return ResponseEntity.ok("👶 Kind erfolgreich hinzugefügt.");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 💍 Heiraten
    @PutMapping("/{id}/marry")
    public ResponseEntity<String> marryPlayer(@PathVariable String id) {
        Player player = playerService.getPlayerById(id);
        if (player == null) return ResponseEntity.notFound().build();

        try {
            player.marry();
            return ResponseEntity.ok("💍 Spieler erfolgreich verheiratet.");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 📈 Investieren
    @PutMapping("/{id}/invest")
    public ResponseEntity<String> invest(@PathVariable String id) {
        Player player = playerService.getPlayerById(id);
        if (player == null) return ResponseEntity.notFound().build();

        try {
            player.investMoney(50000);
            return ResponseEntity.ok("📈 Investition erfolgreich durchgeführt.");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 🎲 Ereignis triggern
    @PutMapping("/{id}/event/{eventType}")
    public ResponseEntity<String> triggerEvent(@PathVariable String id, @PathVariable String eventType) {
        Player player = playerService.getPlayerById(id);
        if (player == null) return ResponseEntity.notFound().build();

        try {
            player.handleEvent(eventType);
            return ResponseEntity.ok("✅ Ereignis erfolgreich verarbeitet: " + eventType);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
