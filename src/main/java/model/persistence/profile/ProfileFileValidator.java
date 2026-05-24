package model.persistence.profile;

import java.math.BigDecimal;
import java.util.List;
import model.persistence.ProfileFile;

/**
 * Validates profile JSON structure after deserialization and before domain restore.
 */
public final class ProfileFileValidator {

  private ProfileFileValidator() {
  }

  /**
   * Validates a deserialized profile document.
   *
   * @param profile profile read from disk
   * @throws IllegalArgumentException when required fields or cross-field rules fail
   */
  public static void validate(ProfileFile profile) {
    requireText(profile.username(), "username");
    requireText(profile.normalizedUsername(), "normalizedUsername");
    requireText(profile.pinHash(), "pinHash");
    requireText(profile.playerName(), "playerName");
    requireText(profile.exchangeName(), "exchangeName");
    requireNonNull(profile.startingMoney(), "startingMoney");
    requireNonNull(profile.cash(), "cash");
    if (profile.day() < 1) {
      throw new IllegalArgumentException("day must be at least 1.");
    }
    if (!ProfilePaths.normalizeUsername(profile.username()).equals(profile.normalizedUsername())) {
      throw new IllegalArgumentException(
          "normalizedUsername does not match username: " + profile.username());
    }
    requireNonNullList(profile.holdings(), "holdings");
    requireNonNullList(profile.transactions(), "transactions");
    requireNonNullList(profile.savings(), "savings");
    requireNonNullList(profile.stockPrices(), "stockPrices");
    requireNonNullList(profile.events(), "events");
    rejectNullEntries(profile.holdings(), "holdings");
    rejectNullEntries(profile.transactions(), "transactions");
    rejectNullEntries(profile.savings(), "savings");
    rejectNullEntries(profile.stockPrices(), "stockPrices");
    rejectNullEntries(profile.events(), "events");
  }

  private static void requireText(String value, String field) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Missing or blank " + field + ".");
    }
  }

  private static void requireNonNull(BigDecimal value, String field) {
    if (value == null) {
      throw new IllegalArgumentException("Missing " + field + ".");
    }
  }

  private static void requireNonNullList(List<?> list, String field) {
    if (list == null) {
      throw new IllegalArgumentException("Missing " + field + " list.");
    }
  }

  private static void rejectNullEntries(List<?> list, String field) {
    for (Object entry : list) {
      if (entry == null) {
        throw new IllegalArgumentException("Null entry in " + field + " list.");
      }
    }
  }
}
