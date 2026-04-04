package model.session.validation;

import java.math.BigDecimal;

/**
 * One link in a chain-of-responsibility registration validation pipeline.
 *
 * <p>Each implementation checks a single concern. Use {@link #then(RegistrationValidator)} to
 * compose validators in order; the chain stops at the first {@link ValidationResult.Failure}.
 */
@FunctionalInterface
public interface RegistrationValidator {

  /**
   * Validates registration-related input. Unused parameters may be ignored by a given link.
   *
   * @param username      requested username (may be ignored except by username rules)
   * @param pin           numeric PIN characters
   * @param startingMoney starting balance (may be ignored except by money rules)
   * @return success or the first validation failure
   */
  ValidationResult validate(String username, char[] pin, BigDecimal startingMoney);

  /**
   * Returns a validator that runs this handler, then {@code next} if this step succeeds.
   *
   * @param next following validator in the chain
   * @return composed validator
   */
  default RegistrationValidator then(RegistrationValidator next) {
    return (username, pin, startingMoney) -> {
      ValidationResult first = validate(username, pin, startingMoney);
      if (first instanceof ValidationResult.Failure) {
        return first;
      }
      return next.validate(username, pin, startingMoney);
    };
  }
}
