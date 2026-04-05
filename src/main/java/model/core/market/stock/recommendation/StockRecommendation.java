package model.core.market.stock.recommendation;

/**
 * Recommendation levels shown to the player for a stock.
 *
 * <p>The values are intentionally simple and presentation-friendly so they can be reused in view
 * components without coupling them to price-update logic.
 *
 * @author kaamyashinde
 * @version 1.0.0
 * @since 2026-04-04
 */
public enum StockRecommendation {
  BUY,
  HOLD,
  SELL;

  /**
   * Returns the display text used in the UI.
   *
   * @return uppercase recommendation text
   */
  public String getDisplayText() {
    return name();
  }
}
