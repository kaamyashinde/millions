package model.persistence;

/**
 * Wraps checked persistence failures in a single runtime exception for the CLI layer.
 */
public class PersistenceException extends RuntimeException {

  /**
   * Creates a persistence exception with a message and cause.
   *
   * @param message failure summary
   * @param cause original checked exception
   */
  public PersistenceException(String message, Throwable cause) {
    super(message, cause);
  }
}
