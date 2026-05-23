package util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ExternalLinkOpenerTest {

  @Test
  void open_nullUrlDoesNotThrow() {
    assertDoesNotThrow(() -> ExternalLinkOpener.open(null));
  }

  @Test
  void open_blankUrlDoesNotThrow() {
    assertDoesNotThrow(() -> ExternalLinkOpener.open("   "));
  }

  @Test
  void open_malformedUrlDoesNotThrow() {
    assertDoesNotThrow(() -> ExternalLinkOpener.open("not-a-valid-uri"));
  }

  @Test
  void open_validUrlDoesNotThrow() {
    assertDoesNotThrow(() -> ExternalLinkOpener.open("https://example.com"));
  }
}
