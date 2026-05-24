package model.trading.savings;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link RegularSavingsPlan} construction and mutators.
 *
 * @author kevindmazali
 * @version 1.0.0
 * @since 30-03-2026
 */
class RegularSavingsPlanTest {

  @Test
  void setMode_rejectsNull() {
    RegularSavingsPlan p =
        new RegularSavingsPlan("AAPL", SavingsInstallmentMode.FIXED_SHARES, new BigDecimal("1"),
            5, 0);
    assertThrows(NullPointerException.class, () -> p.setMode(null));
  }

  @Test
  void setAmount_rejectsNonPositive() {
    RegularSavingsPlan p =
        new RegularSavingsPlan("AAPL", SavingsInstallmentMode.FIXED_SHARES, new BigDecimal("1"),
            5, 0);
    assertThrows(IllegalArgumentException.class, () -> p.setAmount(BigDecimal.ZERO));
    assertThrows(IllegalArgumentException.class, () -> p.setAmount(new BigDecimal("-1")));
  }

  @Test
  void setIntervalDays_rejectsNonPositive() {
    RegularSavingsPlan p =
        new RegularSavingsPlan("AAPL", SavingsInstallmentMode.FIXED_SHARES, new BigDecimal("1"),
            5, 0);
    assertThrows(IllegalArgumentException.class, () -> p.setIntervalDays(0));
    assertThrows(IllegalArgumentException.class, () -> p.setIntervalDays(-1));
  }

  @Test
  void setters_updateFields() {
    RegularSavingsPlan p =
        new RegularSavingsPlan("AAPL", SavingsInstallmentMode.FIXED_SHARES, new BigDecimal("1"),
            5, 10);
    p.setMode(SavingsInstallmentMode.BUDGET);
    p.setAmount(new BigDecimal("250.00"));
    p.setIntervalDays(3);
    assertEquals(SavingsInstallmentMode.BUDGET, p.getMode());
    assertEquals(new BigDecimal("250.00"), p.getAmount());
    assertEquals(3, p.getIntervalDays());
    assertEquals("AAPL", p.getSymbol());
  }

  @Test
  void setMode_amount_interval_doNotChangeNextDueDay() {
    RegularSavingsPlan p =
        new RegularSavingsPlan("AAPL", SavingsInstallmentMode.FIXED_SHARES, new BigDecimal("1"),
            5, 0);
    int due = p.getNextDueDay();
    p.setMode(SavingsInstallmentMode.BUDGET);
    p.setAmount(new BigDecimal("100"));
    p.setIntervalDays(10);
    assertEquals(due, p.getNextDueDay());
  }
}
