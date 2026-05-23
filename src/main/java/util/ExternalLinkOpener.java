package util;

import java.awt.Desktop;
import java.net.URI;

/**
 * Opens external URLs in the system browser when supported.
 */
public final class ExternalLinkOpener {

  private ExternalLinkOpener() {}

  /**
   * Opens {@code url} in the default browser. No-op for null or blank URLs.
   * Fails silently when desktop browsing is unavailable.
   *
   * @param url fully-qualified URL
   */
  public static void open(String url) {
    if (url == null || url.isBlank()) {
      return;
    }
    try {
      if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
        Desktop.getDesktop().browse(URI.create(url));
      }
    } catch (Exception ignored) {
      // Silent fail — desktop browsing may be unavailable in some environments
    }
  }
}
