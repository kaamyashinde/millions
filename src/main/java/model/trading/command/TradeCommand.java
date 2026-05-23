package model.trading.command;


import model.core.market.Exchange;

import java.util.List;
import model.core.player.Player;
import model.trading.transaction.Transaction;

/**
 * Command abstraction for trade execution. Encapsulates validation, transaction construction, and
 * commit so callers can queue, log, or batch trades without coupling to {@link model.core.market.Exchange}
 * internals.
 */
public interface TradeCommand {

  /**
   * Performs the trade against the given player (withdraws/credits cash, updates portfolio,
   * archives committed transactions).
   *
   * @param player the player to trade for
   * @return all committed transactions from this command (one or many)
   */
  List<Transaction> execute(Player player);

  /**
   * Human-readable description for logging or UI (e.g. audit trail).
   *
   * @return non-null description
   */
  String describe();
}
