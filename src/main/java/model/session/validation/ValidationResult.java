package model.session.validation;

/**
 * Outcome of a registration validation step or chain.
 *
 * <p>Either all checks passed ({@link Success}) or the first failure is reported ({@link Failure}).
 */
public sealed interface ValidationResult {

  /**
   * All validators in the chain accepted the input.
   */
  record Success() implements ValidationResult {}

  /**
   * A validator rejected the input.
   *
   * @param error typed reason for rejection
   */
  record Failure(ValidationError error) implements ValidationResult {}
}
