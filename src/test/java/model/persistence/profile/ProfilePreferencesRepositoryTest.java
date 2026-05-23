package model.persistence.profile;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ProfilePreferencesRepositoryTest {

  @TempDir
  Path tempDir;

  @Test
  void load_missingFile_returnsInitial() {
    ProfilePreferencesRepository repository = new ProfilePreferencesRepository(tempDir);
    ProfilePreferences prefs = repository.load("alice");
    assertFalse(prefs.hasSeenWelcome());
  }

  @Test
  void saveAndLoad_roundTrip() {
    ProfilePreferencesRepository repository = new ProfilePreferencesRepository(tempDir);
    repository.save("alice", new ProfilePreferences(true));
    assertTrue(repository.load("alice").hasSeenWelcome());
  }
}
