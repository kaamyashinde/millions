package model.trading.transaction;


import model.Player;
import model.exception.ShareNotFoundException;
import model.market.Share;
import model.trading.transactioncalculator.SaleCalculator;

/**
 * A class that represents a sale transaction.
 *
 * @author kevindmazali
 * @version 0.0.3
 * @since 02-02-2026
 */
public class Sale extends Transaction {

  private final SaleCalculator saleCalc;

  /**
   * Constructor for Sale.
   *
   * @param share the share
   * @param day   the trading day
   */
  public Sale(Share share, int day) {
    super(share, day, new SaleCalculator(share));
    this.saleCalc = (SaleCalculator) this.getCalculator();
  }

  /**
   * No separate precondition check needed; the share removal in {@link #execute(Player)} is atomic
   * (validates and removes in one step).
   *
   * @param player the player making the sale
   */
  @Override
  protected void validatePreconditions(Player player) {
  }

  /**
   * Removes the share from the player's portfolio and credits the sale proceeds.
   *
   * @param player the player making the sale
   * @throws ShareNotFoundException if the portfolio does not hold a matching FIFO lot
   */
  @Override
  protected void execute(Player player) {
    if (!player.getPortfolio().removeFifoSliceForSale(this.getShare())) {
      throw new ShareNotFoundException(this.getShare(), player);
    }
    player.addMoney(saleCalc.calculateTotal());
  }
}
