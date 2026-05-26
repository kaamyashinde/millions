package model.session.validation.rules;


import model.session.validation.RegistrationValidator;
import model.session.validation.ValidationError;
import model.session.validation.ValidationResult;

import java.math.BigDecimal;
import java.util.stream.IntStream;

/**
 * Validates PIN length (4–8) and that every character is a digit.
 */
public final class PinValidator implements RegistrationValidator {

  /**
   * Creates a PIN validator.
   */
  public PinValidator() {
  }

  @Override
  public ValidationResult validate(String username, char[] pin, BigDecimal startingMoney) {
    if (pin == null || pin.length < 4 || pin.length > 8) {
      return new ValidationResult.Failure(ValidationError.INVALID_PIN);
    }
    if (!IntStream.range(0, pin.length).allMatch(index -> Character.isDigit(pin[index]))) {
      return new ValidationResult.Failure(ValidationError.INVALID_PIN);
    }
    return new ValidationResult.Success();
  }
}
