package model.persistence.io;

import java.nio.file.Path;

/**
 * Writes a typed document to a text file at an arbitrary path.
 *
 * @param <T> document type to serialize
 * @param <E> checked exception thrown on failure
 */
@FunctionalInterface
public interface TextDocumentWriter<T, E extends Exception> {

  /**
   * Serializes and writes a document to disk.
   *
   * @param path destination file path
   * @param value document to write
   * @throws E when the file cannot be written
   */
  void write(Path path, T value) throws E;
}
