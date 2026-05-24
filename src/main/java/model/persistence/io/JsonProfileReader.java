package model.persistence.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import model.exception.persistence.PersistenceException;
import model.persistence.ProfileFile;
import model.persistence.profile.ProfileFileValidator;
import util.IoLogger;

/**
 * Reads profile JSON documents from disk with post-read validation.
 */
public final class JsonProfileReader implements TextDocumentReader<ProfileFile, PersistenceException> {

  private final ObjectMapper objectMapper;

  /**
   * Creates a reader with stable Jackson defaults for profile files.
   */
  public JsonProfileReader() {
    this.objectMapper = new ObjectMapper();
  }

  @Override
  public ProfileFile read(Path path) throws PersistenceException {
    try {
      ProfileFile profile = objectMapper.readValue(path.toFile(), ProfileFile.class);
      ProfileFileValidator.validate(profile);
      return profile;
    } catch (IllegalArgumentException exception) {
      IoLogger.logFailure("Validate profile JSON", path, exception);
      throw new PersistenceException("Invalid profile file: " + exception.getMessage(), exception);
    } catch (IOException exception) {
      IoLogger.logFailure("Read profile JSON", path, exception);
      throw new PersistenceException("Could not read JSON file: " + path, exception);
    }
  }
}
