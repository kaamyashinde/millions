package model.session.validation;

import java.math.BigDecimal;

/**
 * Validates that starting money is present and not negative.
 */
public final class StartingMoneyValidator implements RegistrationValidator {

  @Override
  public ValidationResult validate(String username, char[] pin, BigDecimal startingMoney) {
    if (startingMoney == null || startingMoney.compareTo(BigDecimal.ZERO) < 0) {
      return new ValidationResult.Failure(ValidationError.NEGATIVE_STARTING_MONEY);
    }
    return new ValidationResult.Success();
  }
}
