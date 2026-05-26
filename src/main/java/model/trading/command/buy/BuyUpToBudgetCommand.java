package model.trading.command.buy;


import static util.Validator.requirePositive;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import model.core.asset.InvestableAsset;
import model.core.market.Exchange;
import model.core.player.Player;
import model.exception.trading.InsufficientFundsException;
import model.trading.command.TradeCommand;
import model.trading.transaction.Transaction;
import model.trading.transaction.TransactionSizing;

/**
 * Buys as many shares as possible without exceeding a spend cap and available cash.
 */
public final class BuyUpToBudgetCommand implements TradeCommand {

  private final Exchange exchange;
  private final String symbol;
  private final BigDecimal maxSpend;

  /**
   * Creates a budget-capped buy command.
   *
   * @param exchange exchange providing asset lookup
   * @param symbol   stock or fund symbol
   * @param maxSpend upper bound on total purchase cost (gross + commission); must be positive
   */
  public BuyUpToBudgetCommand(Exchange exchange, String symbol, BigDecimal maxSpend) {
    this.exchange = Objects.requireNonNull(exchange, "exchange");
    this.symbol = Objects.requireNonNull(symbol, "symbol");
    this.maxSpend = Objects.requireNonNull(maxSpend, "maxSpend");
  }

  @Override
  public List<Transaction> execute(Player player) {
    InvestableAsset asset = exchange.listings().getAsset(symbol);
    if (asset == null) {
      throw new IllegalArgumentException("Unknown asset symbol: " + symbol);
    }
    requirePositive(maxSpend, "maxSpend");
    BigDecimal budget = maxSpend.min(player.getMoney());
    BigDecimal quantity = TransactionSizing.maxQuantityForBudget(asset, budget);
    if (quantity.signum() <= 0) {
      throw new InsufficientFundsException();
    }
    return new BuyCommand(exchange, symbol, quantity).execute(player);
  }

  @Override
  public String describe() {
    return "Buy up to budget " + maxSpend + " of " + symbol;
  }
}
