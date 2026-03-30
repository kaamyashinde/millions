package model.transaction;


import model.Player;
import model.Share;
import model.exception.AlreadyCommittedException;
import model.exception.ShareNotFoundException;
import model.transactioncalculator.SaleCalculator;

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
   * @param day  the trading day
   */
  public Sale(Share share, int day) {
    super(share, day, new SaleCalculator(share));
    this.saleCalc = (SaleCalculator) this.getCalculator();
  }

  /**
   * Commits the sale transaction.
   *
   * @param player the player making the sale
   * @throws ShareNotFoundException    if the portfolio does not hold a matching FIFO lot (symbol,
   *                                   purchase price, and at least the slice quantity).
   * @throws AlreadyCommittedException if the transaction has already been committed.
   */
  public void commit(Player player) {
    if (this.isCommited()) {
      throw new AlreadyCommittedException();
    }
    if (!player.getPortfolio().removeFifoSliceForSale(this.getShare())) {
      throw new ShareNotFoundException(this.getShare(), player);
    }
    player.addMoney(saleCalc.calculateTotal());
    player.getTransactionArchive().addTransaction(this);
    this.commited = true;
  }
}
