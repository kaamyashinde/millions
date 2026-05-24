package model.exception.market;

/**
 * Thrown when a user-supplied market data CSV cannot be imported during registration.
 */
public final class MarketDataImportException extends RuntimeException {

  /**
   * Creates an exception with a user-facing message.
   *
   * @param message description of the import failure
   */
  public MarketDataImportException(String message) {
    super(message);
  }

  /**
   * Creates an exception with a user-facing message and underlying cause.
   *
   * @param message description of the import failure
   * @param cause   the underlying exception that triggered this failure
   */
  public MarketDataImportException(String message, Throwable cause) {
    super(message, cause);
  }
}
