package model.persistence.profile;


import model.exception.persistence.PersistenceException;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;

/**
 * Copies and validates profile avatar images into the profile directory as PNG.
 */
public final class ProfileImageService {

  /** Maximum source file size (bytes) before decode. */
  public static final long MAX_FILE_BYTES = 2 * 1024 * 1024;

  /** Maximum width or height stored (larger images are scaled down). */
  public static final int MAX_DIMENSION = 512;

  private final ProfileDirectories profileDirectories;

  public ProfileImageService(Path profilesRoot) {
    this.profileDirectories = new ProfileDirectories(profilesRoot);
  }

  /**
   * Reads an image file, validates it, scales if needed, and writes {@code avatar.png} for the
   * profile.
   *
   * @param sourceImage path to PNG or JPEG
   * @param normalizedUsername profile key
   * @throws IllegalArgumentException if the file is missing, too large, or not a supported image
   */
  public void saveAvatarFromFile(Path sourceImage, String normalizedUsername) {
    if (sourceImage == null || !Files.isRegularFile(sourceImage)) {
      throw new IllegalArgumentException("Image file not found.");
    }
    long size;
    try {
      size = Files.size(sourceImage);
    } catch (IOException exception) {
      throw new IllegalArgumentException("Could not read image file.", exception);
    }
    if (size > MAX_FILE_BYTES) {
      throw new IllegalArgumentException("Image must be at most 2 MB.");
    }
    BufferedImage decoded;
    try {
      decoded = ImageIO.read(sourceImage.toFile());
    } catch (IOException exception) {
      throw new IllegalArgumentException("Could not read image data.", exception);
    }
    if (decoded == null) {
      throw new IllegalArgumentException("Unsupported or corrupt image (use PNG or JPEG).");
    }
    BufferedImage scaled = scaleDown(decoded);
    Path dest = profileDirectories.avatarFile(normalizedUsername);
    try {
      Files.createDirectories(dest.getParent());
      if (!ImageIO.write(scaled, "png", dest.toFile())) {
        throw new IOException("PNG encoder not available.");
      }
    } catch (IOException exception) {
      throw new PersistenceException("Could not write avatar file.", exception);
    }
  }

  /**
   * Removes the avatar file for a profile if present.
   *
   * @param normalizedUsername profile key
   */
  public void deleteAvatar(String normalizedUsername) {
    Path dest = profileDirectories.avatarFile(normalizedUsername);
    try {
      Files.deleteIfExists(dest);
    } catch (IOException exception) {
      throw new PersistenceException("Could not remove avatar file.", exception);
    }
  }

  /**
   * Returns the path to the avatar file for a profile (may not exist).
   */
  public Path avatarPath(String normalizedUsername) {
    return profileDirectories.avatarFile(normalizedUsername);
  }

  private static BufferedImage scaleDown(BufferedImage source) {
    int w = source.getWidth();
    int h = source.getHeight();
    if (w <= MAX_DIMENSION && h <= MAX_DIMENSION) {
      return toArgb(source);
    }
    double scale = Math.min((double) MAX_DIMENSION / w, (double) MAX_DIMENSION / h);
    int nw = Math.max(1, (int) Math.round(w * scale));
    int nh = Math.max(1, (int) Math.round(h * scale));
    Image scaled = source.getScaledInstance(nw, nh, Image.SCALE_SMOOTH);
    BufferedImage out = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = out.createGraphics();
    try {
      g.drawImage(scaled, 0, 0, null);
    } finally {
      g.dispose();
    }
    return out;
  }

  private static BufferedImage toArgb(BufferedImage source) {
    if (source.getType() == BufferedImage.TYPE_INT_ARGB) {
      return source;
    }
    BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = copy.createGraphics();
    try {
      g.drawImage(source, 0, 0, null);
    } finally {
      g.dispose();
    }
    return copy;
  }
}
