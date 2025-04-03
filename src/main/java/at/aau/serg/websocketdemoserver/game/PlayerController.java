package at.aau.serg.websocketdemoserver.game;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/players")
public class PlayerController {
    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping
    public List<Player> getAllPlayers() {
        return playerService.getAllPlayers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Player> getPlayerById(@PathVariable int id) {
        Optional<Player> player = playerService.getPlayerById(id);
        return player.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

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
