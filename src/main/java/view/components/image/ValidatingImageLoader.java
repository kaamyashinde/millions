package view.components.image;

import java.nio.file.Files;
import java.nio.file.Path;
import javafx.scene.image.Image;

/**
 * Decorator that skips loading when the path is {@code null} or not a regular file.
 */
public final class ValidatingImageLoader extends ImageLoaderDecorator {

  /**
   * Wraps the given loader; only valid paths are passed through.
   *
   * @param wrapped loader invoked when the path is valid
   */
  public ValidatingImageLoader(ImageLoader wrapped) {
    super(wrapped);
  }

  /**
   * Returns {@code null} when {@code imagePath} is {@code null} or not a regular file; otherwise
   * delegates to the wrapped loader.
   */
  @Override
  public Image load(Path imagePath, double size) {
    if (imagePath == null || !Files.isRegularFile(imagePath)) {
      return null;
    }
    return wrapped.load(imagePath, size);
  }
}
