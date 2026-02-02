package model;

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
   */
  public void commit(Player player) {
    player.addMoney(saleCalc.calculateGross());
    player.getPortfolio().removeShare(this.getShare());
    player.getTransactionArchive().addTransaction(this);
    if (player.getTransactionArchive().getTransactions(getWeek()).contains(this)) {
      this.commited = true;
    }
  }
}
