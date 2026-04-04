package model.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SavedRunRepositoryTest {

  @TempDir
  Path tempDir;

  @Test
  void saveListGet_roundTripAndSortBySavedAtDesc() {
    SavedRunRepository repository = new SavedRunRepository(tempDir);
    UUID older = UUID.fromString("00000000-0000-4000-8000-000000000001");
    UUID newer = UUID.fromString("00000000-0000-4000-8000-000000000002");

    SavedRunRecord first = sampleRun(older, "2026-04-01T10:00:00Z", "first");
    SavedRunRecord second = sampleRun(newer, "2026-04-04T12:00:00Z", "second");

    repository.save("alice", first);
    repository.save("alice", second);

    List<SavedRunRecord> list = repository.list("alice");
    assertEquals(2, list.size());
    assertEquals("second", list.getFirst().label());
    assertEquals("first", list.get(1).label());

    assertEquals(Optional.of(second), repository.get("alice", newer));
  }

  @Test
  void delete_removesFile() {
    SavedRunRepository repository = new SavedRunRepository(tempDir);
    UUID id = UUID.randomUUID();
    repository.save("bob", sampleRun(id, "2026-04-01T10:00:00Z", "x"));
    assertTrue(repository.delete("bob", id));
    assertTrue(repository.list("bob").isEmpty());
    assertFalse(repository.delete("bob", id));
  }

  @Test
  void updateLeaderboardFlag_preservesOtherFields() {
    SavedRunRepository repository = new SavedRunRepository(tempDir);
    UUID id = UUID.randomUUID();
    SavedRunRecord original = sampleRun(id, "2026-04-01T10:00:00Z", "run");
    repository.save("carol", original);
    assertTrue(repository.updateLeaderboardFlag("carol", id, true));
    SavedRunRecord updated = repository.get("carol", id).orElseThrow();
    assertTrue(updated.eligibleForLeaderboard());
    assertEquals(original.label(), updated.label());
    assertEquals(original.stats().netWorth(), updated.stats().netWorth());
  }

  @Test
  void list_isolatedPerUser() {
    SavedRunRepository repository = new SavedRunRepository(tempDir);
    UUID id = UUID.randomUUID();
    repository.save("user1", sampleRun(id, "2026-04-01T10:00:00Z", "a"));
    assertEquals(1, repository.list("user1").size());
    assertTrue(repository.list("user2").isEmpty());
  }

  private static SavedRunRecord sampleRun(UUID runId, String savedAt, String label) {
    return new SavedRunRecord(
        SavedRunRecord.CURRENT_SCHEMA_VERSION,
        runId.toString(),
        savedAt,
        label,
        5,
        new BigDecimal("100.00"),
        List.of(
            new SavedRunRecord.SavedRunHoldingSnapshot(
                "AAPL",
                "Apple",
                new BigDecimal("2"),
                new BigDecimal("150"),
                new BigDecimal("290.00"))),
        new SavedRunRecord.SavedRunStatsSnapshot(
            new BigDecimal("500.00"),
            new BigDecimal("1000.00"),
            "NOVICE",
            new BigDecimal("0.05"),
            new BigDecimal("0.1"),
            new BigDecimal("1.2"),
            new BigDecimal("0.04"),
            new BigDecimal("0.09"),
            new BigDecimal("1.0")),
        false);
  }
}
