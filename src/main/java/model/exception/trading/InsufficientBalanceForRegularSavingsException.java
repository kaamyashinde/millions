package model.exception.trading;


import model.core.asset.Stock;

/**
 * Optional domain-specific wrapper when a regular savings installment fails due to insufficient
 * cash. Production code may catch {@link InsufficientFundsException} directly; this type carries
 * the affected symbol for reporting or logging.
 *
 * @author kevindmazali
 * @version 1.0.0
 * @since 2026-03-29
 */
public class InsufficientBalanceForRegularSavingsException extends RuntimeException {

  /** Stock symbol for the plan that could not be funded. */
  private final String symbol;

  /**
   * Creates an insufficient-balance exception for a savings plan.
   *
   * @param symbol stock symbol for the plan
   * @param cause  typically {@link InsufficientFundsException}
   */
  public InsufficientBalanceForRegularSavingsException(String symbol, Throwable cause) {
    super("Insufficient balance for regular savings in " + symbol + ".", cause);
    this.symbol = symbol;
  }

  /**
   * Returns the affected savings plan symbol.
   *
   * @return the plan's stock symbol
   */
  public String getSymbol() {
    return symbol;
  }
}
