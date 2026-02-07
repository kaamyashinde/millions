package model.transaction;

import model.Player;
import model.Share;
import model.exception.AlreadyCommittedException;
import model.exception.InsufficientFundsException;
import model.transactioncalculator.PurchaseCalculator;

/**
 * A class that represents a purchase transaction.
 *
 * @author kevindmazali
 * @version 0.0.3
 * @since 02-02-2026
 */
public class Purchase extends Transaction {

  private final PurchaseCalculator purchaseCalc;

  /**
   * Constructor for Purchase.
   *
   * @param share the share
   * @param week  the week
   */
  public Purchase(Share share, int week) {
    super(share, week, new PurchaseCalculator(share));
    this.purchaseCalc = (PurchaseCalculator) this.getCalculator();
  }

  /**
   * Commits the purchase transaction.
   *
   * @param player the player making the purchase
   * @throws InsufficientFundsException if the player does not have enough money to complete the
   *                                    purchase.
   * @throws IllegalStateException      if the transaction was not added to the archive after
   *                                    committing.
   * @throws AlreadyCommittedException  if the transaction has already been committed.
   */
  public void commit(Player player) {

    if (this.isCommited()) {
      throw new AlreadyCommittedException();
    }

    if (player.getMoney().compareTo(purchaseCalc.calculateTotal()) < 0) {
      throw new InsufficientFundsException();
    } else {
      player.withdrawMoney(this.purchaseCalc.calculateTotal());
      player.getPortfolio().addShare(this.getShare());
      player.getTransactionArchive().addTransaction(this);
    }

    if (player.getTransactionArchive().getTransactions(getWeek()).contains(this)) {
      this.commited = true;
    } else {
      throw new IllegalStateException("Transaction was not added to the archive.");
    }
  }
}
