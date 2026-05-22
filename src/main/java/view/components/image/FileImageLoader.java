package view.components.image;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javafx.scene.image.Image;

/**
 * Concrete component that reads an image file into a JavaFX {@link Image}.
 *
 * <p>Does not validate the path; pair with {@link ValidatingImageLoader} for null or missing files.
 * On I/O failure, returns {@code null}.
 */
public final class FileImageLoader implements ImageLoader {

  /**
   * Opens the path and builds a square, smoothed {@link Image}, or returns {@code null} on error.
   *
   * @param imagePath file to read (caller should ensure it exists)
   * @param size width and height for the decoded image
   * @return loaded image or {@code null} if reading fails
   */
  @Override
  public Image load(Path imagePath, double size) {
    try (InputStream in = Files.newInputStream(imagePath)) {
      return new Image(in, size, size, true, true);
    } catch (IOException exception) {
      return null;
    }
  }
}
