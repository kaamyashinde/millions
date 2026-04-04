package model.session;

import static org.junit.jupiter.api.Assertions.assertEquals;

import model.session.validation.ValidationError;
import org.junit.jupiter.api.Test;

class RegistrationValidationExceptionTest {

  @Test
  void carriesValidationErrorAndMessage() {
    RegistrationValidationException ex =
        new RegistrationValidationException(ValidationError.INVALID_PIN);
    assertEquals(ValidationError.INVALID_PIN, ex.error());
    assertEquals(ValidationError.INVALID_PIN.defaultMessage(), ex.getMessage());
  }
}
