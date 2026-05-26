package util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class I18nTest {

  private Locale savedDefault;

  @BeforeEach
  void saveLocale() {
    savedDefault = I18n.getLocale();
  }

  @AfterEach
  void restoreLocale() {
    I18n.setLocale(savedDefault);
  }

  @Test
  void get_unknownKey_returnsKeyAsFallback() {
    I18n.setLocale(Locale.ENGLISH);

    assertEquals("nonexistent.key.xyz", I18n.get("nonexistent.key.xyz"));
  }

  @Test
  void getResolvesKnownKey() {
    I18n.setLocale(Locale.ENGLISH);
    assertEquals("------------ MENU -----------", I18n.get("menu.header"));
  }

  @Test
  void formatInterpolatesArguments() {
    I18n.setLocale(Locale.ENGLISH);
    assertEquals("-> Your current balance: 100", I18n.format("balance.current", 100));
  }

  @Test
  void norwegianBundleDiffersForSameKey() {
    I18n.setLocale(Locale.ENGLISH);
    String englishMenu = I18n.get("menu.header");
    I18n.setLocale(Locale.forLanguageTag("nb"));
    String norwegianMenu = I18n.get("menu.header");
    assertTrue(englishMenu.contains("MENU"));
    assertTrue(norwegianMenu.contains("MENY"));
  }
}
