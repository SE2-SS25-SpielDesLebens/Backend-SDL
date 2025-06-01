package at.aau.serg.websocketserver.websocket.broker;

import at.aau.serg.websocketserver.player.Player;
import at.aau.serg.websocketserver.player.PlayerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/players")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    /**
     * Gibt alle registrierten Spieler zurück.
     */
    @GetMapping
    public ResponseEntity<List<Player>> getAllPlayers() {
        Collection<Player> players = playerService.getPlayers().values();
        if (players.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(new ArrayList<>(players));
    }

    /**
     * Erstellt einen neuen Spieler oder gibt bestehenden zurück.
     */
    @PostMapping
    public ResponseEntity<Player> createPlayer(@RequestBody Player player) {
        Player created = playerService.addPlayer(player.getId());
        return ResponseEntity.ok(created);
    }

    /**
     * Gibt einen Spieler anhand der ID zurück.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Player> getPlayerById(@PathVariable String id) {
        Optional<Player> player = Optional.ofNullable(playerService.getPlayerById(id));
        return player.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Fügt ein Kind hinzu, falls Platz vorhanden.
     */
    @PutMapping("/{id}/add-child")
    public ResponseEntity<String> addChild(@PathVariable String id) {
        try {
            Player player = playerService.getPlayerById(id);
            if (player == null) return ResponseEntity.notFound().build();
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
        try {
            Player player = playerService.getPlayerById(id);
            if (player == null) return ResponseEntity.notFound().build();
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
        try {
            Player player = playerService.getPlayerById(id);
            if (player == null) return ResponseEntity.notFound().build();
            player.investMoney(50000);
            return ResponseEntity.ok("📈 Investition erfolgreich durchgeführt.");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Simuliert ein beliebiges Ereignis über handleEvent.
     */
    @PutMapping("/{id}/event/{eventType}")
    public ResponseEntity<String> triggerEvent(@PathVariable String id, @PathVariable String eventType) {
        try {
            Player player = playerService.getPlayerById(id);
            if (player == null) return ResponseEntity.notFound().build();
            player.handleEvent(eventType);
            return ResponseEntity.ok("✅ Ereignis erfolgreich verarbeitet: " + eventType);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
