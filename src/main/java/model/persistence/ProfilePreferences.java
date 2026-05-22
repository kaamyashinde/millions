package model.persistence;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Non-security profile preferences stored beside account and game state.
 *
 * @param hasSeenWelcome {@code true} after the user dismissed the welcome dialog
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProfilePreferences(boolean hasSeenWelcome) {

  /** Default for missing or legacy profiles without a preferences file. */
  public static ProfilePreferences initial() {
    return new ProfilePreferences(false);
  }
}
