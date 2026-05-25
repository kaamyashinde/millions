package view.components.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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
 * Integration tests for {@link ValidatingImageLoader} using real file paths ({@link TempDir}).
 */
class ValidatingImageLoaderTest {

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
  void nullPathReturnsNullWithoutCallingWrapped() {
    CountingLoader counting = new CountingLoader();
    ValidatingImageLoader loader = new ValidatingImageLoader(counting);

    assertNull(loader.load(null, 10));
    assertEquals(0, counting.calls);
  }

  @Test
  void missingFileReturnsNullWithoutCallingWrapped() {
    CountingLoader counting = new CountingLoader();
    ValidatingImageLoader loader = new ValidatingImageLoader(counting);

    assertNull(loader.load(Path.of("/missing/avatar.png"), 10));
    assertEquals(0, counting.calls);
  }

  @Test
  void regularFileDelegatesToWrappedLoader(@TempDir Path tempDir) throws IOException {
    Path file = tempDir.resolve("a.png");
    Files.write(file, ONE_PIXEL_PNG);

    ImageLoader inner = new FileImageLoader();
    ValidatingImageLoader loader = new ValidatingImageLoader(inner);

    Image image = loader.load(file, 24);
    assertNotNull(image);
  }

  /** Counts how often {@link #load} is invoked on the delegate. */
  private static final class CountingLoader implements ImageLoader {
    private int calls;

    @Override
    public Image load(Path imagePath, double size) {
      calls++;
      return null;
    }
  }
}
