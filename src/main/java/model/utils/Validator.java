package model.utils;

import java.math.BigDecimal;

/**
 * A utility class for validating objects. This serves as a centralized place for validation
 * methods, to avoid code duplication.
 *
 * @author kaamyashinde
 * @version 0.0.3
 * @since 30-01-2026
 */
public class Validator {

  private Validator() {
  }

  /**
   * Checks if an object is null.
   *
   * @param obj    The object to be checked
   * @param obType The type of the object, friendly name for error message
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
   * @param value the value to check
   * @param name  friendly name for error messages
   * @throws NullPointerException      if {@code value} is null
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
   * @param value the value to check
   * @param name  friendly name for error messages
   * @throws IllegalArgumentException if {@code value} is not positive
   */
  public static void requirePositive(int value, String name) {
    if (value <= 0) {
      throw new IllegalArgumentException(name + " must be positive");
    }
  }

  /**
   * @param value a big decimal, possibly null
   * @return {@code true} if {@code value} is non-null and strictly greater than zero
   */
  public static boolean isStrictlyPositive(BigDecimal value) {
    return value != null && value.signum() > 0;
  }

}
