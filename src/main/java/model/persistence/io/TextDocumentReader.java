package model.persistence.io;

import java.nio.file.Path;

/**
 * Reads a typed document from a text file at an arbitrary path.
 *
 * @param <T> deserialized document type
 * @param <E> checked exception thrown on failure
 */
@FunctionalInterface
public interface TextDocumentReader<T, E extends Exception> {

  /**
   * Reads and parses a document from disk.
   *
   * @param path source file path
   * @return parsed document
   * @throws E when the file cannot be read or parsed
   */
  T read(Path path) throws E;
}
