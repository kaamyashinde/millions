package model.persistence.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;
import javax.imageio.ImageIO;
import model.exception.persistence.PersistenceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ProfileImageServiceTest {

  @TempDir
  Path tempDir;

  @Test
  void saveAvatarFromFile_rejectsNullAndMissingFiles() {
    ProfileImageService service = new ProfileImageService(new ProfilePaths(tempDir));

    assertThrows(
        IllegalArgumentException.class,
        () -> service.saveAvatarFromFile(null, "alice"));
    assertThrows(
        IllegalArgumentException.class,
        () -> service.saveAvatarFromFile(tempDir.resolve("missing.png"), "alice"));
  }

  @Test
  void saveAvatarFromFile_rejectsOversizedFilesAndCorruptImages() throws Exception {
    ProfileImageService service = new ProfileImageService(new ProfilePaths(tempDir));
    Path large = tempDir.resolve("large.png");
    try (OutputStream output = Files.newOutputStream(large)) {
      output.write(new byte[(int) ProfileImageService.MAX_FILE_BYTES + 1]);
    }
    Path corrupt = tempDir.resolve("corrupt.png");
    Files.writeString(corrupt, "not an image");

    assertEquals(
        "Image must be at most 2 MB.",
        assertThrows(
            IllegalArgumentException.class,
            () -> service.saveAvatarFromFile(large, "alice")).getMessage());
    assertEquals(
        "Unsupported or corrupt image (use PNG or JPEG).",
        assertThrows(
            IllegalArgumentException.class,
            () -> service.saveAvatarFromFile(corrupt, "alice")).getMessage());
  }

  @Test
  void saveAvatarFromFile_wrapsUnreadableImageData() throws Exception {
    ProfileImageService service = new ProfileImageService(new ProfilePaths(tempDir));
    Path unreadable = tempDir.resolve("unreadable.png");
    writeImage(unreadable, "png", 16, 16, BufferedImage.TYPE_INT_ARGB);
    Set<PosixFilePermission> originalPermissions = Files.getPosixFilePermissions(unreadable);
    Files.setPosixFilePermissions(unreadable, Set.of());
    try {
      assertEquals(
          "Could not read image data.",
          assertThrows(
              IllegalArgumentException.class,
              () -> service.saveAvatarFromFile(unreadable, "alice")).getMessage());
    } finally {
      Files.setPosixFilePermissions(unreadable, originalPermissions);
    }
  }

  @Test
  void saveAvatarFromFile_writesSmallPngAndDeletesIt() throws Exception {
    ProfileImageService service = new ProfileImageService(new ProfilePaths(tempDir));
    Path source = tempDir.resolve("avatar.png");
    writeImage(source, "png", 32, 24, BufferedImage.TYPE_INT_ARGB);

    service.saveAvatarFromFile(source, "Alice");
    Path avatar = service.avatarPath("alice");

    assertTrue(Files.isRegularFile(avatar));
    assertEquals(avatar, tempDir.resolve("alice").resolve("avatar.png"));

    service.deleteAvatar("alice");
    assertFalse(Files.exists(avatar));
    service.deleteAvatar("alice");
  }

  @Test
  void saveAvatarFromFile_convertsJpegAndScalesLargeImages() throws Exception {
    ProfileImageService service = new ProfileImageService(new ProfilePaths(tempDir));
    Path jpeg = tempDir.resolve("avatar.jpg");
    writeImage(jpeg, "jpg", 1024, 256, BufferedImage.TYPE_INT_RGB);

    service.saveAvatarFromFile(jpeg, "alice");

    BufferedImage saved = ImageIO.read(service.avatarPath("alice").toFile());
    assertEquals(ProfileImageService.MAX_DIMENSION, saved.getWidth());
    assertEquals(128, saved.getHeight());
  }

  @Test
  void saveAvatarFromFile_wrapsWriteFailures() throws Exception {
    ProfileImageService service = new ProfileImageService(new ProfilePaths(tempDir));
    Path source = tempDir.resolve("avatar.png");
    writeImage(source, "png", 16, 16, BufferedImage.TYPE_INT_ARGB);
    Path avatarPath = service.avatarPath("alice");
    Files.createDirectories(avatarPath);
    Files.writeString(avatarPath.resolve("child"), "blocks image output");

    assertThrows(PersistenceException.class, () -> service.saveAvatarFromFile(source, "alice"));
  }

  @Test
  void deleteAvatar_wrapsIoFailures() throws Exception {
    ProfileImageService service = new ProfileImageService(new ProfilePaths(tempDir));
    Path avatarPath = service.avatarPath("alice");
    Files.createDirectories(avatarPath);
    Files.writeString(avatarPath.resolve("child"), "keeps directory non-empty");

    assertThrows(PersistenceException.class, () -> service.deleteAvatar("alice"));
  }

  @Test
  void toArgb_returnsArgbSourcesUnchanged() throws Exception {
    BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
    Method method = ProfileImageService.class.getDeclaredMethod("toArgb", BufferedImage.class);
    method.setAccessible(true);

    Object result = method.invoke(null, image);

    assertEquals(image, result);
  }

  private static void writeImage(
      Path path,
      String format,
      int width,
      int height,
      int imageType) throws Exception {
    BufferedImage image = new BufferedImage(width, height, imageType);
    Graphics2D graphics = image.createGraphics();
    try {
      graphics.setColor(Color.BLUE);
      graphics.fillRect(0, 0, width, height);
    } finally {
      graphics.dispose();
    }
    ImageIO.write(image, format, path.toFile());
  }
}
