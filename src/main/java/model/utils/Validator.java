package model.utils;

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
   * create a static method to check if an abject is null.
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

}
