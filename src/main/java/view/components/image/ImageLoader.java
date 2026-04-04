package view.components.image;

import java.nio.file.Path;
import javafx.scene.image.Image;

/**
 * Loads a JavaFX {@link Image} from a file path at a square display size.
 */
@FunctionalInterface
public interface ImageLoader {

  /**
   * Loads an image from disk, or returns {@code null} when loading is not possible.
   *
   * @param imagePath path to the image file; may be {@code null} depending on implementation
   * @param size width and height in pixels for the loaded image
   * @return a loaded image, or {@code null} if the path is invalid or reading fails
   */
  Image load(Path imagePath, double size);
}
