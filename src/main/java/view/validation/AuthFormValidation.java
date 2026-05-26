package view.validation;

import java.math.BigDecimal;
import java.util.Optional;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextFormatter;
import model.session.validation.ValidationResult;
import model.session.validation.rules.PinValidator;
import model.session.validation.rules.StartingMoneyValidator;
import model.session.validation.rules.UsernameValidator;

/**
 * Live auth-field validation helpers that reuse model-layer registration validators.
 */
public final class AuthFormValidation {

  private static final UsernameValidator USERNAME_VALIDATOR = new UsernameValidator();
  private static final PinValidator PIN_VALIDATOR = new PinValidator();
  private static final StartingMoneyValidator STARTING_MONEY_VALIDATOR =
      new StartingMoneyValidator();

  private AuthFormValidation() {
  }

  /**
   * Validates username text for the authentication form.
   *
   * @param text username field value
   * @return user-facing error when invalid, otherwise empty
   */
  public static Optional<String> usernameError(String text) {
    ValidationResult result = USERNAME_VALIDATOR.validate(text, new char[0], BigDecimal.ZERO);
    if (result instanceof ValidationResult.Failure failure) {
      return Optional.of(mapValidationMessage(failure.error().defaultMessage()));
    }
    return Optional.empty();
  }

  /**
   * Validates PIN text for the authentication form.
   *
   * @param text PIN field value
   * @return user-facing error when invalid, otherwise empty
   */
  public static Optional<String> pinError(String text) {
    ValidationResult result = PIN_VALIDATOR.validate("", text.toCharArray(), BigDecimal.ZERO);
    if (result instanceof ValidationResult.Failure failure) {
      return Optional.of(mapValidationMessage(failure.error().defaultMessage()));
    }
    return Optional.empty();
  }

  /**
   * Validates starting-money text for the registration form.
   *
   * @param text starting-money field value
   * @return user-facing error when invalid, otherwise empty
   */
  public static Optional<String> startingMoneyError(String text) {
    try {
      BigDecimal amount = new BigDecimal(text.trim());
      ValidationResult result = STARTING_MONEY_VALIDATOR.validate("", new char[0], amount);
      if (result instanceof ValidationResult.Failure failure) {
        return Optional.of(mapValidationMessage(failure.error().defaultMessage()));
      }
      return Optional.empty();
    } catch (NumberFormatException e) {
      return Optional.of("Starting money must be a valid number.");
    }
  }

  /**
   * Restricts PIN input to at most eight digits.
   *
   * @param field password field to format
   */
  public static void restrictPinInput(PasswordField field) {
    TextFormatter<String> formatter = new TextFormatter<>(change -> {
      String newText = change.getControlNewText();
      if (newText.matches("\\d{0,8}")) {
        return change;
      }
      return null;
    });
    field.setTextFormatter(formatter);
  }

  /**
   * Maps model validation messages to concise GUI wording.
   *
   * @param message raw validator message
   * @return user-facing message
   */
  public static String mapValidationMessage(String message) {
    if (message == null) {
      return "Invalid input.";
    }
    return switch (message) {
      case "Username must be 3-32 characters using letters, numbers, underscores, or hyphens." ->
          "Username must be 3-32 characters (letters, numbers, _ or -).";
      case "PIN must be 4 to 8 digits." -> "PIN must be 4 to 8 digits.";
      case "Starting money must be non-negative." -> "Starting money must be non-negative.";
      default -> "Invalid input.";
    };
  }
}
