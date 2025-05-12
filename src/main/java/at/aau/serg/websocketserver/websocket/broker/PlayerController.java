package at.aau.serg.websocketserver.websocket.broker;

import at.aau.serg.websocketserver.Player.Player;
import at.aau.serg.websocketserver.Player.PlayerService;
import at.aau.serg.websocketserver.fieldlogic.BoardService;
import at.aau.serg.websocketserver.fieldlogic.FieldType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/players")
public class PlayerController {
    private final PlayerService playerService;
    private final BoardService boardService;


    public PlayerController(PlayerService playerService, BoardService boardService) {
        this.playerService = playerService;
        this.boardService = boardService;
    }

    @GetMapping
    public ResponseEntity<List<Player>> getAllPlayers() {
        List<Player> players = playerService.getAllPlayers();
        if (players.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(players);
    }

    @PostMapping
    public ResponseEntity<String> createPlayer(@RequestBody Player player) {
        playerService.addPlayer(String.valueOf(player));
        return ResponseEntity.ok("Player created successfully");
    }



    @GetMapping("/{id}")
    public ResponseEntity<Player> getPlayerById(@PathVariable int id) {
        Optional<Player> player = playerService.getPlayerById(String.valueOf(id));
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

    @PostMapping("/field")
    public ResponseEntity<String> playerStepsOnField(
            @RequestParam String playerId,
            @RequestParam FieldType fieldType
    ) {
        return ResponseEntity.ok(boardService.handleFieldEvent(playerId, fieldType));
    }


}

