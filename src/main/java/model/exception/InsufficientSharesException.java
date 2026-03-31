package model.exception;

import java.math.BigDecimal;

/**
 * Thrown when a sale or aggregate sale requests more shares than the portfolio holds for that
 * symbol.
 *
 * @author kevindmazali
 * @version 1.0.0
 * @since 29-03-2026
 */
public class InsufficientSharesException extends RuntimeException {

  /** Stock symbol that was undersized. */
  private final String symbol;

  /** Total quantity the caller attempted to sell. */
  private final BigDecimal requestedQuantity;

  /**
   * @param symbol            stock symbol
   * @param requestedQuantity quantity the caller tried to sell
   */
  public InsufficientSharesException(String symbol, BigDecimal requestedQuantity) {
    super("Not enough shares of " + symbol + " to sell " + requestedQuantity + ".");
    this.symbol = symbol;
    this.requestedQuantity = requestedQuantity;
  }

  /**
   * @return the stock symbol
   */
  public String getSymbol() {
    return symbol;
  }

  /**
   * @return the quantity that could not be satisfied
   */
  public BigDecimal getRequestedQuantity() {
    return requestedQuantity;
  }
}
