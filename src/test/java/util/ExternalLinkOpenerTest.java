package util;

import java.net.URI;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExternalLinkOpenerTest {

  @Test
  void toBrowsableUri_nullOrBlank_returnsEmpty() {
    assertTrue(ExternalLinkOpener.toBrowsableUri(null).isEmpty());
    assertTrue(ExternalLinkOpener.toBrowsableUri("   ").isEmpty());
  }

  @Test
  void toBrowsableUri_nonHttpScheme_returnsEmpty() {
    assertTrue(ExternalLinkOpener.toBrowsableUri("not-a-valid-uri").isEmpty());
    assertTrue(ExternalLinkOpener.toBrowsableUri("ftp://example.com").isEmpty());
  }

  @Test
  void toBrowsableUri_httpOrHttps_returnsUri() {
    Optional<URI> https = ExternalLinkOpener.toBrowsableUri("https://example.com");
    assertTrue(https.isPresent());
    assertEquals("https", https.get().getScheme());
    assertEquals("example.com", https.get().getHost());

    Optional<URI> http = ExternalLinkOpener.toBrowsableUri("http://foo.bar/path");
    assertTrue(http.isPresent());
    assertEquals("http", http.get().getScheme());
    assertEquals("foo.bar", http.get().getHost());
    assertEquals("/path", http.get().getPath());
  }

  @Test
  void open_nullOrBlank_doesNotThrow() {
    assertDoesNotThrow(() -> ExternalLinkOpener.open(null));
    assertDoesNotThrow(() -> ExternalLinkOpener.open("   "));
  }
}
