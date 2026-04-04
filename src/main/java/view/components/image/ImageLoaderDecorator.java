package view.components.image;

import java.util.Objects;

/**
 * Base class for {@link ImageLoader} decorators that wrap another loader.
 */
public abstract class ImageLoaderDecorator implements ImageLoader {

  /** The wrapped loader; subclasses delegate to it after applying extra behavior. */
  protected final ImageLoader wrapped;

  /**
   * Creates a decorator around a non-null wrapped loader.
   *
   * @param wrapped inner loader to decorate
   */
  protected ImageLoaderDecorator(ImageLoader wrapped) {
    this.wrapped = Objects.requireNonNull(wrapped, "wrapped");
  }
}
