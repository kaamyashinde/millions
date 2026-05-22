package model.session;

/**
 * Thrown when a profile cannot be deleted because it is the active session.
 */
public final class ProfileInUseException extends RuntimeException {

  public ProfileInUseException(String message) {
    super(message);
  }
}
