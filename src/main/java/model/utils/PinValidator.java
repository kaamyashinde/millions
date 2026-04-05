package model.utils;

import java.math.BigDecimal;

/**
 * Validates PIN length (4–8) and that every character is a digit.
 */
public final class PinValidator implements RegistrationValidator {

  @Override
  public ValidationResult validate(String username, char[] pin, BigDecimal startingMoney) {
    if (pin == null || pin.length < 4 || pin.length > 8) {
      return new ValidationResult.Failure(ValidationError.INVALID_PIN);
    }
    for (char digit : pin) {
      if (!Character.isDigit(digit)) {
        return new ValidationResult.Failure(ValidationError.INVALID_PIN);
      }
    }
    return new ValidationResult.Success();
  }
}
