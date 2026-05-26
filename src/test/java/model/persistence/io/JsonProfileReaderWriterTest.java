package model.persistence.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import model.exception.persistence.PersistenceException;
import model.persistence.ProfileFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * {@link JsonProfileReader#readJson(String)} covers parsing and validation without disk I/O.
 * {@link TempDir} is used only for round-trip and filesystem error paths.
 */
class JsonProfileReaderWriterTest {

  @TempDir
  Path tempDir;

  @Test
  void writerAndReader_roundTripValidProfile() {
    Path path = tempDir.resolve("profiles").resolve("alice").resolve("profile.json");
    ProfileFile profile = validProfile();

    new JsonProfileWriter().write(path, profile);
    ProfileFile loaded = new JsonProfileReader().read(path);

    assertTrue(Files.isRegularFile(path));
    assertEquals("Alice", loaded.username());
    assertEquals("alice", loaded.normalizedUsername());
  }

  @Test
  void readJson_malformedSyntax_throwsPersistenceException() {
    PersistenceException thrown = assertThrows(
        PersistenceException.class,
        () -> new JsonProfileReader().readJson("{ not json"));

    assertTrue(thrown.getMessage().contains("Could not read JSON content"));
  }

  @Test
  void readJson_mismatchedNormalizedUsername_throwsPersistenceException() {
    String json = """
        {
          "username": "Alice",
          "normalizedUsername": "bob",
          "pinHash": "hash",
          "playerName": "Alice",
          "startingMoney": 10,
          "cash": 10,
          "exchangeName": "NYSE",
          "day": 1
        }
        """;

    PersistenceException thrown = assertThrows(
        PersistenceException.class,
        () -> new JsonProfileReader().readJson(json));

    assertTrue(thrown.getMessage().startsWith("Invalid profile file:"));
  }

  @Test
  void readJson_dayBelowOne_throwsPersistenceException() {
    String json = """
        {
          "username": "Alice",
          "normalizedUsername": "alice",
          "pinHash": "hash",
          "playerName": "Alice",
          "startingMoney": 10,
          "cash": 10,
          "exchangeName": "NYSE",
          "day": 0
        }
        """;

    PersistenceException thrown = assertThrows(
        PersistenceException.class,
        () -> new JsonProfileReader().readJson(json));

    assertTrue(thrown.getMessage().contains("day must be at least 1"));
  }

  @Test
  void readJson_missingRequiredField_throwsPersistenceException() {
    String json = """
        {
          "normalizedUsername": "alice",
          "pinHash": "hash",
          "playerName": "Alice",
          "startingMoney": 10,
          "cash": 10,
          "exchangeName": "NYSE",
          "day": 1
        }
        """;

    PersistenceException thrown = assertThrows(
        PersistenceException.class,
        () -> new JsonProfileReader().readJson(json));

    assertTrue(thrown.getMessage().contains("Invalid profile file"));
  }

  @Test
  void reader_wrapsIoFailures() {
    Path missing = tempDir.resolve("missing.json");

    PersistenceException thrown = assertThrows(
        PersistenceException.class,
        () -> new JsonProfileReader().read(missing));

    assertTrue(thrown.getMessage().contains("Could not read JSON file"));
  }

  @Test
  void writer_wrapsIoFailures() throws Exception {
    Path directoryAsTarget = tempDir.resolve("profile-dir");
    Files.createDirectories(directoryAsTarget);

    PersistenceException thrown = assertThrows(
        PersistenceException.class,
        () -> new JsonProfileWriter().write(directoryAsTarget, validProfile()));

    assertTrue(thrown.getMessage().contains("Could not write JSON file"));
  }

  @Test
  void jsonStorage_wrapsWriteFailures() throws Exception {
    Path directoryAsTarget = tempDir.resolve("storage-dir");
    Files.createDirectories(directoryAsTarget);

    PersistenceException thrown = assertThrows(
        PersistenceException.class,
        () -> new JsonStorage().write(directoryAsTarget, validProfile()));

    assertTrue(thrown.getMessage().contains("Could not write JSON file"));
  }

  private static ProfileFile validProfile() {
    return new ProfileFile(
        "Alice",
        "alice",
        "hash",
        null,
        false,
        "Alice",
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
