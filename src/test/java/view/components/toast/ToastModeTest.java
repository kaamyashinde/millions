package view.components.toast;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import old_view.components.toast.ToastMode;

/**
 * Tests for ToastMode enum. No JavaFX toolkit initialization required as these tests only cover
 * getColorHex() and getSymbol().
 */
class ToastModeTest {

  @Test
  void enumHasFourConstants() {
    assertEquals(4, ToastMode.values().length);
  }

  @Test
  void valueOfError() {
    assertEquals(ToastMode.ERROR, ToastMode.valueOf("ERROR"));
  }

  @Test
  void valueOfWarning() {
    assertEquals(ToastMode.WARNING, ToastMode.valueOf("WARNING"));
  }

  @Test
  void valueOfInfo() {
    assertEquals(ToastMode.INFO, ToastMode.valueOf("INFO"));
  }

  @Test
  void valueOfSuccess() {
    assertEquals(ToastMode.SUCCESS, ToastMode.valueOf("SUCCESS"));
  }

  @Test
  void errorColorHex() {
    assertEquals("#FF4444", ToastMode.ERROR.getColorHex());
  }

  @Test
  void warningColorHex() {
    assertEquals("#FFA500", ToastMode.WARNING.getColorHex());
  }

  @Test
  void infoColorHex() {
    assertEquals("#2196F3", ToastMode.INFO.getColorHex());
  }

  @Test
  void successColorHex() {
    assertEquals("#4CAF50", ToastMode.SUCCESS.getColorHex());
  }

  @Test
  void errorSymbol() {
    assertEquals("i", ToastMode.ERROR.getSymbol());
  }

  @Test
  void warningSymbol() {
    assertEquals("i", ToastMode.WARNING.getSymbol());
  }

  @Test
  void infoSymbol() {
    assertEquals("i", ToastMode.INFO.getSymbol());
  }

  @Test
  void successSymbol() {
    assertEquals("\u2713", ToastMode.SUCCESS.getSymbol());
  }

  @Test
  void allColorHexesAreNonNull() {
    for (ToastMode mode : ToastMode.values()) {
      assertNotNull(mode.getColorHex());
    }
  }

  @Test
  void allSymbolsAreNonNull() {
    for (ToastMode mode : ToastMode.values()) {
      assertNotNull(mode.getSymbol());
    }
  }

  @Test
  void getColorHexDoesNotThrow() {
    for (ToastMode mode : ToastMode.values()) {
      assertDoesNotThrow(mode::getColorHex);
    }
  }

  @Test
  void getSymbolDoesNotThrow() {
    for (ToastMode mode : ToastMode.values()) {
      assertDoesNotThrow(mode::getSymbol);
    }
  }
}
