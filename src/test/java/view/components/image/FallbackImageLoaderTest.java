package view.components.image;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

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
 * Integration tests for {@link FallbackImageLoader} with filesystem-backed delegates ({@link TempDir}).
 */
class FallbackImageLoaderTest {

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
  void returnsFallbackWhenWrappedReturnsNull(@TempDir Path tempDir) throws IOException {
    Path file = tempDir.resolve("fallback.png");
    Files.write(file, ONE_PIXEL_PNG);
    Image fallback = new FileImageLoader().load(file, 8);
    assertNotNull(fallback);

    ImageLoader alwaysNull = (path, size) -> null;
    FallbackImageLoader loader = new FallbackImageLoader(alwaysNull, fallback);

    assertSame(fallback, loader.load(Path.of("/missing.png"), 40));
  }

  @Test
  void returnsWrappedResultWhenNonNull(@TempDir Path tempDir) throws IOException {
    Path primary = tempDir.resolve("primary.png");
    Files.write(primary, ONE_PIXEL_PNG);
    Image primaryImage = new FileImageLoader().load(primary, 8);
    assertNotNull(primaryImage);

    Path fallbackFile = tempDir.resolve("fallback.png");
    Files.write(fallbackFile, ONE_PIXEL_PNG);
    Image fallbackImage = new FileImageLoader().load(fallbackFile, 8);
    assertNotNull(fallbackImage);

    ImageLoader inner = (path, size) -> path.equals(primary) ? primaryImage : null;
    FallbackImageLoader loader = new FallbackImageLoader(inner, fallbackImage);

    assertSame(primaryImage, loader.load(primary, 8));
  }

  @Test
  void nullFallbackPropagatesWhenWrappedReturnsNull() {
    ImageLoader inner = (path, size) -> null;
    FallbackImageLoader loader = new FallbackImageLoader(inner, null);

    assertNull(loader.load(Path.of("/any.png"), 10));
  }
}
