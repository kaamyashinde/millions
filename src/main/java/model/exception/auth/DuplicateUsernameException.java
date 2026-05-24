package model.exception.auth;


/**
 * Signals that a new profile username is already taken.
 */
public class DuplicateUsernameException extends RuntimeException {

  /**
   * Creates a duplicate-username exception with a user-facing message.
   *
   * @param message failure summary
   */
  public DuplicateUsernameException(String message) {
    super(message);
  }
}
