package model.session.validation.rules;


import model.session.validation.RegistrationValidator;
import model.session.validation.ValidationError;
import model.session.validation.ValidationResult;

import java.math.BigDecimal;
import model.persistence.profile.ProfileDirectories;

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
