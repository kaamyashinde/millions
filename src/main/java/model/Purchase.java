package model;

/**
 * A class that represents a purchase transaction.
 *
 * @author kevindmazali
 * @version 0.0.1
 * @since 02-02-2026
 */
public class Purchase extends Transaction {

  /**
   * Constructor for Purchase.
   *
   * @param share the share
   * @param week  the week
   */
  public Purchase(Share share, int week) {
    super(share, week, new PurchaseCalculator(share));
  }

  /**
   * Commits the purchase transaction.
   */
  public void commit() {
    // Implementation for committing the purchase transaction
  }
}
