package model.utils;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ValidatorTest {

  @Test
  void requirePositiveBigDecimal_nullThrowsNpe() {
    assertThrows(NullPointerException.class,
        () -> Validator.requirePositive(null, "x"));
  }

  @Test
  void requirePositiveBigDecimal_nonPositiveThrowsIae() {
    IllegalArgumentException z =
        assertThrows(IllegalArgumentException.class,
            () -> Validator.requirePositive(BigDecimal.ZERO, "budget"));
    assertTrue(z.getMessage().contains("budget"));
    assertTrue(z.getMessage().contains("positive"));

    IllegalArgumentException neg =
        assertThrows(IllegalArgumentException.class,
            () -> Validator.requirePositive(new BigDecimal("-1"), "budget"));
    assertTrue(neg.getMessage().contains("budget"));
  }

  @Test
  void requirePositiveBigDecimal_acceptsPositive() {
    Validator.requirePositive(BigDecimal.ONE, "v");
    Validator.requirePositive(new BigDecimal("0.00000001"), "v");
  }

  @Test
  void requirePositiveInt_nonPositiveThrowsIae() {
    assertThrows(IllegalArgumentException.class,
        () -> Validator.requirePositive(0, "intervalDays"));
    assertThrows(IllegalArgumentException.class,
        () -> Validator.requirePositive(-3, "intervalDays"));
  }

  @Test
  void requirePositiveInt_acceptsPositive() {
    Validator.requirePositive(1, "n");
    Validator.requirePositive(Integer.MAX_VALUE, "n");
  }

  @Test
  void isStrictlyPositive_falseForNullOrNonPositive() {
    assertFalse(Validator.isStrictlyPositive(null));
    assertFalse(Validator.isStrictlyPositive(BigDecimal.ZERO));
    assertFalse(Validator.isStrictlyPositive(new BigDecimal("-1")));
  }

  @Test
  void isStrictlyPositive_trueForPositive() {
    assertTrue(Validator.isStrictlyPositive(BigDecimal.ONE));
    assertTrue(Validator.isStrictlyPositive(new BigDecimal("0.00000001")));
  }
}
