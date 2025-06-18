package at.aau.serg.websocketserver.session.actioncard;

import java.util.List;

public class PlayActionCardLogic {

    /**
     * Executes the effect of an ActionCard based on the player's decision or spin result.
     * @param actionCard the card being played
     * @param lobbyId    the game lobby identifier
     * @param playerId   the ID of the player making the play
     * @param decision   the player's choice ("A","B"), spin result (number or color), or null
     */
    public void playActionCard(ActionCard actionCard, String lobbyId, String playerId, String decision) {
        String handle = actionCard.getHandle();
        switch (handle) {
            // Decision cards with two options A and B
            case "decision_animals_enrich":
                if ("A".equals(decision)) {
                    addStudToCar(playerId, 1);
                } else {
                    // From each other player
                    for (String other : getOtherPlayers(lobbyId, playerId)) {
                        transferMoney(other, playerId, 20000);
                    }
                }
                break;
            case "decision_rollercoaster":
                if ("A".equals(decision)) {
                    transferMoney(playerId, BANK, 50000);
                } else {
                    for (String friend : getOtherPlayers(lobbyId, playerId)) {
                        transferMoney(playerId, friend, 30000);
                    }
                }
                break;
            case "decision_inherit_villa":
                if ("A".equals(decision)) {
                    addStudToCar(playerId, 1);
                } else {
                    for (String other : getOtherPlayers(lobbyId, playerId)) {
                        transferMoney(other, playerId, 20000);
                    }
                }
                break;
            case "decision_festival_tickets":
                if ("A".equals(decision)) {
                    addStudToCar(playerId, 1);
                } else {
                    transferMoney(getAnyOtherPlayer(lobbyId, playerId), playerId, 40000);
                }
                break;
            case "decision_christmas_gifts":
                if ("A".equals(decision)) {
                    transferMoney(playerId, getAnyOtherPlayer(lobbyId, playerId), 40000);
                } else {
                    for (String friend : getOtherPlayers(lobbyId, playerId)) {
                        transferMoney(playerId, friend, 20000);
                    }
                }
                break;
            case "decision_sell_car":
                if ("A".equals(decision)) {
                    transferMoney(BANK, playerId, 50000);
                } else {
                    transferMoney(getAnyOtherPlayer(lobbyId, playerId), playerId, 30000);
                }
                break;
            case "decision_sell_bus":
                if ("A".equals(decision)) {
                    transferMoney(BANK, playerId, 50000);
                } else {
                    for (String other : getOtherPlayers(lobbyId, playerId)) {
                        transferMoney(other, playerId, 20000);
                    }
                }
                break;
            case "decision_quiz_jacht":
                if ("A".equals(decision)) {
                    addStudToCar(playerId, 1);
                } else {
                    transferMoney(BANK, playerId, 50000);
                }
                break;
            case "decision_inherit_rv":
                if ("A".equals(decision)) {
                    addStudToCar(playerId, 1);
                } else {
                    transferMoney(BANK, playerId, 50000);
                }
                break;
            case "decision_paris_trip":
                if ("A".equals(decision)) {
                    addStudToCar(playerId, 1);
                } else {
                    transferMoney(getAnyOtherPlayer(lobbyId, playerId), playerId, 40000);
                }
                break;
            case "decision_comics_auction":
                if ("A".equals(decision)) {
                    transferMoney(BANK, playerId, 40000);
                } else {
                    for (String other : getOtherPlayers(lobbyId, playerId)) {
                        transferMoney(other, playerId, 20000);
                    }
                }
                break;
            case "decision_sell_farm":
                if ("A".equals(decision)) {
                    transferMoney(BANK, playerId, 50000);
                } else {
                    for (String other : getOtherPlayers(lobbyId, playerId)) {
                        transferMoney(other, playerId, 20000);
                    }
                }
                break;

            // Spin cards with multiplier on spun number
            case "spin_cat_videos":
                int spin1 = Integer.parseInt(decision);
                transferMoney(BANK, playerId, spin1 * 20000);
                break;
            case "spin_first_album":
                int spin2 = Integer.parseInt(decision);
                transferMoney(BANK, playerId, spin2 * 10000);
                break;
            case "spin_livestream_guest":
                int spin3 = Integer.parseInt(decision);
                for (String other : getOtherPlayers(lobbyId, playerId)) {
                    transferMoney(other, playerId, spin3 * 10000);
                }
                break;
            case "spin_sushi_investment":
                int spin4 = Integer.parseInt(decision);
                for (String other : getOtherPlayers(lobbyId, playerId)) {
                    transferMoney(other, playerId, spin4 * 10000);
                }
                break;

            // Spin cards with color outcomes
            case "spin_biography_launch":
                if ("Rot".equalsIgnoreCase(decision)) {
                    transferMoney(BANK, playerId, 30000);
                } else {
                    transferMoney(playerId, BANK, 30000);
                }
                break;
            case "spin_fashion_line":
                if ("Rot".equalsIgnoreCase(decision)) {
                    transferMoney(BANK, playerId, 30000);
                } else {
                    transferMoney(playerId, BANK, 30000);
                }
                break;
            case "spin_poetry_contest":
                if ("Rot".equalsIgnoreCase(decision)) {
                    transferMoney(BANK, playerId, 10000);
                } else {
                    transferMoney(BANK, playerId, 50000);
                }
                break;
            case "spin_costume_contest":
                if ("Rot".equalsIgnoreCase(decision)) {
                    transferMoney(BANK, playerId, 10000);
                } else {
                    transferMoney(BANK, playerId, 50000);
                }
                break;

            // Contest cards: highest spinner wins
            case "contest_ice_flavor":
            case "contest_trash_collection":
            case "contest_videogame_duel":
            case "contest_global_invention":
                String winner = findHighestSpinner(lobbyId);
                int amount = handle.equals("contest_videogame_duel") || handle.equals("contest_global_invention")
                        ? 20000 * (getAllPlayers(lobbyId).size() - 1)
                        : 50000;
                if (amount == 50000) {
                    transferMoney(BANK, winner, 50000);
                } else {
                    for (String other : getOtherPlayers(lobbyId, winner)) {
                        transferMoney(other, winner, 20000);
                    }
                }
                break;

            // Receive freebies
            case "receive_simplify_life":
                for (String other : getOtherPlayers(lobbyId, playerId)) {
                    transferMoney(other, playerId, 10000);
                }
                break;
            case "receive_claim_roses":
            case "receive_claim_noise":
                transferMoney(getAnyOtherPlayer(lobbyId, playerId), playerId, 30000);
                break;
            case "receive_ancestry_research":
                transferMoney(BANK, playerId, 50000);
                break;
            case "receive_volunteer_reward":
                transferMoney(BANK, playerId, 40000);
                break;
            case "receive_kids_book":
                transferMoney(BANK, playerId, 30000);
                break;
            case "receive_food_truck":
                transferMoney(BANK, playerId, 20000);
                break;
            case "receive_portrait_commission":
                transferMoney(getAnyOtherPlayer(lobbyId, playerId), playerId, 20000);
                break;
            case "receive_cat_rescue":
                for (String other : getOtherPlayers(lobbyId, playerId)) {
                    transferMoney(other, playerId, 20000);
                }
                break;
            case "receive_dance_victory":
            case "receive_car_damage":
                transferMoney(getAnyOtherPlayer(lobbyId, playerId), playerId, 20000);
                break;
            case "receive_team_funding":
            case "receive_band_tour":
                int studs = getStudCount(playerId);
                transferMoney(BANK, playerId, studs * 10000);
                break;
            case "receive_keep_it_up":
                transferMoney(BANK, playerId, 10000);
                break;
            case "receive_flea_market":
                for (String other : getOtherPlayers(lobbyId, playerId)) {
                    transferMoney(other, playerId, 10000);
                }
                break;

            // Pay expenses
            case "pay_theater_production":
                for (String other : getOtherPlayers(lobbyId, playerId)) {
                    transferMoney(playerId, other, 20000);
                }
                break;
            case "pay_cruise_trip":
            case "pay_year_long_trip":
                int cnt = getStudCount(playerId);
                transferMoney(playerId, BANK, cnt * 10000);
                break;
            case "pay_hamster_reward":
                transferMoney(playerId, getAnyOtherPlayer(lobbyId, playerId), 20000);
                break;
            case "pay_birthday_bill":
                transferMoney(playerId, BANK, 10000);
                break;
            case "pay_pool_build":
                transferMoney(playerId, BANK, 50000);
                break;
            case "pay_gamenight":
            case "pay_share_wealth":
                for (String other : getOtherPlayers(lobbyId, playerId)) {
                    transferMoney(playerId, other, 10000);
                }
                break;
            case "pay_buy_pony":
                transferMoney(playerId, BANK, 40000);
                break;
            case "pay_skatepark":
                transferMoney(playerId, BANK, 30000);
                break;
            case "pay_kitchen_reno":
            case "pay_support_local":
            case "pay_tuition_stranger":
                transferMoney(playerId, getAnyOtherPlayer(lobbyId, playerId),  priceForHandle(handle));
                break;
            case "pay_roof_damage":
                transferMoney(playerId, BANK, 20000);
                break;
            case "pay_wall_mural":
                for (String other : getOtherPlayers(lobbyId, playerId)) {
                    transferMoney(playerId, other, 20000);
                }
                break;
            case "pay_insect_farm":
                transferMoney(playerId, BANK, 30000);
                break;

            default:
                throw new IllegalArgumentException("Unknown action card: " + handle);
        }
    }

    // Placeholder constants and helper methods to be implemented elsewhere:
    private static final String BANK = "BANK";
    private List<String> getOtherPlayers(String lobbyId, String excludePlayerId) { /*...*/ return null; }
    private String getAnyOtherPlayer(String lobbyId, String excludePlayerId) { /*...*/ return null; }
    private List<String> getAllPlayers(String lobbyId) { /*...*/ return null; }
    private int findHighestSpinner(String lobbyId) { /*...*/ return 0; }
    private int getStudCount(String playerId) { /*...*/ return 0; }
    private void addStudToCar(String playerId, int count) { /*...*/ }
    private void transferMoney(String fromPlayerId, String toPlayerId, int amount) { /*...*/ }
    private int priceForHandle(String handle) {
        switch(handle) {
            case "pay_kitchen_reno": return 20000;
            case "pay_support_local": return 30000;
            case "pay_tuition_stranger": return 30000;
            default: return 0;
        }
    }
}

