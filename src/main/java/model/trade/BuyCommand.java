package model.trade;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import model.Exchange;
import model.InvestableAsset;
import model.Player;
import model.Share;
import model.transaction.Purchase;
import model.transaction.Transaction;

/**
 * Buys a fixed quantity of an listed asset at the current ask price.
 */
public final class BuyCommand implements TradeCommand {

  private final Exchange exchange;
  private final String symbol;
  private final BigDecimal quantity;

  /**
   * @param exchange exchange providing day and asset lookup
   * @param symbol   stock or fund symbol
   * @param quantity positive quantity of shares/units
   */
  public BuyCommand(Exchange exchange, String symbol, BigDecimal quantity) {
    this.exchange = Objects.requireNonNull(exchange, "exchange");
    this.symbol = Objects.requireNonNull(symbol, "symbol");
    this.quantity = Objects.requireNonNull(quantity, "quantity");
  }

  @Override
  public List<Transaction> execute(Player player) {
    InvestableAsset assetToBuy = exchange.getAsset(symbol);
    if (assetToBuy == null) {
      throw new IllegalArgumentException("Unknown asset symbol: " + symbol);
    }
    Share shareToBuy = new Share(assetToBuy, quantity, assetToBuy.getSalesPrice());
    Purchase purchase = new Purchase(shareToBuy, exchange.getDay());
    purchase.commit(player);
    return List.of(purchase);
  }

  @Override
  public String describe() {
    return "Buy " + quantity + " of " + symbol;
  }
}
