package model.session.validation.rules;


import java.math.BigDecimal;
import model.persistence.profile.ProfilePaths;
import model.session.validation.RegistrationValidator;
import model.session.validation.ValidationError;
import model.session.validation.ValidationResult;

/**
 * Validates that the username matches local profile naming rules.
 */
public final class UsernameValidator implements RegistrationValidator {

  /**
   * Creates a username validator.
   */
  public UsernameValidator() {
  }

  @Override
  public ValidationResult validate(String username, char[] pin, BigDecimal startingMoney) {
    if (!ProfilePaths.isValidUsername(username)) {
      return new ValidationResult.Failure(ValidationError.INVALID_USERNAME);
    }
    return new ValidationResult.Success();
  }
}
