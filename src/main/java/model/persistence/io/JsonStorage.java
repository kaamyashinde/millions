package model.persistence.io;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import model.exception.persistence.PersistenceException;

/**
 * Reads and writes small JSON documents used for profile persistence.
 */
public final class JsonStorage {

  private final ObjectMapper objectMapper;

  /**
   * Creates a JSON storage helper with stable pretty-printed output.
   */
  public JsonStorage() {
    this.objectMapper = new ObjectMapper()
        .enable(SerializationFeature.INDENT_OUTPUT);
  }

  /**
   * Writes one JSON document to disk, creating parent directories when needed.
   *
   * @param path destination file path
   * @param value document value to serialize
   */
  public void write(Path path, Object value) {
    try {
      Path parent = path.getParent();
      if (parent != null) {
        Files.createDirectories(parent);
      }
      objectMapper.writeValue(path.toFile(), value);
    } catch (IOException exception) {
      throw new PersistenceException("Could not write JSON file: " + path, exception);
    }
  }

  /**
   * Reads one JSON document from disk.
   *
   * @param path source file path
   * @param type expected record/class type
   * @param <T> deserialized type
   * @return parsed document
   */
  public <T> T read(Path path, Class<T> type) {
    try {
      return objectMapper.readValue(path.toFile(), type);
    } catch (IOException exception) {
      throw new PersistenceException("Could not read JSON file: " + path, exception);
    }
  }
}
