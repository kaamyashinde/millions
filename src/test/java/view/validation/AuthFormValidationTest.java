package view.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class AuthFormValidationTest {

  @Test
  void acceptsValidUsername() {
    assertTrue(AuthFormValidation.usernameError("valid_user").isEmpty());
  }

  @Test
  void rejectsTooShortUsername() {
    Optional<String> error = AuthFormValidation.usernameError("ab");
    assertTrue(error.isPresent());
    assertEquals(
        "Username must be 3-32 characters (letters, numbers, _ or -).",
        error.get());
  }

  @Test
  void acceptsValidPin() {
    assertTrue(AuthFormValidation.pinError("1234").isEmpty());
  }

  @Test
  void rejectsLettersInPin() {
    Optional<String> error = AuthFormValidation.pinError("12ab");
    assertTrue(error.isPresent());
    assertEquals("PIN must be 4 to 8 digits.", error.get());
  }

  @Test
  void rejectsTooShortPin() {
    Optional<String> error = AuthFormValidation.pinError("123");
    assertTrue(error.isPresent());
    assertEquals("PIN must be 4 to 8 digits.", error.get());
  }

  @Test
  void acceptsValidStartingMoney() {
    assertTrue(AuthFormValidation.startingMoneyError("1000").isEmpty());
    assertTrue(AuthFormValidation.startingMoneyError("0").isEmpty());
  }

  @Test
  void rejectsNegativeStartingMoney() {
    Optional<String> error = AuthFormValidation.startingMoneyError("-50");
    assertTrue(error.isPresent());
    assertEquals("Starting money must be non-negative.", error.get());
  }

  @Test
  void rejectsNonNumericStartingMoney() {
    Optional<String> error = AuthFormValidation.startingMoneyError("not-a-number");
    assertTrue(error.isPresent());
    assertEquals("Starting money must be a valid number.", error.get());
  }
}
