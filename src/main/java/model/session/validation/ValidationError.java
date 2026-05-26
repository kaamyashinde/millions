package model.session.validation;


/**
 * Typed registration validation failures shared by the model and view layers.
 */
public enum ValidationError {

  /** Username does not match profile naming rules. */
  INVALID_USERNAME(
      "Username must be 3-32 characters using letters, numbers, underscores, or hyphens."),
  /** PIN is not 4 to 8 digits. */
  INVALID_PIN("PIN must be 4 to 8 digits."),
  /** Starting money is missing or negative. */
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
