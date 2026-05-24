package util;


import static org.junit.jupiter.api.Assertions.assertEquals;
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

  @Test
  void parsePositiveInt_blankThrows() {
    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> Validator.parsePositiveInt("  ", "trading days", 30));
    assertTrue(ex.getMessage().contains("whole number"));
  }

  @Test
  void parsePositiveInt_nonNumericThrows() {
    assertThrows(
        IllegalArgumentException.class,
        () -> Validator.parsePositiveInt("abc", "trading days", 30));
  }

  @Test
  void parsePositiveInt_zeroOrNegativeThrows() {
    assertThrows(
        IllegalArgumentException.class,
        () -> Validator.parsePositiveInt("0", "trading days", 30));
    assertThrows(
        IllegalArgumentException.class,
        () -> Validator.parsePositiveInt("-1", "trading days", 30));
  }

  @Test
  void parsePositiveInt_acceptsOneThroughMax() {
    assertEquals(1, Validator.parsePositiveInt("1", "trading days", 30));
    assertEquals(30, Validator.parsePositiveInt("30", "trading days", 30));
    assertEquals(5, Validator.parsePositiveInt("  5  ", "trading days", 30));
  }

  @Test
  void parsePositiveInt_aboveMaxThrows() {
    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> Validator.parsePositiveInt("31", "trading days", 30));
    assertTrue(ex.getMessage().contains("must not exceed 30"));
  }
}
