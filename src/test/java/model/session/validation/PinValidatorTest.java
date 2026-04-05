package model.session.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import java.math.BigDecimal;
import model.utils.PinValidator;
import model.utils.ValidationError;
import model.utils.ValidationResult;
import org.junit.jupiter.api.Test;

class PinValidatorTest {

  private final PinValidator validator = new PinValidator();

  @Test
  void acceptsFourToEightDigits() {
    ValidationResult r = validator.validate("user", "12345678".toCharArray(), BigDecimal.ZERO);
    assertInstanceOf(ValidationResult.Success.class, r);
  }

  @Test
  void rejectsNullPin() {
    ValidationResult r = validator.validate("user", null, BigDecimal.ZERO);
    ValidationResult.Failure f = assertInstanceOf(ValidationResult.Failure.class, r);
    assertEquals(ValidationError.INVALID_PIN, f.error());
  }

  @Test
  void rejectsTooShortPin() {
    ValidationResult r = validator.validate("user", "123".toCharArray(), BigDecimal.ZERO);
    ValidationResult.Failure f = assertInstanceOf(ValidationResult.Failure.class, r);
    assertEquals(ValidationError.INVALID_PIN, f.error());
  }

  @Test
  void rejectsNonDigit() {
    ValidationResult r = validator.validate("user", "123a56".toCharArray(), BigDecimal.ZERO);
    ValidationResult.Failure f = assertInstanceOf(ValidationResult.Failure.class, r);
    assertEquals(ValidationError.INVALID_PIN, f.error());
  }
}
