package model.persistence.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import model.exception.persistence.PersistenceException;
import model.persistence.ProfileFile;
import util.IoLogger;

/**
 * Writes profile JSON documents to disk, creating parent directories when needed.
 */
public final class JsonProfileWriter implements TextDocumentWriter<ProfileFile, PersistenceException> {

  private final ObjectMapper objectMapper;

  /**
   * Creates a writer with pretty-printed JSON output.
   */
  public JsonProfileWriter() {
    this.objectMapper = new ObjectMapper()
        .enable(SerializationFeature.INDENT_OUTPUT);
  }

  @Override
  public void write(Path path, ProfileFile value) throws PersistenceException {
    try {
      Path parent = path.getParent();
      if (parent != null) {
        Files.createDirectories(parent);
      }
      objectMapper.writeValue(path.toFile(), value);
    } catch (IOException exception) {
      IoLogger.logFailure("Write profile JSON", path, exception);
      throw new PersistenceException("Could not write JSON file: " + path, exception);
    }
  }
}
