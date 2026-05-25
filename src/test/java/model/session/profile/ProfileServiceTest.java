package model.session.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;
import model.exception.persistence.PersistenceException;
import model.persistence.io.JsonStorage;
import model.persistence.profile.ProfileImageService;
import model.persistence.profile.ProfilePaths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ProfileServiceTest {

  @TempDir
  Path tempDir;

  @Test
  void exposesImageServiceAndIgnoresMissingProfileDirectoryDeletes() {
    ProfilePaths paths = new ProfilePaths(tempDir);
    JsonStorage storage = new JsonStorage();
    ProfileImageService imageService = new ProfileImageService(paths);
    ProfileService service = new ProfileService(paths, storage, imageService);

    service.deleteProfileDirectory("missing");

    assertEquals(imageService, service.profileImageService());
  }

  @Test
  void deleteProfileDirectory_wrapsWalkFailures() throws Exception {
    ProfilePaths paths = new ProfilePaths(tempDir);
    JsonStorage storage = new JsonStorage();
    ProfileImageService imageService = new ProfileImageService(paths);
    ProfileService service = new ProfileService(paths, storage, imageService);
    Path profileDir = paths.profileDirectory("alice");
    Files.createDirectories(profileDir);
    Set<PosixFilePermission> originalPermissions = Files.getPosixFilePermissions(profileDir);
    Files.setPosixFilePermissions(profileDir, Set.of());
    try {
      assertThrows(PersistenceException.class, () -> service.deleteProfileDirectory("alice"));
    } finally {
      Files.setPosixFilePermissions(profileDir, originalPermissions);
    }
  }
}
