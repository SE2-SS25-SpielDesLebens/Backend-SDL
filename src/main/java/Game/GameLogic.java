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
    private GameController gameController;
    private PlayerTurnManager turnManager;

    // Farbreihenfolge für Autos
    private static final List<String> CAR_COLORS = List.of("Rot", "Blau", "Gelb", "Grün");
    private static final int MAX_PLAYERS = 4;

    private final List<String> millionaersvillaPlayers = new ArrayList<>();
    private final Set<String> retirementHomePlayers = new HashSet<>();
    private final Set<String> completedVillaChoice = new HashSet<>();

    public void setTurnManager(PlayerTurnManager turnManager) {
        this.turnManager = turnManager;
    }

    public void setJobService(JobService jobService) {
        this.jobService = jobService;
    }

    public void setGameController(GameController gameController) {
        this.gameController = gameController;
    }

    // Spielerregistrierung
    public boolean registerPlayer(String id) {
        if (players.size() >= MAX_PLAYERS) {
            System.out.println("[LOBBY-VOLL] Spieler " + id + " konnte nicht beitreten. Lobby ist voll.");
            return false;
        }

        Player player = new Player(id);
        players.add(player);

        int playerIndex = players.size();
        player.setCarColor(CAR_COLORS.get(playerIndex - 1));
        player.addMoney(10000); // Startkapital
        System.out.println("[JOIN] " + id + " wird Spieler " + playerIndex + " mit Farbe " + player.getCarColor());
        return true;
    }

    // Spielvorbereitung: z.B. für zusätzliche Setups
    public void prepareGameStart() {
        for (Player p : players) {
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
        }else{
            setCurrentPlayerStatus();
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

    public Player getPolicePlayer() {
        return players.stream()
                .filter(Player::hasPoliceCard)
                .findFirst()
                .orElse(null);
    }

    public boolean isPlayerInRetirement(String playerId) {
        return retirementHomePlayers.contains(playerId);
    }

    public boolean isPlayerInVilla(String playerId) {
        return millionaersvillaPlayers.contains(playerId);
    }

    public void enterMillionaersvilla(String playerId) {
        if (!millionaersvillaPlayers.contains(playerId)) {
            millionaersvillaPlayers.add(playerId);
        }
        int position = millionaersvillaPlayers.indexOf(playerId);
        if (position < 3) {
            System.out.println("[VILLA] " + playerId + " darf ein LebensKärtchen wählen.");
        } else {
            System.out.println("[VILLA] " + playerId + " geht leer aus.");
        }
    }

    public boolean hasPlayerMadeVillaChoice(String playerId) {
        return completedVillaChoice.contains(playerId);
    }

    public void markVillaChoiceComplete(String playerId) {
        completedVillaChoice.add(playerId);
    }

    public Player findStealableVillaPlayer(String stealerId) {
        if (isPlayerInRetirement(stealerId)) return null;
        for (Player p : players) {
            if (!p.getId().equals(stealerId)
                    && isPlayerInVilla(p.getId())
                    && !p.getLifeCards().isEmpty()) {
                return p;
            }
        }
        return null;
    }

    public boolean canStealLifeCard(String stealerId) {
        return findStealableVillaPlayer(stealerId) != null;
    }

    public boolean stealLifeCard(String stealerId) {
        Player stealer = getPlayerByName(stealerId);
        Player victim = findStealableVillaPlayer(stealerId);

        if (stealer == null || victim == null) return false;
        List<String> victimCards = victim.getLifeCards();

        if (victimCards.isEmpty()) return false;
        String stolenCard = victimCards.remove(0);
        stealer.addLifeCard(stolenCard);
        System.out.println("[DIEBSTAHL] " + stealerId + " stiehlt eine Lebenskarte von " + victim.getId());
        return true;
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

