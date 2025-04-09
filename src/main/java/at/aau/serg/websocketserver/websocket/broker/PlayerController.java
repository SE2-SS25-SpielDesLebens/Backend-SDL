package at.aau.serg.websocketserver.websocket.broker;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*")  // Hier die Frontend-URL eintragen
@RestController
@RequestMapping("/players")
public class PlayerController {
    private final PlayerService playerService;

    // Konstruktor zur Initialisierung des PlayerService
    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    // GET: Gibt alle Spieler zurück
    @GetMapping
    public ResponseEntity<List<Player>> getAllPlayers() {
        List<Player> players = playerService.getAllPlayers();
        if (players.isEmpty()) {
            return ResponseEntity.noContent().build();  // Wenn keine Spieler vorhanden sind, geben wir ein "No Content"-Antwort zurück.
        }
        return ResponseEntity.ok(players);  // Gibt die Liste der Spieler zurück
    }

    // GET: Gibt einen Spieler nach seiner ID zurück
    @GetMapping("/{id}")
    public ResponseEntity<Player> getPlayerById(@PathVariable int id) {
        Optional<Player> player = playerService.getPlayerById(id);
        return player.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // PUT: Aktualisiert einen Spieler
    @PutMapping("/{id}")
    public ResponseEntity<String> updatePlayer(@PathVariable int id, @RequestBody Player player) {
        boolean updated = playerService.updatePlayer(id, player);
        if (updated) {
            return ResponseEntity.ok("Player updated successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
