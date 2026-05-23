package model.trading.command.sell;


import model.trading.command.TradeCommand;

import java.util.List;
import java.util.Objects;
import model.core.market.Exchange;
import model.core.player.Player;
import model.core.asset.Share;
import model.trading.transaction.Sale;
import model.trading.transaction.Transaction;

/**
 * Sells a single {@link Share} slice (one FIFO lot or partial lot) at the current market price.
 */
public final class SellCommand implements TradeCommand {

  private final Exchange exchange;
  private final Share share;

  /**
   * @param exchange exchange providing the trading day
   * @param share    the lot or slice to sell
   */
  public SellCommand(Exchange exchange, Share share) {
    this.exchange = Objects.requireNonNull(exchange, "exchange");
    this.share = Objects.requireNonNull(share, "share");
  }

  @Override
  public List<Transaction> execute(Player player) {
    Sale sale = new Sale(share, exchange.getDay());
    sale.commit(player);
    return List.of(sale);
  }

  @Override
  public String describe() {
    return "Sell " + share.getQuantity() + " of " + share.getAsset().getSymbol();
  }
}
