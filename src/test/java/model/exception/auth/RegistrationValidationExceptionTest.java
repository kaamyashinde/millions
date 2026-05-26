package model.exception.auth;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import model.session.validation.ValidationResult;
import model.session.validation.rules.UsernameValidator;

import model.session.validation.ValidationError;
import org.junit.jupiter.api.Test;

class RegistrationValidationExceptionTest {

  @Test
  void thrownFromValidatorChain_carriesInvalidUsernameError() {
    ValidationResult result =
        new UsernameValidator().validate("x", "1234".toCharArray(), java.math.BigDecimal.TEN);

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
  void carriesValidationErrorAndMessage() {
    RegistrationValidationException ex =
        new RegistrationValidationException(ValidationError.INVALID_PIN);
    assertEquals(ValidationError.INVALID_PIN, ex.error());
    assertEquals(ValidationError.INVALID_PIN.defaultMessage(), ex.getMessage());
  }
}
