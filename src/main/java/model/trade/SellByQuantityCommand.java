package model.trade;

import static model.utils.Validator.requirePositive;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import model.Exchange;
import model.Player;
import model.Share;
import model.exception.InsufficientSharesException;
import model.transaction.Transaction;

/**
 * Sells a total quantity across FIFO lots, producing one committed {@link model.transaction.Sale}
 * per slice as needed.
 */
public final class SellByQuantityCommand implements TradeCommand {

  private final Exchange exchange;
  private final String symbol;
  private final BigDecimal quantity;

  /**
   * @param exchange exchange providing day for each sale
   * @param symbol   asset symbol to sell
   * @param quantity total shares to sell (positive)
   */
  public SellByQuantityCommand(Exchange exchange, String symbol, BigDecimal quantity) {
    this.exchange = Objects.requireNonNull(exchange, "exchange");
    this.symbol = Objects.requireNonNull(symbol, "symbol");
    this.quantity = Objects.requireNonNull(quantity, "quantity");
  }

  @Override
  public List<Transaction> execute(Player player) {
    requirePositive(quantity, "quantity");
    if (player.getPortfolio().totalQuantityForSymbol(symbol).compareTo(quantity) < 0) {
      throw new InsufficientSharesException(symbol, quantity);
    }
    List<Transaction> transactions = new ArrayList<>();
    BigDecimal remaining = quantity;
    while (remaining.signum() > 0) {
      Share slice = player.getPortfolio().buildNextFifoSaleSlice(symbol, remaining);
      if (slice == null) {
        throw new InsufficientSharesException(symbol, quantity);
      }
      transactions.addAll(new SellCommand(exchange, slice).execute(player));
      remaining = remaining.subtract(slice.getQuantity());
    }
    return List.copyOf(transactions);
  }

  @Override
  public String describe() {
    return "Sell by quantity " + quantity + " of " + symbol;
  }
}
