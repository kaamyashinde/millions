package model.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UserAccountRepositoryTest {

  @TempDir
  Path tempDir;

  @Test
  void listUsernames_ignoresUnknownFieldsInOlderAccountJson() throws IOException {
    Path profileDir = tempDir.resolve("kaamya");
    Files.createDirectories(profileDir);
    Files.writeString(
        profileDir.resolve("account.json"),
        """
        {
          "username": "Kaamya",
          "normalizedUsername": "kaamya",
          "saltBase64": "salt",
          "pinHashBase64": "hash",
          "displayName": "Kaamya Shinde"
        }
        """);

    UserAccountRepository repository = new UserAccountRepository(tempDir);

    assertEquals(List.of("Kaamya"), repository.listUsernames());
    assertTrue(repository.findByUsername("kaamya").isPresent());
    assertEquals("Kaamya", repository.findByUsername("kaamya").orElseThrow().username());
  }
}
