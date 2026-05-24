package model.trading.command.sell;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import model.core.market.Exchange;
import model.core.player.Player;
import model.trading.command.TradeCommand;
import model.trading.transaction.Transaction;

/**
 * Sells every held symbol in FIFO order, producing one or more {@link Transaction}s per symbol.
 */
public final class SellAllHoldingsCommand implements TradeCommand {

  private final Exchange exchange;

  /**
   * @param exchange exchange providing day for each sale
   */
  public SellAllHoldingsCommand(Exchange exchange) {
    this.exchange = Objects.requireNonNull(exchange, "exchange");
  }

  @Override
  public List<Transaction> execute(Player player) {
    Objects.requireNonNull(player, "player");
    Set<String> symbols = new LinkedHashSet<>();
    player.getPortfolio().getShares().stream()
        .map(share -> share.getAsset().getSymbol())
        .forEach(symbols::add);
    List<Transaction> transactions = new ArrayList<>();
    for (String symbol : symbols) {
      var quantity = player.getPortfolio().totalQuantityForSymbol(symbol);
      if (quantity.signum() <= 0) {
        continue;
      }
      transactions.addAll(
          new SellByQuantityCommand(exchange, symbol, quantity).execute(player));
    }
    return List.copyOf(transactions);
  }

  @Override
  public String describe() {
    return "Sell all holdings";
  }
}
