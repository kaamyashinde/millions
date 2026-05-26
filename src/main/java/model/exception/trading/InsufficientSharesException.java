package model.exception.trading;


import java.math.BigDecimal;
import model.core.asset.Stock;

/**
 * Thrown when a sale or aggregate sale requests more shares than the portfolio holds for that
 * symbol.
 *
 * @author kevindmazali
 * @version 1.0.0
 * @since 2026-03-29
 */
public class InsufficientSharesException extends RuntimeException {

  /** Stock symbol that was undersized. */
  private final String symbol;

  /** Total quantity the caller attempted to sell. */
  private final BigDecimal requestedQuantity;

  /**
   * Creates an insufficient-shares exception.
   *
   * @param symbol            stock symbol
   * @param requestedQuantity quantity the caller tried to sell
   */
  public InsufficientSharesException(String symbol, BigDecimal requestedQuantity) {
    super("Not enough shares of " + symbol + " to sell " + requestedQuantity + ".");
    this.symbol = symbol;
    this.requestedQuantity = requestedQuantity;
  }

  /**
   * Returns the affected stock symbol.
   *
   * @return the stock symbol
   */
  public String getSymbol() {
    return symbol;
  }

  /**
   * Returns the requested quantity.
   *
   * @return the quantity that could not be satisfied
   */
  public BigDecimal getRequestedQuantity() {
    return requestedQuantity;
  }
}
