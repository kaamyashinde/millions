package view.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class UiFormatTest {

  @Test
  void quantity_null_returnsDash() {
    assertEquals("-", UiFormat.quantity(null));
  }

  @Test
  void quantity_wholeNumber_stripsTrailingZeros() {
    assertEquals("2", UiFormat.quantity(new BigDecimal("2")));
  }

  @Test
  void quantity_smallFraction_roundsToTwoDecimals() {
    assertEquals("0.01", UiFormat.quantity(new BigDecimal("0.0051")));
  }

  @Test
  void quantity_trailingZeros_stripped() {
    assertEquals("31.96", UiFormat.quantity(new BigDecimal("31.960000")));
  }

  @Test
  void decimal_stillUsesTwoPlaces() {
    assertEquals("70.36", UiFormat.decimal(new BigDecimal("70.364")));
  }
}
