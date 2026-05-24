package controller;

/**
 * Outcome of a trade attempt from {@link TradingController}.
 */
public sealed interface TradeResult {

  /**
   * Trade completed; {@link #message()} is suitable for toasts or status text.
   */
  record Success(String message) implements TradeResult {}

  /**
   * Trade rejected; {@link #message()} is user-facing text for inline display.
   */
  record Failure(String message) implements TradeResult {}
}
