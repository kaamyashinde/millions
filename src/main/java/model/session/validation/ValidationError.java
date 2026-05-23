package model.session.validation;


/**
 * Typed registration validation failures shared by the model and view layers.
 */
public enum ValidationError {

  INVALID_USERNAME(
      "Username must be 3-32 characters using letters, numbers, underscores, or hyphens."),
  INVALID_PIN("PIN must be 4 to 8 digits."),
  NEGATIVE_STARTING_MONEY("Starting money must be non-negative.");

  private final String defaultMessage;

  ValidationError(String defaultMessage) {
    this.defaultMessage = defaultMessage;
  }

  /**
   * Default English message for GUI display or logging.
   *
   * @return user-facing description
   */
  public String defaultMessage() {
    return defaultMessage;
  }
}
