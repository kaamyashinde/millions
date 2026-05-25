package controller;

/**
 * Outcome of a trade attempt from {@link TradingController}.
 *
 * <p>The sealed hierarchy keeps GUI trade flows explicit: a command either produces a
 * user-facing success message or a user-facing failure message.
 *
 * @author kevindmazali
 * @version 1.0.0
 * @since 2026-05-23
 */
public sealed interface TradeResult {

  /**
   * Trade completed; {@link #message()} is suitable for toasts or status text.
   *
   * @param message user-facing success message
   */
  record Success(String message) implements TradeResult {}

  /**
   * Trade rejected; {@link #message()} is user-facing text for inline display.
   *
   * @param message user-facing failure message
   */
  record Failure(String message) implements TradeResult {}
}
