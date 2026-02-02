package model;

public class Sale extends Transaction {

  /**
   * Constructor for Sale.
   *
   * @param share the share
   * @param week  the week
   */
  public Sale(Share share, int week) {
    super(share, week, new SaleCalculator(share));
  }

  /**
   * Commits the sale transaction.
   */
  public void commit() {
    // TODO Implementation for committing the sale transaction
  }
}
