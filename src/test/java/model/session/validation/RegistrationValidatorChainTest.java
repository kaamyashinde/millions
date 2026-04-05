package model.session.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import java.math.BigDecimal;
import model.utils.PinValidator;
import model.utils.RegistrationValidator;
import model.utils.StartingMoneyValidator;
import model.utils.UsernameValidator;
import model.utils.ValidationError;
import model.utils.ValidationResult;
import org.junit.jupiter.api.Test;

class RegistrationValidatorChainTest {

  private final RegistrationValidator chain =
      new UsernameValidator().then(new PinValidator()).then(new StartingMoneyValidator());

  @Test
  void fullChainSucceeds() {
    ValidationResult r =
        chain.validate("good_user", "5678".toCharArray(), new BigDecimal("100"));
    assertInstanceOf(ValidationResult.Success.class, r);
  }

  @Test
  void stopsAtUsernameFailure() {
    ValidationResult r = chain.validate("x", "1234".toCharArray(), BigDecimal.TEN);
    ValidationResult.Failure f = assertInstanceOf(ValidationResult.Failure.class, r);
    assertEquals(ValidationError.INVALID_USERNAME, f.error());
  }

  @Test
  void stopsAtPinAfterUsernameOk() {
    ValidationResult r = chain.validate("good_user", "12".toCharArray(), BigDecimal.TEN);
    ValidationResult.Failure f = assertInstanceOf(ValidationResult.Failure.class, r);
    assertEquals(ValidationError.INVALID_PIN, f.error());
  }

  @Test
  void stopsAtStartingMoneyAfterUsernameAndPinOk() {
    ValidationResult r =
        chain.validate("good_user", "1234".toCharArray(), new BigDecimal("-1"));
    ValidationResult.Failure f = assertInstanceOf(ValidationResult.Failure.class, r);
    assertEquals(ValidationError.NEGATIVE_STARTING_MONEY, f.error());
  }
}
