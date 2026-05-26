package util;

import java.awt.Desktop;
import java.net.URI;
import java.util.Optional;

/**
 * Opens external URLs in the system browser when supported.
 */
public final class ExternalLinkOpener {

  private ExternalLinkOpener() {}

  /**
   * Parses {@code url} into a URI suitable for {@link Desktop#browse(URI)}.
   *
   * @param url candidate URL string
   * @return parsed http/https URI, or empty when null, blank, or not browsable
   */
  static Optional<URI> toBrowsableUri(String url) {
    if (url == null || url.isBlank()) {
      return Optional.empty();
    }
    try {
      URI uri = URI.create(url.trim());
      String scheme = uri.getScheme();
      if (scheme == null) {
        return Optional.empty();
      }
      if (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https")) {
        return Optional.empty();
      }
      return Optional.of(uri);
    } catch (Exception ignored) {
      return Optional.empty();
    }
  }

  /**
   * Opens {@code url} in the default browser. No-op for null or blank URLs.
   * Fails silently when desktop browsing is unavailable.
   *
   * @param url fully-qualified URL
   */
  public static void open(String url) {
    toBrowsableUri(url).ifPresent(uri -> {
      try {
        if (Desktop.isDesktopSupported()
            && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
          Desktop.getDesktop().browse(uri);
        }
      } catch (Exception ignored) {
        // Silent fail — desktop browsing may be unavailable in some environments
      }
    });
  }
}
