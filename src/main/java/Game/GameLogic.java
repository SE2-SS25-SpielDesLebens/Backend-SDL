package Game;

import at.aau.serg.websocketserver.Player.Player;
import at.aau.serg.websocketserver.board.BoardService;
import at.aau.serg.websocketserver.board.Field;
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
    @Setter
    private int gameId; // Wird beim Spielstart gesetzt

    private JobService jobService;
    private GameController gameController;
    private PlayerTurnManager turnManager;
    private BoardService boardService;

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
        if (stopFields.contains(endField.getType())) {
            return;
        }

        if (turnManager != null) {
            turnManager.completeTurn(player.getId(), spinResult);
        } else {
            nextTurn();
        }
    }

    private void handleField(Player player, Field field) {
        String type = field.getType();
        switch (type) {
            case "ZAHLTAG" -> handleSalaryField(player, true);
            case "AKTION" -> handleActionField(player);
            case "HAUS" -> handleHouseField(player);
            case "BERUF" -> handleJobField(player);
            case "ANLAGE" -> handleInvestmentField(player);
            case "FREUND" -> handleFriendField(player, field);
            case "HEIRAT" -> handleMarriageField(player);
            case "EXAMEN" -> handleExamField(player);
            default -> System.out.println("[INFO] Kein spezielles Verhalten für Feldtyp: " + type);
        }
    }

    private void handleSalaryField(Player player, boolean landedDirectly) {
        Job job = player.getJobId();
        if (job == null) return;

        int amount = landedDirectly ? job.getBonusSalary() : job.getSalary();
        player.addMoney(amount);
        System.out.println("[ZAHLTAG] Spieler " + player.getId() + (landedDirectly ? " landet auf " : " überquert ") + "Zahltagfeld: +" + amount + " €");
    }

    private void handleActionField(Player player) {
        // TODO: Aktionskarten-Mechanik integrieren

        // Platzhalter: Simuliere eine zufällige Aktion für den Spieler
        Random random = new Random();
        int event = random.nextInt(3);

        switch (event) {
            case 0 -> {
                player.addMoney(10000);
                System.out.println("[AKTION] Spieler " + player.getId() + " gewinnt 10.000 € durch eine Aktionskarte.");
            }
            case 1 -> {
                player.removeMoney(15000);
                System.out.println("[AKTION] Spieler " + player.getId() + " verliert 15.000 € durch eine Aktionskarte.");
            }
            case 2 -> {
                player.addChild();
                System.out.println("[AKTION] Spieler " + player.getId() + " bekommt ein Kind durch eine Aktionskarte.");
            }
            default -> System.out.println("[AKTION] Keine Aktion gefunden.");
        }

        // Später: Aktionskarte ziehen, anzeigen, auswählen (falls Optionen), ausführen und zurück unter Stapel legen
        // Kartenmechanik fehlt noch -> Aktionskarten-Stapel hier einbinden
    }

    private void handleHouseField(Player player) {
        // TODO: Hauskarten-Mechanik integrieren

        // Platzhalter: Simuliere Hauskauf oder -verkauf Entscheidung
        Random random = new Random();
        boolean wantsToBuy = random.nextBoolean();

        if (wantsToBuy) {
            // Haus kaufen: zufällig einen Preis festlegen und Geld abziehen
            int housePrice = 200000;
            player.removeMoney(housePrice);
            player.getHouseId().put(random.nextInt(1000), housePrice); // Dummy-ID und Wert
            System.out.println("[HAUS] Spieler " + player.getId() + " kauft ein Haus für " + housePrice + " €.");
        } else if (!player.getHouseId().isEmpty()) {
            // Haus verkaufen: zufällig ein Haus auswählen und Wert zurückgeben (50 % oder 150 %)
            Integer houseKey = player.getHouseId().keySet().stream().findFirst().get();
            int originalValue = player.getHouseId().get(houseKey);
            boolean red = random.nextBoolean();
            int sellPrice = red ? (int) (originalValue * 1.5) : (int) (originalValue * 0.5);
            player.addMoney(sellPrice);
            player.removeHouse(houseKey);
            System.out.println("[HAUS] Spieler " + player.getId() + " verkauft ein Haus für " + sellPrice + " € (" + (red ? "Rot" : "Schwarz") + ").");
        } else {
            System.out.println("[HAUS] Spieler " + player.getId() + " hat keine Häuser zum Verkaufen.");
        }

        // Später: Hauskarten ziehen (2 Stück), auswählen, kaufen oder verkaufen
        // Hauskarten-Stack muss in GameLogic oder GameController integriert werden
    }

    private void handleJobField(Player player) {
        if (jobService == null) {
            System.out.println("[BERUF] Kein JobService verfügbar.");
            return;
        }

        JobRepository repo = jobService.getOrCreateRepository(this.gameId);
        Optional<Job> maybeJob = repo.getRandomAvailableJobs(player.getEducation(), 1).stream().findFirst();

        if (maybeJob.isEmpty()) {
            System.out.println("[BERUF] Keine verfügbaren Jobs für Spieler " + player.getId());
            return;
        }

        Job newJob = maybeJob.get();
        Job currentJob = player.getJobId();

        if (newJob.isRequiresDegree() && !player.getEducation()) {
            System.out.println("[BERUF] Spieler " + player.getId() + " hat kein Studium, kann Job '" + newJob.getTitle() + "' nicht annehmen.");
            return;
        }

        boolean willSwitch = new Random().nextBoolean();
        if (willSwitch || currentJob == null) {
            repo.assignJobToPlayer(player.getId(), newJob);
            player.assignJob(newJob);
            player.setSalary(newJob.getSalary());
            System.out.println("[BERUF] Spieler " + player.getId() + " wechselt zu Job: " + newJob.getTitle());
        } else {
            System.out.println("[BERUF] Spieler " + player.getId() + " behält seinen aktuellen Job: " + currentJob.getTitle());
        }

        // Später: Spielerentscheidung durch UI/Frontend
    }

    private void handleInvestmentField(Player player) {
        int currentSlot = player.getInvestments();
        Random random = new Random();

        if (currentSlot != 0) {
            boolean wantsToSwitch = random.nextBoolean();
            if (wantsToSwitch) {
                int newSlot = 1 + random.nextInt(10);
                player.setInvestments(newSlot);
                player.setInvestmentPayout(0);
                System.out.println("[ANLAGE] Spieler " + player.getId() + " steckt seine Investition kostenlos auf Zahl " + newSlot + " um (Rücksetzung auf 10.000 € Stufe).");
            } else {
                int payoutStage = player.getInvestmentPayout();
                int payoutAmount = switch (payoutStage) {
                    case 0 -> 10000;
                    case 1 -> 20000;
                    case 2 -> 20000;
                    default -> 0;
                };
                if (payoutAmount > 0) {
                    player.addMoney(payoutAmount);
                    player.setInvestmentPayout(payoutStage + 1);
                    System.out.println("[ANLAGE] Spieler " + player.getId() + " bleibt bei Zahl " + currentSlot + ", schiebt vor und erhält " + payoutAmount + " €.");
                }
            }
            return;
        }

        boolean wantsToInvest = random.nextBoolean();

        if (!wantsToInvest) {
            System.out.println("[ANLAGE] Spieler " + player.getId() + " entscheidet sich gegen eine Investition.");
            return;
        }

        if (player.getMoney() < 50000) {
            System.out.println("[ANLAGE] Spieler " + player.getId() + " hat nicht genug Geld für eine Investition.");
            return;
        }

        int chosenNumber = 1 + random.nextInt(10);
        player.removeMoney(50000);
        player.setInvestments(chosenNumber);
        player.setInvestmentPayout(0);

        System.out.println("[ANLAGE] Spieler " + player.getId() + " investiert 50.000 € auf Zahl " + chosenNumber + ".");
    }

    public void checkAndPayoutInvestment(String spinningPlayerId, int spinResult) {
        for (Player p : players.values()) {
            int slot = p.getInvestments();
            if (slot == spinResult) {
                int payoutStage = p.getInvestmentPayout();
                int payoutAmount = switch (payoutStage) {
                    case 0 -> 10000;
                    case 1 -> 20000;
                    case 2 -> 20000;
                    default -> 0;
                };

                if (payoutAmount > 0) {
                    p.addMoney(payoutAmount);
                    p.setInvestmentPayout(payoutStage + 1);
                    System.out.println("[ANLAGE] Spieler " + p.getId()
                            + " erhält " + payoutAmount + " €, weil "
                            + spinningPlayerId + " seine Anlagezahl " + spinResult + " gedreht hat.");
                }
            }
        }
    }


    private void handleFriendField(Player player, Field field) {
        // Einheitliche Behandlung für Baby-, Freund- oder Haustierfelder als 1 Stift im Auto
        String type = field.getType();
        if ("FREUND".equals(type)) {
            player.addChild();
            System.out.println("[STIFT] Spieler " + player.getId() + " landet auf einem " + type + "-Feld und bekommt 1 Stift ins Auto gesetzt.");
        }
    }


    private void handleMarriageField(Player player) {
        Random random = new Random();
        boolean wantsToMarry = random.nextBoolean();

        if (wantsToMarry) {
            player.removeMoney(50000);
            player.addChild();
            System.out.println("[HEIRAT] Spieler " + player.getId() + " heiratet, zahlt 50.000 € und bekommt einen Stift (Kind). Dreht erneut!");
        } else {
            System.out.println("[HEIRAT] Spieler " + player.getId() + " heiratet nicht. Dreht erneut!");
        }

        int spinResult = 1 + random.nextInt(10);
        performTurn(player, spinResult);
    }

    private void handleExamField(Player player) {
        Random random = new Random();
        int result = 1 + random.nextInt(10);
        if (result <= 2) {
            System.out.println("[EXAMEN] Spieler " + player.getId() + " ist durchgefallen und muss beim nächsten Zug erneut drehen.");
            return;
        }

        JobRepository repo = jobService.getOrCreateRepository(this.gameId);
        List<Job> jobs = repo.getRandomAvailableJobs(true, 4);
        if (jobs.isEmpty()) {
            System.out.println("[EXAMEN] Keine verfügbaren Jobs mit Abschluss gefunden.");
            return;
        }

        Job chosenJob = jobs.get(random.nextInt(jobs.size()));
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
        System.out.println("[EXAMEN] Spieler " + player.getId() + " hat bestanden und erhält Job: " + chosenJob.getTitle());
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
        System.out.println("🌟 Gewonnen hat: " + winner.getId() + " mit " + calculateFinalWealth(winner) + " € Vermögen!");
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

    public int getGameId() {
        return this.gameId;
    }
}





