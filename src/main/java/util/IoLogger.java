package util;

import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Logs file I/O failures at persistence boundaries before exceptions are rethrown.
 */
public final class IoLogger {

  private static final Logger LOGGER = Logger.getLogger(IoLogger.class.getName());

  private IoLogger() {
  }

  /**
   * Logs a failed file operation with path context and the underlying cause.
   *
   * @param operation short description of the attempted operation
   * @param path file path involved, or {@code null}
   * @param cause exception that triggered the failure
   */
  public static void logFailure(String operation, Path path, Exception cause) {
    String pathLabel = path == null ? "n/a" : path.toString();
    LOGGER.log(
        Level.WARNING,
        "{0} failed for path {1}: {2}",
        new Object[] {operation, pathLabel, cause.toString()});
    LOGGER.log(Level.FINE, "I/O failure details", cause);
  }
}
