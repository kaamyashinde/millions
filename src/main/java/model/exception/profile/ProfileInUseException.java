package model.exception.profile;


/**
 * Thrown when a profile cannot be deleted because it is the active session.
 */
public final class ProfileInUseException extends RuntimeException {

  /**
   * Creates an exception for a profile that cannot be modified while active.
   *
   * @param message user-facing explanation
   */
  public ProfileInUseException(String message) {
    super(message);
  }
}
