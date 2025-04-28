package Game;

import java.util.*;

public class GameLogic {

    private final List<Player> players = new ArrayList<>();
    private Queue<String> lifeCardsDeck = new LinkedList<>();
    private Queue<String> shareJoyCardsDeck = new LinkedList<>();
    private int currentPlayerIndex = 0;
    private boolean gameEnded = false;

    public void registerPlayer(String id) {
        players.add(new Player(id));
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public void nextTurn() {
        if (allPlayersRetired()) {
            endGame();
        } else {
            do {
                currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            } while (players.get(currentPlayerIndex).isRetired());
        }
    }

    protected boolean allPlayersRetired() {
        return players.stream().allMatch(Player::isRetired);
    }

    protected void endGame() {
        gameEnded = true;
        players.sort(Comparator.comparingInt(this::calculatePlayerWealth).reversed());
        System.out.println("Gewonnen hat: " + players.get(0).getId());
    }

    protected int calculatePlayerWealth(Player player) {
        int wealth = player.getMoney() - player.getDebts() * 25000;
        wealth += player.getLifeCards().size() * 10000; // Beispielwert für LifeCards
        return wealth;
    }

    public void handleUniversityChoice(Player player, boolean chooseUniversity) {
        if (chooseUniversity) {
            for (int i = 0; i < 5; i++) player.addDebt();
        }
    }

    public void handleMarriage(Player player) {
        player.marry();
        player.addLifeCard(drawLifeCard());
    }

    public void handleBaby(Player player, int numberOfChildren) {
        for (int i = 0; i < numberOfChildren; i++) {
            player.addChild();
        }
        player.addLifeCard(drawLifeCard());
    }

    public void handleCreditApplication(Player player) {
        player.addDebt();
        player.addMoney(20000);
    }

    public void handleHouseAcquirement(Player player, String houseName, int price) {
        player.setHouse(houseName);
        player.removeMoney(price);
    }

    protected String drawLifeCard() {
        return lifeCardsDeck.isEmpty() ? "Standard-Leben" : lifeCardsDeck.poll();
    }

    protected String drawShareJoyCard() {
        return shareJoyCardsDeck.isEmpty() ? "Standard-Freude" : shareJoyCardsDeck.poll();
    }

    protected void setLifeCardsDeck(Queue<String> lifeCardsDeck) {
        this.lifeCardsDeck = lifeCardsDeck;
    }

    protected void setShareJoyCardsDeck(Queue<String> shareJoyCardsDeck) {
        this.shareJoyCardsDeck = shareJoyCardsDeck;
    }

    public void playerRetires(Player player) {
        player.retire();
    }

    public boolean isGameEnded() {
        return gameEnded;
    }
}
