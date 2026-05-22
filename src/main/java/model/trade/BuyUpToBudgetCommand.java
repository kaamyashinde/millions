package model.trade;

import static model.utils.Validator.requirePositive;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import model.Exchange;
import model.InvestableAsset;
import model.Player;
import model.exception.InsufficientFundsException;
import model.transaction.Transaction;
import model.transaction.TransactionSizing;

/**
 * Buys as many shares as possible without exceeding a spend cap and available cash.
 */
public final class BuyUpToBudgetCommand implements TradeCommand {

  private final Exchange exchange;
  private final String symbol;
  private final BigDecimal maxSpend;

  /**
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
    InvestableAsset asset = exchange.getAsset(symbol);
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
