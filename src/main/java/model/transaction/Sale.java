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
   * @param week  the week
   */
  public Sale(Share share, int week) {
    super(share, week, new SaleCalculator(share));
    this.saleCalc = (SaleCalculator) this.getCalculator();
  }

  /**
   * Commits the sale transaction.
   *
   * @param player the player making the sale
   * @throws ShareNotFoundException    if the player does not have the share being sold in their
   *                                   portfolio.
   * @throws AlreadyCommittedException if the transaction has already been committed.
   */
  public void commit(Player player) {
    if (this.isCommited()) {
      throw new AlreadyCommittedException();
    }
    if (!player.getPortfolio().containsShare(this.getShare())) {
      throw new ShareNotFoundException(this.getShare(), player);
    } else {
      player.getPortfolio().removeShare(this.getShare());
      player.addMoney(saleCalc.calculateTotal());
      player.getTransactionArchive().addTransaction(this);
      this.commited = true;
    }


  }
}
