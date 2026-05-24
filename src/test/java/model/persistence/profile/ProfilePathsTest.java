package model.persistence.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.List;
import java.util.Set;
import model.exception.persistence.PersistenceException;
import model.persistence.ProfileFile;
import model.persistence.io.JsonStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ProfilePathsTest {

  @TempDir
  Path tempDir;

  @Test
  void usernameValidationAndNormalization_coverAcceptedAndRejectedInputs() {
    assertTrue(ProfilePaths.isValidUsername(" Alice_123- "));
    assertFalse(ProfilePaths.isValidUsername(null));
    assertFalse(ProfilePaths.isValidUsername("ab"));
    assertFalse(ProfilePaths.isValidUsername("bad name"));

    assertEquals("alice_123-", ProfilePaths.normalizeUsername(" Alice_123- "));
    assertThrows(IllegalArgumentException.class, () -> ProfilePaths.normalizeUsername("ab"));
  }

  @Test
  void pathMethods_resolveNormalizedProfileFiles() {
    ProfilePaths paths = new ProfilePaths(tempDir);

    assertEquals(tempDir, paths.profilesRoot());
    assertEquals(tempDir.resolve("alice"), paths.profileDirectory("Alice"));
    assertEquals(tempDir.resolve("alice").resolve("profile.json"), paths.profileFile("Alice"));
    assertEquals(tempDir.resolve("alice").resolve("avatar.png"), paths.avatarFile("Alice"));
    assertEquals(
        tempDir.resolve("alice").resolve("market-data.csv"),
        paths.marketDataFile("Alice"));
  }

  @Test
  void profileExists_tracksProfileFilePresence() throws Exception {
    ProfilePaths paths = new ProfilePaths(tempDir);
    assertFalse(paths.profileExists("Alice"));

    Files.createDirectories(paths.profileDirectory("Alice"));
    Files.writeString(paths.profileFile("Alice"), "{}");

    assertTrue(paths.profileExists("Alice"));
  }

  @Test
  void listUsernames_returnsEmptyForMissingRootAndSkipsInvalidDirectories() throws Exception {
    ProfilePaths missingRoot = new ProfilePaths(tempDir.resolve("missing"));
    assertEquals(List.of(), missingRoot.listUsernames());

    ProfilePaths paths = new ProfilePaths(tempDir);
    JsonStorage storage = new JsonStorage();
    storage.write(paths.profileFile("Bob"), validProfile("Bob", "bob"));
    Files.createDirectories(tempDir.resolve("bad name"));
    Files.writeString(tempDir.resolve("bad name").resolve("profile.json"), "{}");
    Files.createDirectories(tempDir.resolve("charlie"));

    assertEquals(List.of("bob"), paths.listUsernames());
  }

  @Test
  void listUsernames_wrapsDirectoryReadFailures() throws Exception {
    Path fileRoot = tempDir.resolve("not-a-directory");
    Files.writeString(fileRoot, "content");
    ProfilePaths paths = new ProfilePaths(fileRoot);

    assertEquals(List.of(), paths.listUsernames());
  }

  @Test
  void listUsernames_includesDirectoriesWithProfileFileRegardlessOfContent() throws Exception {
    ProfilePaths paths = new ProfilePaths(tempDir);
    Files.createDirectories(paths.profileDirectory("Alice"));
    Files.writeString(paths.profileFile("Alice"), "not-json");

    assertEquals(List.of("alice"), paths.listUsernames());
  }

  @Test
  void listUsernames_wrapsDirectoryListFailures() throws Exception {
    ProfilePaths paths = new ProfilePaths(tempDir);
    Set<PosixFilePermission> originalPermissions = Files.getPosixFilePermissions(tempDir);
    Files.setPosixFilePermissions(tempDir, Set.of());
    try {
      assertThrows(PersistenceException.class, () -> paths.listUsernames());
    } finally {
      Files.setPosixFilePermissions(tempDir, originalPermissions);
    }
  }

  private static ProfileFile validProfile(String username, String normalizedUsername) {
    return new ProfileFile(
        username,
        normalizedUsername,
        "hash",
        null,
        false,
        username,
        BigDecimal.TEN,
        BigDecimal.TEN,
        List.of(),
        List.of(),
        List.of(),
        "NYSE",
        1,
        List.of(),
        List.of(),
        null);
  }
}
