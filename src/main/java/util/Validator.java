package util;

import java.math.BigDecimal;

/**
 * Validates common argument and value preconditions.
 */
public final class Validator {

  private Validator() {
  }

  /**
   * Checks that an object is not null.
   *
   * @param obj object to check
   * @param obType friendly name for the error message
   * @throws NullPointerException if the object is null
   */
  public static void checkNotNull(Object obj, String obType) {
    if (obj == null) {
      throw new NullPointerException(obType + " cannot be null");
    }
  }

  /**
   * Requires a non-null {@link BigDecimal} strictly greater than zero.
   *
   * @param value value to check
   * @param name friendly name for error messages
   * @throws NullPointerException if {@code value} is null
   * @throws IllegalArgumentException if {@code value} is not positive
   */
  public static void requirePositive(BigDecimal value, String name) {
    checkNotNull(value, name);
    if (value.signum() <= 0) {
      throw new IllegalArgumentException(name + " must be positive");
    }
  }

  /**
   * Requires a strictly positive integer.
   *
   * @param value value to check
   * @param name friendly name for error messages
   * @throws IllegalArgumentException if {@code value} is not positive
   */
  public static void requirePositive(int value, String name) {
    if (value <= 0) {
      throw new IllegalArgumentException(name + " must be positive");
    }
  }

  /**
   * Checks whether a decimal value is non-null and strictly positive.
   *
   * @param value value to check
   * @return {@code true} if {@code value} is non-null and strictly greater than zero
   */
  public static boolean isStrictlyPositive(BigDecimal value) {
    return value != null && value.signum() > 0;
  }
}
