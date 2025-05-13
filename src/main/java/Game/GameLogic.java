package Game;

import at.aau.serg.websocketserver.session.Job;
import at.aau.serg.websocketserver.session.JobRepository;
import at.aau.serg.websocketserver.session.JobService;
import at.aau.serg.websocketserver.Player.Player;
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

    private static final List<String> CAR_COLORS = List.of("Rot", "Blau", "Gelb", "Grün");
    private static final int MAX_PLAYERS = 4;

    private final List<String> retirementOrder = new ArrayList<>();
    private final Set<Integer> usedInvestmentSlots = new HashSet<>();

    public boolean registerPlayer(String id) {
        if (players.size() >= MAX_PLAYERS) return false;

        Player player = new Player(id);
        players.put(id, player);

        int playerIndex = players.size();
        player.setCarColor(CAR_COLORS.get(playerIndex - 1));
        player.addMoney(250000);
        return true;
    }

    public void prepareGameStart() {
        int i = 0;
        for (Player p : players.values()) {
            p.setActive(i == currentPlayerIndex);
            i++;
        }
    }

    public void handleGameStartChoice(int gameId, String playerName, boolean chooseUniversity) {
        Player player = getPlayerByName(playerName);

        if (chooseUniversity) {
            player.removeMoney(100000);
            // Direkter Zugriff auf das Feld, da kein Setter vorhanden
            try {
                java.lang.reflect.Field educationField = Player.class.getDeclaredField("university");
                educationField.setAccessible(true);
                educationField.setBoolean(player, true);
            } catch (Exception e) {
                throw new RuntimeException("Fehler beim Setzen des Bildungsstatus", e);
            }
        } else {
            assignDefaultCareer(gameId, playerName);
        }
    }

    private void assignDefaultCareer(int gameId, String playerName) {
        JobRepository repo = jobService.getOrCreateRepository(gameId);
        List<Job> jobs = repo.getRandomAvailableJobs(false, 2);

        if (!jobs.isEmpty()) {
            Job chosen;
            boolean bothWithDegree = jobs.stream().allMatch(Job::isRequiresDegree);
            if (bothWithDegree) {
                chosen = jobs.get(0);
            } else {
                chosen = jobs.stream().filter(j -> !j.isRequiresDegree()).findFirst().orElse(jobs.get(0));
            }
            repo.assignJobToPlayer(playerName, chosen);
            Player player = getPlayerByName(playerName);
            player.assignJob(chosen);
            player.setSalary(chosen.getSalary());
        }
    }

    public boolean requestLoan(String playerId) {
        Player player = getPlayerByName(playerId);
        if (!player.isActive()) {
            System.out.println("[VERWEIGERT] Nur der aktive Spieler darf einen Kredit aufnehmen.");
            return false;
        }
        player.takeLoan();
        System.out.println("[KREDIT] Spieler " + playerId + " hat einen Kredit aufgenommen (+20.000 €). Schulden: " + player.getDebts());
        return true;
    }

    public void performTurn(Player player, int spinResult) {
        if (turnManager != null) {
            turnManager.completeTurn(player.getId(), spinResult);
        } else {
            nextTurn();
        }
    }

    public void nextTurn() {
        if (allPlayersRetired()) {
            endGame();
            return;
        }

        List<Player> playerList = new ArrayList<>(players.values());
        int total = playerList.size();
        for (int i = 1; i <= total; i++) {
            int nextIndex = (currentPlayerIndex + i) % total;
            if (!playerList.get(nextIndex).isRetired()) {
                currentPlayerIndex = nextIndex;
                break;
            }
        }

        int i = 0;
        for (Player p : players.values()) {
            p.setActive(i == currentPlayerIndex);
            i++;
        }

        if (gameController != null) {
            String nextPlayerId = getCurrentPlayer().getId();
            gameController.startPlayerTurn(nextPlayerId, false);
        }
    }

    public void playerRetires(String playerName) {
        Player player = getPlayerByName(playerName);
        player.retire();
        retirementOrder.add(playerName);

        switch (retirementOrder.size()) {
            case 1 -> player.addMoney(250000);
            case 2 -> player.addMoney(100000);
            case 3 -> player.addMoney(50000);
            case 4 -> player.addMoney(10000);
        }

        player.clearJob();
        if (allPlayersRetired()) endGame();
    }

    public boolean tryInvestInSlot(String playerId, int slot) {
        if (usedInvestmentSlots.contains(slot)) return false;
        Player player = players.get(playerId);
        player.setInvestments(slot);
        usedInvestmentSlots.add(slot);
        return true;
    }

    public void payoutInvestment(String playerId, int amount) {
        Player player = players.get(playerId);
        player.addMoney(amount);
    }

    public void resetAndReinvest(String playerId, int newSlot) {
        Player player = players.get(playerId);
        int oldSlot = player.getInvestments();
        usedInvestmentSlots.remove(oldSlot);
        player.setInvestments(newSlot);
        usedInvestmentSlots.add(newSlot);
    }

    public void endGame() {
        gameEnded = true;
        List<Map.Entry<String, Player>> sorted = players.entrySet()
                .stream().sorted((a, b) -> Integer.compare(calculateFinalWealth(b.getValue()), calculateFinalWealth(a.getValue())))
                .collect(Collectors.toList());
        Player winner = sorted.get(0).getValue();
        System.out.println("🏁 Gewonnen hat: " + winner.getId() + " mit " + calculateFinalWealth(winner) + " € Vermögen!");
    }

    private int calculateFinalWealth(Player p) {
        int geld = p.getMoney();
        int kinderBonus = p.getChildren() * 50000;
        int schuldenStrafe = p.getDebts() * 60000;
        int hauswert = p.getHouseId().values().stream().mapToInt(Integer::intValue).sum();
        return geld + kinderBonus + hauswert - schuldenStrafe;
    }

    protected boolean allPlayersRetired() {
        return players.values().stream().allMatch(Player::isRetired);
    }

    public Player getCurrentPlayer() {
        List<Player> playerList = new ArrayList<>(players.values());
        return playerList.get(currentPlayerIndex);
    }

    private Player getPlayerByName(String name) {
        return players.values().stream()
                .filter(p -> p.getId().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Spieler nicht gefunden: " + name));
    }
    // Feldlogik muss noch implementiert werden
    // zb. handleActionField(Player), handleHouseField(Player), ...
}



