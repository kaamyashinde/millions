package model.session.validation;

import java.math.BigDecimal;
import model.persistence.ProfileDirectories;

/**
 * Validates that the username matches local profile naming rules.
 */
public final class UsernameValidator implements RegistrationValidator {

  @Override
  public ValidationResult validate(String username, char[] pin, BigDecimal startingMoney) {
    if (!ProfileDirectories.isValidUsername(username)) {
      return new ValidationResult.Failure(ValidationError.INVALID_USERNAME);
    }
    return new ValidationResult.Success();
  }
}
