package Game;

import at.aau.serg.websocketserver.Player.Player;
import at.aau.serg.websocketserver.session.Job;
import at.aau.serg.websocketserver.session.JobRepository;
import at.aau.serg.websocketserver.session.JobService;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Getter
@Setter

public class GameLogic {

    private final Map<String, Player> players = new ConcurrentHashMap<>();
    private int currentPlayerIndex = 0;
    private boolean gameEnded = false;

    private JobService jobService;
    private GameController gameController;
    private PlayerTurnManager turnManager;

    // Farbreihenfolge für Autos
    private static final List<String> CAR_COLORS = List.of("Rot", "Blau", "Gelb", "Grün");
    private static final int MAX_PLAYERS = 4;



    // Spielerregistrierung
    public boolean registerPlayer(String id) {
        if (players.size() >= MAX_PLAYERS) {
            System.out.println("[LOBBY-VOLL] Spieler " + id + " konnte nicht beitreten. Lobby ist voll.");
            return false;
        }

        Player player = new Player(id);
        players.put(id,player);

        int playerIndex = players.size();
        player.setCarColor(CAR_COLORS.get(playerIndex - 1));
        player.addMoney(10000); // Startkapital
        System.out.println("[JOIN] " + id + " wird Spieler " + playerIndex + " mit Farbe " + player.getCarColor());
        return true;
    }

    // Spielvorbereitung: z.B. für zusätzliche Setups
    public void prepareGameStart() {
        for (Player p : players.values()) {
            System.out.println("[SETUP] " + p.getId() + " erhält 10.000 € und Auto: " + p.getCarColor());
        }
        setCurrentPlayerStatus();
    }

    private void setCurrentPlayerStatus() {
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            p.setActive(i == currentPlayerIndex);
        }
    }

    // Entscheidung: Universität oder direkt Beruf
    public void handleGameStartChoice(int gameId, String playerName, boolean chooseUniversity) {
        Player player = getPlayerByName(playerName);

        if (chooseUniversity) {
            for (int i = 0; i < 5; i++) {
                player.addDebt();
            }
            player.setUniversity(true);
            System.out.println("[ENTSCHEIDUNG] " + playerName + " wählt Universität.");
        } else {
            JobRepository repo = jobService.getOrCreateRepository(gameId);
            List<Job> jobs = repo.getRandomAvailableJobs(false, 1);
            if (!jobs.isEmpty()) {
                Job job = jobs.get(0);
                repo.assignJobToPlayer(playerName, job);
                players.get(playerName).assignJob(job);
                System.out.println("[ENTSCHEIDUNG] " + playerName + " startet mit Beruf: " + jobs.get(0).getTitle());
            }
        }
    }

    // Ein Spieler führt einen Spielzug durch
    public void performTurn(Player player, int spinResult) {
        if (turnManager != null) {
            turnManager.completeTurn(player.getId(), spinResult);
        } else {
            System.out.println("[FEHLER] Kein TurnManager verfügbar – Zug wird lokal abgeschlossen.");
            nextTurn();
        }
    }


    // Nächster Spieler
    public void nextTurn() {
        if (allPlayersRetired()) {
            endGame();
            return;
        }

        int total = players.size();
        for (int i = 1; i <= total; i++) {
            int nextIndex = (currentPlayerIndex + i) % total;
            if (!players.get(nextIndex).isRetired()) {
                currentPlayerIndex = nextIndex;
                break;
            }
        }
        setCurrentPlayerStatus();

        if (gameController != null) {
            String nextPlayerId = getCurrentPlayer().getId();
            gameController.startPlayerTurn(nextPlayerId, false);
        }
    }

    // Spieler geht in Rente – vollständige Verarbeitung
    public void playerRetires(String playerName) {
        Player player = getPlayerByName(playerName);
        player.retire();

        // 1. Beruf ablegen
        player.clearJob();

        // 2. "Teilen macht Freude"-Karten behalten → keine Aktion
        // 3. Kapitalanlage bleibt → keine Aktion

        // 4. Schulden + Zinsen zurückzahlen
        int schulden = player.getDebts();
        int rueckzahlung = schulden * 25000;
        player.removeMoney(rueckzahlung);
        player.resetDebts();

        // 5. Haus verkaufen und muss noch angepasst werden wenn die häuser fertig sind
        if (!player.getHouseId().isEmpty()) {
            int hauswert = player.getHouseId().get(0);
            player.addMoney(hauswert);
            player.removeHouse(player.getHouseId().get(0));
        }

        // 6. Zusatzrente pro Kind
        int kinder = player.getChildren();
        int bonus = kinder * 10000;
        player.addMoney(bonus);

        System.out.println("[RENTE] " + playerName + " geht in Rente. Bonus: " + bonus + " €, Schulden: " + rueckzahlung + " €");

        if (allPlayersRetired()) {
            endGame();
        }else{
            setCurrentPlayerStatus();
        }
    }

    // Spielende
    protected void endGame() {
        gameEnded = true;
        List<Map.Entry<String,Player>> sorted=players.entrySet()
                        .stream().sorted(Comparator.comparing(entry-> this.calculatePlayerWealth(entry.getValue())))
                        .collect(Collectors.toList());
        System.out.println("🏁 Gewonnen hat: " + players.get(0).getId());
    }

    protected int calculatePlayerWealth(Player player) {
        int wealth = player.getMoney() - player.getDebts() * 25000;
        return wealth;
    }

    protected boolean allPlayersRetired() {
        return players.values().stream().allMatch(Player::isRetired);
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }



    private Player getPlayerByName(String name) {
        return players.values().stream()
                .filter(p -> p.getId().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Spieler nicht gefunden: " + name));
    }


}

