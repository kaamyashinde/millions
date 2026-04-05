package model.session;

import model.player.Player;
import model.market.Exchange;

/**
 * In-memory representation of the currently logged-in user and their game state.
 *
 * @param username           display username shown in the CLI
 * @param normalizedUsername canonical username key used for persistence
 * @param player             active player state
 * @param exchange           active exchange state
 */
public record ActiveSession(
    String username,
    String normalizedUsername,
    Player player,
    Exchange exchange
) {

}
