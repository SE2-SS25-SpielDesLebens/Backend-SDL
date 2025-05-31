package at.aau.serg.websocketserver.game;

import at.aau.serg.websocketserver.player.Player;
import at.aau.serg.websocketserver.board.BoardService;
import at.aau.serg.websocketserver.board.Field;
import at.aau.serg.websocketserver.player.PlayerService;
import at.aau.serg.websocketserver.session.Job;
import at.aau.serg.websocketserver.session.JobRepository;
import at.aau.serg.websocketserver.session.JobService;
import lombok.Getter;
import lombok.Setter;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Getter
@Setter
public class GameLogic {

    private final Map<String, Player> players = new ConcurrentHashMap<>();
    private int currentPlayerIndex = 0;
    private boolean gameEnded = false;
    @Getter
    @Setter
    private int gameId; // Wird beim Spielstart gesetzt

    private JobService jobService;
    private GameController gameController;
    private PlayerTurnManager turnManager;
    private BoardService boardService;
    @Setter
    private PlayerService playerService; // ✅ Injected PlayerService

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
        player.addDebt();
        player.addMoney(50000);
        System.out.println("[KREDIT] Spieler " + playerId + " nimmt einen Kredit auf (+50.000 €). Schulden: " + player.getDebts());
        return true;
    }

    public void repayLoan(String playerId) {
        Player player = getPlayerByName(playerId);
        if (!player.isActive()) {
            System.out.println("[VERWEIGERT] Rückzahlung nur im eigenen Zug erlaubt.");
            return;
        }
        if (player.getDebts() == 0) {
            System.out.println("[INFO] Spieler " + playerId + " hat keine offenen Kredite.");
            return;
        }
        if (player.getMoney() < 60000) {
            System.out.println("[FEHLER] Spieler " + playerId + " hat nicht genug Geld zur Rückzahlung (60.000 € benötigt).");
            return;
        }
        player.removeMoney(60000);
        player.setDebts(player.getDebts() - 1);
        System.out.println("[RÜCKZAHLUNG] Spieler " + playerId + " zahlt einen Kredit zurück (–60.000 €). Verbleibende Schulden: " + player.getDebts());
    }

    public void performTurn(Player player, int spinResult) {
        if (player.mustRepeatExam()) {
            handleExamField(player);
            return;
        }

        if (boardService == null) {
            throw new IllegalStateException("BoardService wurde nicht gesetzt.");
        }

        int playerIdInt = Integer.parseInt(player.getId());
        int startIndex = boardService.getPlayerField(playerIdInt).getIndex();
        boardService.movePlayer(playerIdInt, spinResult);
        Field endField = boardService.getPlayerField(playerIdInt);

        for (int i = 1; i <= spinResult; i++) {
            Field intermediate = boardService.getFieldByIndex(startIndex + i);
            if (intermediate != null && "ZAHLTAG".equals(intermediate.getType()) && intermediate.getIndex() != endField.getIndex()) {
                Job job = player.getJobId();
                if (job != null) {
                    player.addMoney(job.getSalary());
                    System.out.println("[ZAHLTAG] Spieler " + player.getId() + " überquert ein Zahltagfeld: +" + job.getSalary() + " €");
                }
            }
        }

        handleField(player, endField);
        checkAndPayoutInvestment(player.getId(), spinResult);

        List<String> stopFields = List.of("EXAMEN", "HEIRAT");
        if (stopFields.contains(endField.getType())) return;

        if (turnManager != null) {
            turnManager.completeTurn(player.getId(), spinResult);
        } else {
            nextTurn();
        }
    }

    void handleField(Player player, Field field) {
        String type = field.getType();
        switch (type) {
            case "ZAHLTAG":
                handleSalaryField(player);
                break;
            case "AKTION":
                handleActionField(player);
                break;
            case "HAUS":
                handleHouseField(player);
                break;
            case "BERUF":
                handleJobField(player);
                break;
            case "ANLAGE":
                handleInvestmentField(player);
                break;
            case "FREUND":
                handleFriendField(player, field);
                break;
            case "HEIRAT":
                handleMarriageField(player);
                break;
            case "EXAMEN":
                handleExamField(player);
                break;
            default:
                System.out.println("[INFO] Kein spezielles Verhalten für Feldtyp: " + type);
        }
    }

    private void handleSalaryField(Player player) {
        Job job = player.getJobId();
        if (job == null) return;

        int amount = job.getBonusSalary();
        player.addMoney(amount);
        System.out.println("[ZAHLTAG] Spieler " + player.getId() + (true ? " landet auf " : " überquert ") + "Zahltagfeld: +" + amount + " €");
    }

    private void handleActionField(Player player) {
        SecureRandom random = new SecureRandom();
        int event = random.nextInt(3);

        switch (event) {
            case 0:
                player.addMoney(10000);
                System.out.println("[AKTION] Spieler " + player.getId() + " gewinnt 10.000 € durch eine Aktionskarte.");
                break;
            case 1:
                player.removeMoney(15000);
                System.out.println("[AKTION] Spieler " + player.getId() + " verliert 15.000 € durch eine Aktionskarte.");
                break;
            case 2:
                playerService.incrementCounterForPlayer(player.getId(), "kind"); // ✅ ersetzt player.addChild()
                break;
            default:
                System.out.println("[AKTION] Keine Aktion gefunden.");
        }
    }

    void handleHouseField(Player player) {
        SecureRandom random = new SecureRandom();
        boolean wantsToBuy = random.nextBoolean();

        if (wantsToBuy) {
            int housePrice = 200000;
            player.removeMoney(housePrice);
            player.getHouseId().put(random.nextInt(1000), housePrice);
            System.out.println("[HAUS] Spieler " + player.getId() + " kauft ein Haus für " + housePrice + " €.");
        } else if (!player.getHouseId().isEmpty()) {
            Optional<Integer> optionalKey = player.getHouseId().keySet().stream().findFirst();

            optionalKey.ifPresent(houseKey -> {
                int originalValue = player.getHouseId().get(houseKey);
                boolean red = random.nextBoolean();
                int sellPrice = red ? (int) (originalValue * 1.5) : (int) (originalValue * 0.5);
                player.addMoney(sellPrice);
                player.removeHouse(houseKey);
                System.out.println("[HAUS] Spieler " + player.getId() + " verkauft ein Haus für " + sellPrice + " € (" + (red ? "Rot" : "Schwarz") + ").");
            });
        }
    }

    void handleJobField(Player player) {
        if (jobService == null) {
            System.out.println("[BERUF] Kein JobService verfügbar.");
            return;
        }

        JobRepository repo = jobService.getOrCreateRepository(this.gameId);
        Optional<Job> maybeJob = repo.getRandomAvailableJobs(player.getEducation(), 1).stream().findFirst();

        maybeJob.ifPresent(newJob -> {
            if (newJob.isRequiresDegree() && !player.getEducation()) {
                System.out.println("[BERUF] Spieler " + player.getId() + " hat kein Studium.");
                return;
            }

            boolean willSwitch = new SecureRandom().nextBoolean();
            if (willSwitch || player.getJobId() == null) {
                repo.assignJobToPlayer(player.getId(), newJob);
                player.assignJob(newJob);
                player.setSalary(newJob.getSalary());
                System.out.println("[BERUF] Spieler " + player.getId() + " wechselt zu Job: " + newJob.getTitle());
            }
        });
    }

    void handleInvestmentField(Player player) {
        int currentSlot = player.getInvestments();
        SecureRandom random = new SecureRandom();

        if (currentSlot != 0) {
            boolean wantsToSwitch = random.nextBoolean();
            if (wantsToSwitch) {
                int newSlot = 1 + random.nextInt(10);
                player.setInvestments(newSlot);
                player.setInvestmentPayout(0);
                System.out.println("[ANLAGE] Spieler " + player.getId() + " steckt um auf Zahl " + newSlot + ".");
            } else {
                int payoutStage = player.getInvestmentPayout();
                int payoutAmount = (payoutStage + 1) * 10000;
                player.addMoney(payoutAmount);
                player.setInvestmentPayout(payoutStage + 1);
                System.out.println("[ANLAGE] Spieler " + player.getId() + " erhält " + payoutAmount + " €.");
            }
            return;
        }

        if (random.nextBoolean() && player.getMoney() >= 50000) {
            int chosenNumber = 1 + random.nextInt(10);
            player.removeMoney(50000);
            player.setInvestments(chosenNumber);
            player.setInvestmentPayout(0);
            System.out.println("[ANLAGE] Spieler " + player.getId() + " investiert in Zahl " + chosenNumber + ".");
        }
    }

    public void checkAndPayoutInvestment(String spinningPlayerId, int spinResult) {
        for (Player p : players.values()) {
            if (p.getInvestments() == spinResult) {
                int payoutStage = p.getInvestmentPayout();
                int payoutAmount = (payoutStage + 1) * 10000;
                p.addMoney(payoutAmount);
                p.setInvestmentPayout(payoutStage + 1);
                System.out.println("[ANLAGE] Spieler " + p.getId() + " erhält " + payoutAmount + " €.");
            }
        }
    }

    void handleFriendField(Player player, Field field) {
        playerService.incrementCounterForPlayer(player.getId(), "freund"); // ✅ zentral über PlayerService
    }

    void handleMarriageField(Player player) {
        if (new SecureRandom().nextBoolean()) {
            player.removeMoney(50000);
            playerService.incrementCounterForPlayer(player.getId(), "heirat"); // ✅
            playerService.incrementCounterForPlayer(player.getId(), "kind");   // 🎁 Kind zur Hochzeit
        }
        if (gameController != null) {
            gameController.requestAdditionalSpin(player.getId());
        }
    }

    void handleExamField(Player player) {
        if (player.mustRepeatExam()) {
            System.out.println("[EXAMEN] Wiederholung für " + player.getId());
            player.setMustRepeatExam(false);
        }

        int result = 1 + new SecureRandom().nextInt(10);
        if (result <= 2) {
            player.setMustRepeatExam(true);
            if (gameController != null) {
                gameController.startRepeatExamTurn(player.getId());
            }
            return;
        }

        JobRepository repo = jobService.getOrCreateRepository(this.gameId);
        List<Job> jobs = repo.getRandomAvailableJobs(true, 4);
        if (jobs.isEmpty()) return;

        Job chosenJob = jobs.get(new SecureRandom().nextInt(jobs.size()));
        repo.assignJobToPlayer(player.getId(), chosenJob);
        player.assignJob(chosenJob);
        player.setSalary(chosenJob.getSalary());

        try {
            java.lang.reflect.Field educationField = Player.class.getDeclaredField("university");
            educationField.setAccessible(true);
            educationField.setBoolean(player, true);
        } catch (Exception e) {
            throw new RuntimeException("Fehler beim Setzen des Bildungsstatus", e);
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

        int position = retirementOrder.size();
        switch (position) {
            case 1: player.addMoney(250000); break;
            case 2: player.addMoney(100000); break;
            case 3: player.addMoney(50000); break;
            case 4: player.addMoney(10000); break;
        }

        player.clearJob();
        if (allPlayersRetired()) endGame();
    }

    public void endGame() {
        gameEnded = true;
        List<Map.Entry<String, Player>> sorted = players.entrySet()
                .stream().sorted((a, b) -> Integer.compare(calculateFinalWealth(b.getValue()), calculateFinalWealth(a.getValue())))
                .collect(Collectors.toList());
        Player winner = sorted.get(0).getValue();
        System.out.println(" Gewonnen hat: " + winner.getId() + " mit " + calculateFinalWealth(winner) + " € Vermögen!");
    }

    int calculateFinalWealth(Player p) {
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

    public Player getPlayerByName(String name) {
        return players.values().stream()
                .filter(p -> p.getId().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Spieler nicht gefunden: " + name));
    }
}
