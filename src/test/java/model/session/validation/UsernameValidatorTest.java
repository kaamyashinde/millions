package model.session.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import java.math.BigDecimal;
import model.utils.UsernameValidator;
import model.utils.ValidationError;
import model.utils.ValidationResult;
import org.junit.jupiter.api.Test;

class UsernameValidatorTest {

  private final UsernameValidator validator = new UsernameValidator();

  @Test
  void acceptsValidUsername() {
    ValidationResult result =
        validator.validate("valid_user", "1234".toCharArray(), BigDecimal.TEN);
    assertInstanceOf(ValidationResult.Success.class, result);
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
