package view.components.image;

import java.nio.file.Path;
import javafx.scene.image.Image;

/**
 * Decorator that returns a fallback image when the wrapped loader yields {@code null}.
 */
public final class FallbackImageLoader extends ImageLoaderDecorator {

  private final Image fallback;

  /**
   * Wraps a loader and supplies an image when the inner load returns {@code null}.
   *
   * @param wrapped inner loader
   * @param fallback image used when {@code wrapped.load} returns {@code null}; may be {@code null}
   */
  public FallbackImageLoader(ImageLoader wrapped, Image fallback) {
    super(wrapped);
    this.fallback = fallback;
  }

  /**
   * Delegates to the wrapped loader; if the result is {@code null}, returns the configured fallback
   * (which may also be {@code null}).
   */
  @Override
  public Image load(Path imagePath, double size) {
    Image image = wrapped.load(imagePath, size);
    return image != null ? image : fallback;
  }
}
