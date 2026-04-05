package model.core.trading.transaction;

import model.core.player.Share;
import model.core.player.Player;
import model.core.trading.transactioncalculator.PurchaseCalculator;
import model.exception.InsufficientFundsException;

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
   * @param day   the trading day
   */
  public Purchase(Share share, int day) {
    super(share, day, new PurchaseCalculator(share));
    this.purchaseCalc = (PurchaseCalculator) this.getCalculator();
  }

  /**
   * Checks that the player has enough money to cover the purchase total.
   *
   * @param player the player making the purchase
   * @throws InsufficientFundsException if the player's balance is less than the purchase cost
   */
  @Override
  protected void validatePreconditions(Player player) {
    if (player.getMoney().compareTo(purchaseCalc.calculateTotal()) < 0) {
      throw new InsufficientFundsException();
    }
  }

  /**
   * Withdraws the purchase cost from the player and adds the share to their portfolio.
   *
   * @param player the player making the purchase
   */
  @Override
  protected void execute(Player player) {
    player.withdrawMoney(purchaseCalc.calculateTotal());
    player.getPortfolio().addShare(this.getShare());
  }
}
