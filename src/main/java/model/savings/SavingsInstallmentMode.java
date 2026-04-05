package model.savings;

import model.market.Exchange;

/**
 * How a regular savings plan sizes each installment.
 *
 * @author kevindmazali
 * @version 1.0.0
 * @since 29-03-2026
 */
public enum SavingsInstallmentMode {

  /**
   * Buy a fixed number of shares each time.
   */
  FIXED_SHARES,

  /**
   * Spend up to a fixed cash amount (see {@link Exchange#buyUpToBudget}).
   */
  BUDGET
}
