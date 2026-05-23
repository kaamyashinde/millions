package model.session.profile;


import model.persistence.profile.ProfilePreferences;
import model.persistence.profile.ProfilePreferencesRepository;

/**
 * Manages per-profile UI preferences such as the welcome-dialog flag.
 */
public final class ProfilePreferencesService {

  private final ProfilePreferencesRepository profilePreferencesRepository;

  /**
   * Creates a preferences service backed by the supplied repository.
   *
   * @param profilePreferencesRepository per-profile preferences store
   */
  public ProfilePreferencesService(ProfilePreferencesRepository profilePreferencesRepository) {
    this.profilePreferencesRepository = profilePreferencesRepository;
  }

  /**
   * Returns whether the specified profile has already dismissed the welcome dialog.
   *
   * @param normalizedUsername profile directory key
   * @return {@code true} when welcome was previously dismissed
   */
  public boolean hasSeenWelcome(String normalizedUsername) {
    return profilePreferencesRepository.load(normalizedUsername).hasSeenWelcome();
  }

  /**
   * Records that the welcome dialog has been dismissed for the specified profile.
   *
   * @param normalizedUsername profile directory key
   */
  public void markWelcomeSeen(String normalizedUsername) {
    profilePreferencesRepository.save(normalizedUsername, new ProfilePreferences(true));
  }
}
