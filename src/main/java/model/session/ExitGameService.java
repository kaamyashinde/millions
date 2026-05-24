package model.session;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import model.core.player.Player;
import model.session.profile.ProfileService;
import model.trading.transaction.Transaction;

/**
 * Performs the final liquidation and profile deletion flow for exiting the game.
 */
final class ExitGameService {

  private final ProfileService profileService;

  /**
   * @param profileService profile operations used for PIN checks and deletion
   */
  ExitGameService(ProfileService profileService) {
    this.profileService = profileService;
  }

  /**
   * Liquidates holdings, clears savings plans, and deletes the profile directory.
   *
   * @param session active session to close
   * @param pin PIN confirming the deletion
   * @return summary of the liquidation
   */
  ExitGameResult exitAndDelete(ActiveSession session, char[] pin) {
    Player player = session.player();
    Set<String> symbols = heldSymbols(player);
    profileService.verifyDeletionPin(session.username(), pin);
    List<Transaction> transactions = session.exchange().sellAllHoldings(player);
    player.clearRegularSavingsPlans();
    BigDecimal finalCash = player.getMoney();
    profileService.deleteProfileDirectory(session.username());
    return new ExitGameResult(symbols.size(), transactions.size(), finalCash);
  }

  /**
   * Collects distinct symbols currently held by the player before liquidation.
   *
   * @param player player whose portfolio is inspected
   * @return held symbols in portfolio order
   */
  private static Set<String> heldSymbols(Player player) {
    Set<String> symbols = new LinkedHashSet<>();
    player.getPortfolio().getShares().stream()
        .map(share -> share.getAsset().getSymbol())
        .forEach(symbols::add);
    return symbols;
  }
}
