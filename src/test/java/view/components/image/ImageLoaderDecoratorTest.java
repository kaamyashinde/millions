package view.components.image;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.scene.image.Image;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Integration tests for {@link ImageLoaderDecorator} with filesystem-backed loaders ({@link TempDir}).
 */
class ImageLoaderDecoratorTest {

  private static final byte[] ONE_PIXEL_PNG = Base64.getDecoder().decode(
      "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==");

  @BeforeAll
  static void initJavaFx() throws InterruptedException {
    try {
      CountDownLatch latch = new CountDownLatch(1);
      Platform.startup(latch::countDown);
      latch.await(5, TimeUnit.SECONDS);
    } catch (IllegalStateException ignored) {
      // toolkit already running
    }
  }

  @Test
  void constructorRejectsNullWrapped() {
    assertThrows(NullPointerException.class, () -> new PassthroughDecorator(null));
  }

  @Test
  void validatingWithPassthroughDecoratorLoadsFile(@TempDir Path tempDir) throws IOException {
    Path file = tempDir.resolve("chain.png");
    Files.write(file, ONE_PIXEL_PNG);

    ImageLoader chain =
        new ValidatingImageLoader(new PassthroughDecorator(new FileImageLoader()));

    Image image = chain.load(file, 16);
    assertNotNull(image);
  }

  /**
   * Decorator that forwards every call to the wrapped loader unchanged.
   */
  private static final class PassthroughDecorator extends ImageLoaderDecorator {

    PassthroughDecorator(ImageLoader wrapped) {
      super(wrapped);
    }

    @Override
    public Image load(Path imagePath, double size) {
      return wrapped.load(imagePath, size);
    }
  }
}
