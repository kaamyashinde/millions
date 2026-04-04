package view.components.image;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javafx.scene.image.Image;

/**
 * Loads a JavaFX {@link Image} from a regular file on disk. Returns {@code null} when the path is
 * {@code null}, not a regular file, or when reading fails.
 */
public final class DiskImageLoader implements ImageLoader {

  @Override
  public Image load(Path imagePath, double size) {
    if (imagePath == null || !Files.isRegularFile(imagePath)) {
      return null;
    }
    try (InputStream in = Files.newInputStream(imagePath)) {
      return new Image(in, size, size, true, true);
    } catch (IOException exception) {
      return null;
    }
  }
}
