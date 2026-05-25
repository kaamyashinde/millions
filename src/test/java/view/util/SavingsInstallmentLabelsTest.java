package view.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;
import model.trading.savings.SavingsInstallmentMode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.I18n;

class SavingsInstallmentLabelsTest {

  private Locale previous;

  @BeforeEach
  void setEnglishLocale() {
    previous = I18n.getLocale();
    I18n.setLocale(Locale.ENGLISH);
  }

  @AfterEach
  void restoreLocale() {
    I18n.setLocale(previous);
  }

  @Test
  void label_mapsModesToLocalizedStrings() {
    assertEquals("Fixed shares", SavingsInstallmentLabels.label(SavingsInstallmentMode.FIXED_SHARES));
    assertEquals(
        "Investment amount", SavingsInstallmentLabels.label(SavingsInstallmentMode.BUDGET));
  }
}
