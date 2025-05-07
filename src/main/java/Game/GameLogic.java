package Game;

import at.aau.serg.websocketserver.session.Job;
import at.aau.serg.websocketserver.session.JobRepository;
import at.aau.serg.websocketserver.session.JobService;

import java.util.*;

public class GameLogic {

    private final List<Player> players = new ArrayList<>();
    private int currentPlayerIndex = 0;
    private boolean gameEnded = false;

    private JobService jobService;

    public void setJobService(JobService jobService) {
        this.jobService = jobService;
    }

    // Spielerregistrierung
    public void registerPlayer(String id) {
        players.add(new Player(id));
    }

    // Spielvorbereitung: Startgeld & Auto
    public void prepareGameStart(List<String> carColors) {
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            p.addMoney(10000);
            p.setCarColor(carColors.get(i));
            // Platzhalter: 2 "Teilen macht Freude"-Karten könnten hier auch gesetzt werden
            System.out.println("[SETUP] " + p.getId() + " erhält 10.000 € und Auto: " + carColors.get(i));
        }
    }

    // Reihenfolge bestimmen (höchster Drehwert beginnt)
    public void determineFirstPlayer(Map<String, Integer> spinResults) {
        String winnerId = Collections.max(spinResults.entrySet(), Map.Entry.comparingByValue()).getKey();
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getId().equals(winnerId)) {
                currentPlayerIndex = i;
                System.out.println("[START] " + winnerId + " beginnt das Spiel.");
                break;
            }
        }
    }

    // Entscheidung: Universität oder direkt Beruf
    public void handleGameStartChoice(int gameId, String playerName, boolean chooseUniversity) {
        Player player = getPlayerByName(playerName);

        if (chooseUniversity) {
            for (int i = 0; i < 5; i++) {
                player.addDebt();
            }
            System.out.println("[ENTSCHEIDUNG] " + playerName + " wählt Universität.");
        } else {
            JobRepository repo = jobService.getOrCreateRepository(gameId);
            List<Job> jobs = repo.getRandomAvailableJobs(false, 1);
            if (!jobs.isEmpty()) {
                repo.assignJobToPlayer(playerName, jobs.get(0));
                System.out.println("[ENTSCHEIDUNG] " + playerName + " startet mit Beruf: " + jobs.get(0).getTitle());
            }
        }
    }

    // Ein Spieler führt einen Spielzug durch
    public void performTurn(Player player, int spinResult) {
        System.out.println("[ZUG] " + player.getId() + " dreht: " + spinResult);

        // TODO: Bewegung auf Spielfeld
        // TODO: handleField(player)

        // Zug beenden
        nextTurn();
    }

    // Nächster Spieler
    public void nextTurn() {
        if (allPlayersRetired()) {
            endGame();
        } else {
            do {
                currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            } while (players.get(currentPlayerIndex).isRetired());
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

        // 5. Haus verkaufen
        if (player.getHouse() != null) {
            int hauswert = player.getHouseValue();
            player.addMoney(hauswert);
            player.removeHouse();
        }

        // 6. Zusatzrente pro Kind
        int kinder = player.getChildren();
        int bonus = kinder * 10000;
        player.addMoney(bonus);

        System.out.println("[RENTE] " + playerName + " geht in Rente. Bonus: " + bonus + " €, Schulden: " + rueckzahlung + " €");

        if (allPlayersRetired()) {
            endGame();
        }
    }

    // Spielende
    protected void endGame() {
        gameEnded = true;
        players.sort(Comparator.comparingInt(this::calculatePlayerWealth).reversed());
        System.out.println("🏁 Gewonnen hat: " + players.get(0).getId());
    }

    protected int calculatePlayerWealth(Player player) {
        int wealth = player.getMoney() - player.getDebts() * 25000;
        wealth += player.getLifeCards().size() * 10000;
        return wealth;
    }

    protected boolean allPlayersRetired() {
        return players.stream().allMatch(Player::isRetired);
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    private Player getPlayerByName(String name) {
        return players.stream()
                .filter(p -> p.getId().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Spieler nicht gefunden: " + name));
    }

    public boolean isGameEnded() {
        return gameEnded;
    }

    public List<Player> getPlayers() {
        return players;
    }
}

