package model;

/**
 * A class that represents a purchase transaction.
 *
 * @author kevindmazali
 * @version 0.0.2
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
   */
  public void commit(Player player) {
    try {
      player.withdrawMoney(this.purchaseCalc.calculateTotal());
      player.getPortfolio().addShare(this.getShare());
      player.getTransactionArchive().addTransaction(this);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Insufficient funds to complete the purchase.");
    } finally {
      if (player.getTransactionArchive().getTransactions(getWeek()).contains(this)) {
        this.commited = true;
      } else {
        System.out.println("Purchase could not be completed.");
      }
    }
  }
}
