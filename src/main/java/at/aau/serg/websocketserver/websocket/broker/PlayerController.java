package at.aau.serg.websocketserver.websocket.broker;

import at.aau.serg.websocketserver.Player.Player;
import at.aau.serg.websocketserver.Player.PlayerService;
import at.aau.serg.websocketserver.fieldlogic.FieldService;
import at.aau.serg.websocketserver.fieldlogic.FieldType;
import at.aau.serg.websocketserver.player.Player;
import at.aau.serg.websocketserver.player.PlayerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/players")
public class PlayerController {
    private final PlayerService playerService;
    private final FieldService fieldService;


    public PlayerController(PlayerService playerService, FieldService fieldService) {
        this.playerService = playerService;
        this.fieldService = fieldService;
    }

    @GetMapping
    public ResponseEntity<List<Player>> getAllPlayers() {
        Collection<Player> players = playerService.getPlayers().values();
        if (players.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok((List<Player>) players);
    }

    @PostMapping
    public ResponseEntity<Player> createPlayer(@RequestBody Player player) {
        Player created = playerService.addPlayer(player.getId());
        return ResponseEntity.ok(created); // gib den ganzen Spieler zurück
    }




    @GetMapping("/{id}")
    public ResponseEntity<Player> getPlayerById(@PathVariable int id) {
        Optional<Player> player = Optional.ofNullable(playerService.getPlayerById(String.valueOf(id)));
        return player.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }


    @PutMapping("/{id}")
    public ResponseEntity<String> updatePlayer(@PathVariable int id, @RequestBody Player player) {
        boolean updated = playerService.updatePlayer(String.valueOf(id), player);
        if (updated) {
            return ResponseEntity.ok("Player updated successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/add-child")
    public ResponseEntity<String> addChild(@PathVariable int id) {
        try {
            playerService.addChildToPlayer(String.valueOf(id));
            return ResponseEntity.ok("Kind erfolgreich hinzugefügt.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/marry")
    public ResponseEntity<String> marryPlayer(@PathVariable int id) {
        try {
            playerService.marryPlayer(String.valueOf(id));
            return ResponseEntity.ok("Spieler erfolgreich verheiratet.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/invest")
    public ResponseEntity<String> invest(@PathVariable int id) {
        try {
            playerService.investForPlayer(String.valueOf(id));
            return ResponseEntity.ok("Investition erfolgreich!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/trigger-field")
    public ResponseEntity<String> triggerFieldEffect(@PathVariable int id) {
        return ResponseEntity.ok(fieldService.triggerCurrentFieldEvent(id));
    }



}

