package model.exception.trading;


/**
 * An exception thrown when a player attempts to commit a transaction that has already been
 * committed.
 *
 * @author kevindmazali
 * @version 0.0.1
 * @since 2026-02-07
 */
public class AlreadyCommittedException extends RuntimeException {

  /**
   * Constructs a new AlreadyCommittedException with a default error message indicating that the
   */
  public AlreadyCommittedException() {
    super("This purchase is already commited. A transaction cannot be commited twice.");
  }
}
