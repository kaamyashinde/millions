package model.session.validation.rules;


import model.session.validation.ValidationError;
import model.session.validation.ValidationResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import model.exception.auth.RegistrationValidationException;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class UsernameValidatorTest {

  private final UsernameValidator validator = new UsernameValidator();

  @Test
  void acceptsValidUsername() {
    ValidationResult result = validator.validate("valid_user", "1234".toCharArray(), BigDecimal.TEN);
    assertInstanceOf(ValidationResult.Success.class, result);
  }

  @Test
  void blankUsername_mapsToRegistrationValidationException() {
    ValidationResult result = validator.validate("  ", "1234".toCharArray(), BigDecimal.TEN);

    RegistrationValidationException thrown = assertThrows(
        RegistrationValidationException.class,
        () -> {
          if (result instanceof ValidationResult.Failure failure) {
            throw new RegistrationValidationException(failure.error());
          }
          throw new AssertionError("expected failure");
        });

    assertEquals(ValidationError.INVALID_USERNAME, thrown.error());
  }

  @Test
  void rejectsBlankUsername() {
    ValidationResult result = validator.validate("  ", "1234".toCharArray(), BigDecimal.TEN);
    ValidationResult.Failure failure = assertInstanceOf(ValidationResult.Failure.class, result);
    assertEquals(ValidationError.INVALID_USERNAME, failure.error());
  }

  @Test
  void rejectsTooShortUsername() {
    ValidationResult result = validator.validate("ab", "1234".toCharArray(), BigDecimal.TEN);
    ValidationResult.Failure failure = assertInstanceOf(ValidationResult.Failure.class, result);
    assertEquals(ValidationError.INVALID_USERNAME, failure.error());
  }
}
