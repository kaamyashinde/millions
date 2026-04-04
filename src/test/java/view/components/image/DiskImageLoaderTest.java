package view.components.image;

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
 * Tests {@link DiskImageLoader}. Requires JavaFX for {@link javafx.scene.image.Image}.
 */
class DiskImageLoaderTest {

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
  void nullPathReturnsNull() {
    DiskImageLoader loader = new DiskImageLoader();
    assertNull(loader.load(null, 40));
  }

  @Test
  void missingOrNonRegularPathReturnsNull() {
    DiskImageLoader loader = new DiskImageLoader();
    assertNull(loader.load(Path.of("/no/such/avatar/file.png"), 40));
  }

  @Test
  void loadValidPngReturnsImage(@TempDir Path tempDir) throws IOException {
    Path file = tempDir.resolve("avatar.png");
    Files.write(file, ONE_PIXEL_PNG);

    DiskImageLoader loader = new DiskImageLoader();
    Image image = loader.load(file, 32);
    assertNotNull(image);
  }
}
