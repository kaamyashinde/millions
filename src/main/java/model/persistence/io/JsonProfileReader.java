package model.persistence.io;

import com.fasterxml.jackson.databind.ObjectMapper;
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
public final class JsonProfileReader
    implements TextDocumentReader<ProfileFile, PersistenceException> {

  private final ObjectMapper objectMapper;

  /**
   * Creates a reader with stable Jackson defaults for profile files.
   */
  public JsonProfileReader() {
    this.objectMapper = new ObjectMapper();
  }

  /**
   * Parses and validates profile JSON from an in-memory string.
   *
   * @param json UTF-8 profile document
   * @return validated profile
   * @throws PersistenceException when JSON is malformed or fails validation
   */
  public ProfileFile readJson(String json) throws PersistenceException {
    try {
      ProfileFile profile = objectMapper.readValue(json, ProfileFile.class);
      ProfileFileValidator.validate(profile);
      return profile;
    } catch (IllegalArgumentException exception) {
      throw new PersistenceException("Invalid profile file: " + exception.getMessage(), exception);
    } catch (IOException exception) {
      throw new PersistenceException(
          "Could not read JSON content: " + exception.getMessage(), exception);
    }
  }

  @Override
  public ProfileFile read(Path path) throws PersistenceException {
    try {
      return readJson(Files.readString(path));
    } catch (IOException exception) {
      IoLogger.logFailure("Read profile JSON", path, exception);
      throw new PersistenceException("Could not read JSON file: " + path, exception);
    } catch (PersistenceException exception) {
      IoLogger.logFailure("Validate profile JSON", path, exception);
      throw exception;
    }
  }
}
