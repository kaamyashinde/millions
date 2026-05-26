package model.trading.command.sell;


import static util.Validator.requirePositive;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import model.core.asset.Share;
import model.core.market.Exchange;
import model.core.player.Player;
import model.trading.calculator.SaleCalculator;
import model.trading.command.TradeCommand;
import model.trading.transaction.Transaction;

/**
 * Sells FIFO slices until cumulative net proceeds reach {@code targetNet} or holdings are
 * exhausted.
 */
public final class SellUpToTargetNetCommand implements TradeCommand {

  private final Exchange exchange;
  private final String symbol;
  private final BigDecimal targetNet;

  /**
   * Creates a target-net sell command.
   *
   * @param exchange  exchange providing day for each sale
   * @param symbol    asset symbol
   * @param targetNet maximum total net cash to raise (positive)
   */
  public SellUpToTargetNetCommand(Exchange exchange, String symbol, BigDecimal targetNet) {
    this.exchange = Objects.requireNonNull(exchange, "exchange");
    this.symbol = Objects.requireNonNull(symbol, "symbol");
    this.targetNet = Objects.requireNonNull(targetNet, "targetNet");
  }

  @Override
  public List<Transaction> execute(Player player) {
    requirePositive(targetNet, "targetNet");
    List<Transaction> transactions = new ArrayList<>();
    BigDecimal remainingTarget = targetNet;
    while (remainingTarget.signum() > 0
        && player.getPortfolio().totalQuantityForSymbol(symbol).signum() > 0) {
      Share slice =
          player.getPortfolio().buildNextFifoSliceForTargetNet(symbol, remainingTarget);
      if (slice == null) {
        break;
      }
      transactions.addAll(new SellCommand(exchange, slice).execute(player));
      BigDecimal net = new SaleCalculator(slice).calculateTotal();
      remainingTarget = remainingTarget.subtract(net);
    }
    return List.copyOf(transactions);
  }

  @Override
  public String describe() {
    return "Sell up to net " + targetNet + " of " + symbol;
  }
}
