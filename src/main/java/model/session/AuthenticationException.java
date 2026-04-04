package model.session;

/**
 * Signals invalid login credentials for a local user profile.
 */
public class AuthenticationException extends RuntimeException {

  /**
   * Creates an authentication exception with a user-facing message.
   *
   * @param message failure summary
   */
  public AuthenticationException(String message) {
    super(message);
  }
}
