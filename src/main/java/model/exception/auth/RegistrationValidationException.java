package model.exception.auth;


import model.session.validation.ValidationError;

/**
 * Thrown when registration input fails model validation.
 *
 * <p>Carries a {@link ValidationError} so callers can branch without matching exception messages.
 */
public class RegistrationValidationException extends RuntimeException {

  private final ValidationError error;

  /**
   * Creates an exception for the given validation failure.
   *
   * @param error typed validation error
   */
  public RegistrationValidationException(ValidationError error) {
    super(error.defaultMessage());
    this.error = error;
  }

  /**
   * Typed failure reason for UI mapping.
   *
   * @return validation error enum constant
   */
  public ValidationError error() {
    return error;
  }
}
