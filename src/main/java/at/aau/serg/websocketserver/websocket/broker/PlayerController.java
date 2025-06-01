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
        this.playerService = PlayerService.getInstance(); // Singleton verwenden
    }

    /**
     * Gibt alle registrierten Spieler zurück.
     */
    @GetMapping
    public ResponseEntity<List<Player>> getAllPlayers() {
        List<Player> players = playerService.getAllPlayers();
        if (players.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(players);
    }

    /**
     * Erstellt einen neuen Spieler oder gibt bestehenden zurück.
     */
    @PostMapping
    public ResponseEntity<Player> createPlayer(@RequestBody Player request) {
        Player player = playerService.createPlayerIfNotExists(request.getId());
        return ResponseEntity.status(201).body(player);
    }

    /**
     * Gibt einen Spieler anhand der ID zurück.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Player> getPlayerById(@PathVariable String id) {
        Player player = playerService.getPlayerById(id);
        if (player == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(player);
    }

    /**
     * Entfernt einen Spieler anhand der ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlayer(@PathVariable String id) {
        if (!playerService.isPlayerRegistered(id)) {
            return ResponseEntity.notFound().build();
        }
        playerService.removePlayer(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Fügt ein Kind hinzu, falls Platz vorhanden.
     */
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

    /**
     * Verheiratet den Spieler, wenn er noch nicht verheiratet ist.
     */
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

    /**
     * Führt eine Investition durch, falls noch nicht investiert.
     */
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

    /**
     * Simuliert ein Ereignis über handleEvent.
     */
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
