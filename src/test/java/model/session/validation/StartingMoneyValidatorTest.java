package model.session.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import java.math.BigDecimal;
import model.utils.StartingMoneyValidator;
import model.utils.ValidationError;
import model.utils.ValidationResult;
import org.junit.jupiter.api.Test;

class StartingMoneyValidatorTest {

  private final StartingMoneyValidator validator = new StartingMoneyValidator();

  @Test
  void acceptsZeroAndPositive() {
    assertInstanceOf(
        ValidationResult.Success.class,
        validator.validate("u", "1234".toCharArray(), BigDecimal.ZERO));
    assertInstanceOf(
        ValidationResult.Success.class,
        validator.validate("u", "1234".toCharArray(), new BigDecimal("0.01")));
  }

  @Test
  void rejectsNull() {
    ValidationResult r = validator.validate("u", "1234".toCharArray(), null);
    ValidationResult.Failure f = assertInstanceOf(ValidationResult.Failure.class, r);
    assertEquals(ValidationError.NEGATIVE_STARTING_MONEY, f.error());
  }

  @Test
  void rejectsNegative() {
    ValidationResult r =
        validator.validate("u", "1234".toCharArray(), new BigDecimal("-0.01"));
    ValidationResult.Failure f = assertInstanceOf(ValidationResult.Failure.class, r);
    assertEquals(ValidationError.NEGATIVE_STARTING_MONEY, f.error());
  }
}
