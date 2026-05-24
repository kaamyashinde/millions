package model.trading.savings;


import model.core.market.Exchange;

/**
 * How a regular savings plan sizes each installment.
 *
 * @author kevindmazali
 * @version 1.0.0
 * @since 2026-03-29
 */
public enum SavingsInstallmentMode {

  /** Buy a fixed number of shares each time. */
  FIXED_SHARES,

  /** Spend up to a fixed cash amount (see {@link model.core.market.Exchange#buyUpToBudget}). */
  BUDGET
}
