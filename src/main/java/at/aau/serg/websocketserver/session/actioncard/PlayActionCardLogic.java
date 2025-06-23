package at.aau.serg.websocketserver.session.actioncard;

import at.aau.serg.websocketserver.lobby.Lobby;
import at.aau.serg.websocketserver.lobby.LobbyService;
import at.aau.serg.websocketserver.player.Player;
import lombok.Setter;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayActionCardLogic {
    private static final String BANK = "BANK";
    @Setter
    private LobbyService lobbyService;
    private final SecureRandom random = new SecureRandom();
    /**
     * -- SETTER --
     *  Allows tests to inject a deterministic random supplier.
     */
    @Setter
    private IntUnaryOperator randomSupplier;
    private static final Logger logger = Logger.getLogger(PlayActionCardLogic.class.getName());

    public PlayActionCardLogic() {
        this.lobbyService = LobbyService.getInstance();
        this.randomSupplier = bound -> 1 + random.nextInt(bound);
    }

    public void playActionCard(ActionCard actionCard, String lobbyId, String playerId, String decision) {
        Lobby lobby = lobbyService.getLobby(lobbyId);
        String handle = actionCard.getHandle();
        try {
            switch (handle) {
                // === Decision cards ===
                case "decision_animals_enrich":
                    if ("A".equals(decision)) addStudToCar(playerId, lobby);
                    else collectFromEachPlayer(lobby, playerId, 20_000);
                    break;

                case "decision_rollercoaster":
                    if ("A".equals(decision)) transferMoney(playerId, BANK, 50_000);
                    else collectFromEachPlayer(lobby, playerId, 30_000);
                    break;

                case "decision_inherit_villa":
                    if ("A".equals(decision)) addStudToCar(playerId, lobby);
                    else collectFromEachPlayer(lobby, playerId, 20_000);
                    break;

                case "decision_festival_tickets":
                    if ("A".equals(decision)) addStudToCar(playerId, lobby);
                    else transferMoney(getAnyOtherPlayer(lobby, playerId), playerId, 40_000);
                    break;

                case "decision_christmas_gifts":
                    if ("A".equals(decision)) transferMoney(playerId, getAnyOtherPlayer(lobby, playerId), 40_000);
                    else payEachPlayer(lobby, playerId, 20_000);
                    break;

                case "decision_sell_car":
                    if ("A".equals(decision)) transferMoney(BANK, playerId, 50_000);
                    else transferMoney(getAnyOtherPlayer(lobby, playerId), playerId, 30_000);
                    break;

                case "decision_sell_bus":
                    if ("A".equals(decision)) transferMoney(BANK, playerId, 50_000);
                    else collectFromEachPlayer(lobby, playerId, 20_000);
                    break;

                case "decision_quiz_jacht":
                    if ("A".equals(decision)) addStudToCar(playerId, lobby);
                    else transferMoney(BANK, playerId, 50_000);
                    break;

                case "decision_inherit_rv":
                    if ("A".equals(decision)) addStudToCar(playerId, lobby);
                    else transferMoney(BANK, playerId, 50_000);
                    break;

                case "decision_paris_trip":
                    if ("A".equals(decision)) addStudToCar(playerId, lobby);
                    else transferMoney(getAnyOtherPlayer(lobby, playerId), playerId, 40_000);
                    break;

                case "decision_comics_auction":
                    if ("A".equals(decision)) transferMoney(BANK, playerId, 40_000);
                    else collectFromEachPlayer(lobby, playerId, 20_000);
                    break;

                case "decision_sell_farm":
                    if ("A".equals(decision)) transferMoney(BANK, playerId, 50_000);
                    else collectFromEachPlayer(lobby, playerId, 20_000);
                    break;

                // === Spin cards with multiplier ===
                case "spin_cat_videos":
                    spinAndTransfer(playerId, BANK, 20_000);
                    break;

                case "spin_first_album":
                    spinAndTransfer(playerId, BANK, 10_000);
                    break;

                case "spin_livestream_guest":
                case "spin_sushi_investment":
                    spinAndCollect(lobby, playerId, 10_000);
                    break;

                // === Spin cards with color outcome ===
                case "spin_biography_launch":
                case "spin_fashion_line":
                    spinColorOutcome(playerId);
                    break;

                // === Spin cards with placement outcome ===
                case "spin_poetry_contest":
                case "spin_costume_contest":
                    spinPlacementOutcome(playerId);
                    break;

                // === Contest cards: highest spin wins ===
                case "contest_ice_flavor":
                case "contest_trash_collection":
                    contestWinFromBank(lobby, 50_000);
                    break;

                case "contest_videogame_duel":
                case "contest_global_invention":
                    contestCollectFromPlayers(lobby, 20_000);
                    break;

                // === Receive freebies ===
                case "receive_simplify_life":
                    collectFromEachPlayer(lobby, playerId, 10_000);
                    break;

                case "receive_claim_roses":
                case "receive_claim_noise":
                    transferMoney(getAnyOtherPlayer(lobby, playerId), playerId, 30_000);
                    break;

                case "receive_ancestry_research":
                    transferMoney(BANK, playerId, 50_000);
                    break;

                case "receive_volunteer_reward":
                    transferMoney(BANK, playerId, 40_000);
                    break;

                case "receive_kids_book":
                    transferMoney(BANK, playerId, 30_000);
                    break;

                case "receive_food_truck":
                    transferMoney(BANK, playerId, 20_000);
                    break;

                case "receive_portrait_commission":
                case "receive_car_damage":
                    transferMoney(getAnyOtherPlayer(lobby, playerId), playerId, 20_000);
                    break;

                case "receive_team_funding":
                case "receive_band_tour":
                    int studs = getStudCount(lobby, playerId);
                    transferMoney(BANK, playerId, studs * 10_000);
                    break;

                case "receive_keep_it_up":
                    transferMoney(BANK, playerId, 10_000);
                    break;

                case "receive_flea_market":
                    collectFromEachPlayer(lobby, playerId, 10_000);
                    break;

                // === Pay expenses ===
                case "pay_theater_production":
                    payEachPlayer(lobby, playerId, 20_000);
                    break;

                case "pay_cruise_trip":
                case "pay_year_long_trip":
                    payBankBasedOnStuds(lobby, playerId, 10_000);
                    break;

                case "pay_hamster_reward":
                    transferMoney(playerId, getAnyOtherPlayer(lobby, playerId), 20_000);
                    break;

                case "pay_birthday_bill":
                    transferMoney(playerId, BANK, 10_000);
                    break;

                case "pay_pool_build":
                    transferMoney(playerId, BANK, 50_000);
                    break;

                case "pay_gamenight":
                case "pay_share_wealth":
                    payEachPlayer(lobby, playerId, 10_000);
                    break;

                case "pay_buy_pony":
                    transferMoney(playerId, BANK, 40_000);
                    break;

                case "pay_skatepark":
                    transferMoney(playerId, BANK, 30_000);
                    break;

                case "pay_kitchen_reno":
                case "pay_support_local":
                case "pay_tuition_stranger":
                    transferMoney(playerId, getAnyOtherPlayer(lobby, playerId), priceForHandle(handle));
                    break;

                case "pay_roof_damage":
                    transferMoney(playerId, BANK, 20_000);
                    break;

                case "pay_wall_mural":
                    payEachPlayer(lobby, playerId, 20_000);
                    break;

                case "pay_insect_farm":
                    transferMoney(playerId, BANK, 30_000);
                    break;

                default:
                    throw new IllegalArgumentException("Unknown action card: " + handle);
            }
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error executing action card " + handle
                    + " for player " + playerId + ": " + ex.getMessage());
        }
    }

    // --- Helper Methods ---
    private void transferMoney(String fromId, String toId, int amount) {
        Player from = BANK.equals(fromId) ? null : lobbyService.getPlayerService().getPlayerById(fromId);
        Player to   = BANK.equals(toId)   ? null : lobbyService.getPlayerService().getPlayerById(toId);

        int actualAmount = amount;
        if (from != null) {
            int balance = from.getMoney();
            actualAmount = Math.min(balance, amount);
            if (actualAmount > 0) from.removeMoney(actualAmount);
        }
        if (to != null && actualAmount > 0) to.addMoney(actualAmount);
    }

    private void addStudToCar(String playerId, Lobby lobby) {
        Player p = lobby.getPlayers().stream()
                .filter(pl -> pl.getId().equals(playerId))
                .findFirst().orElseThrow();
        p.addPassengerWithLimit("Haustier", 1);
    }

    private List<String> getOtherPlayers(Lobby lobby, String playerId) {
        return lobby.getPlayers().stream()
                .map(Player::getId)
                .filter(id -> !id.equals(playerId))
                .collect(Collectors.toList());
    }

    private String getAnyOtherPlayer(Lobby lobby, String playerId) {
        return getOtherPlayers(lobby, playerId).stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No other players in lobby"));
    }

    private int getStudCount(Lobby lobby, String playerId) {
        return lobby.getPlayers().stream()
                .filter(p -> p.getId().equals(playerId))
                .findFirst()
                .orElseThrow()
                .getAutoPassengers();
    }

    private void collectFromEachPlayer(Lobby lobby, String toId, int amount) {
        for (Player p : lobby.getPlayers()) if (!p.getId().equals(toId)) transferMoney(p.getId(), toId, amount);
    }

    private void payEachPlayer(Lobby lobby, String fromId, int amount) {
        for (Player p : lobby.getPlayers()) if (!p.getId().equals(fromId)) transferMoney(fromId, p.getId(), amount);
    }

    private void payBankBasedOnStuds(Lobby lobby, String playerId, int perStud) {
        int studs = getStudCount(lobby, playerId);
        transferMoney(playerId, BANK, studs * perStud);
    }


    private void spinAndTransfer(String playerId, String toId, int multiplier) {
        int spin = randomSupplier.applyAsInt(10);
        transferMoney(BANK.equals(toId) ? playerId : BANK,
                BANK.equals(toId) ? toId : playerId,
                spin * multiplier);
    }

    private void spinAndCollect(Lobby lobby, String toId, int multiplier) {
        int spin = 1 + random.nextInt(10);
        String other = getAnyOtherPlayer(lobby, toId);
        transferMoney(other, toId, spin * multiplier);
    }

    private void spinColorOutcome(String playerId) {
        int spin = randomSupplier.applyAsInt(10);
        if (spin % 2 == 1) transferMoney(BANK, playerId, 30_000);
        else               transferMoney(playerId, BANK, 30_000);
    }

    private void spinPlacementOutcome(String playerId) {
        int spin = 1 + random.nextInt(10);
        if (spin % 2 == 0) transferMoney(BANK, playerId, 50_000);
        else               transferMoney(BANK, playerId, 10_000);
    }

    private void contestWinFromBank(Lobby lobby, int prize) {
        Map<String, Integer> spins = lobby.getPlayers().stream()
                .collect(Collectors.toMap(Player::getId, p -> 1 + random.nextInt(10)));
        String winner = spins.entrySet().stream()
                .max(Map.Entry.comparingByValue()).get().getKey();
        transferMoney(BANK, winner, prize);
    }

    private void contestCollectFromPlayers(Lobby lobby, int amount) {
        Map<String, Integer> spins = lobby.getPlayers().stream()
                .collect(Collectors.toMap(Player::getId, p -> 1 + random.nextInt(10)));
        String winner = spins.entrySet().stream()
                .max(Map.Entry.comparingByValue()).get().getKey();
        for (Player p : lobby.getPlayers()) if (!p.getId().equals(winner)) transferMoney(p.getId(), winner, amount);
    }

    private int priceForHandle(String handle) {
        switch (handle) {
            case "pay_kitchen_reno": return 20_000;
            case "pay_support_local": return 30_000;
            case "pay_tuition_stranger": return 30_000;
            default: throw new IllegalArgumentException("Unknown price for handle " + handle);
        }
    }
}