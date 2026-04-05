package model.session;

import java.util.List;
import java.util.UUID;
import model.player.Player;
import model.market.Exchange;
import model.persistence.SavedRunMapper;
import model.persistence.SavedRunRecord;
import model.persistence.SavedRunRepository;

/**
 * Manages saved playthrough snapshots for user profiles.
 */
public final class SavedRunService {

  private final SavedRunRepository savedRunRepository;
  private final SavedRunMapper savedRunMapper;

  /**
   * Creates a saved-run service with the supplied repository and mapper.
   *
   * @param savedRunRepository persists run snapshot files
   * @param savedRunMapper     converts live state to run records
   */
  public SavedRunService(SavedRunRepository savedRunRepository, SavedRunMapper savedRunMapper) {
    this.savedRunRepository = savedRunRepository;
    this.savedRunMapper = savedRunMapper;
  }

  /**
   * Captures the current game state as a saved run snapshot.
   *
   * @param normalizedUsername profile directory key
   * @param player             active player state
   * @param exchange           active exchange state
   * @param label              optional label for the run
   * @return persisted run record
   */
  public SavedRunRecord saveCurrentRun(
      String normalizedUsername, Player player, Exchange exchange, String label) {
    SavedRunRecord record = savedRunMapper.toSavedRun(player, exchange, label, false);
    savedRunRepository.save(normalizedUsername, record);
    return record;
  }

  /**
   * Lists all saved runs for a profile, newest first.
   *
   * @param normalizedUsername profile directory key
   * @return sorted run snapshots
   */
  public List<SavedRunRecord> listSavedRuns(String normalizedUsername) {
    return savedRunRepository.list(normalizedUsername);
  }

  /**
   * Deletes one saved run.
   *
   * @param normalizedUsername profile directory key
   * @param runId              run identifier
   * @return {@code true} when a run file was removed
   */
  public boolean deleteSavedRun(String normalizedUsername, UUID runId) {
    return savedRunRepository.delete(normalizedUsername, runId);
  }

  /**
   * Updates the leaderboard eligibility flag for one saved run.
   *
   * @param normalizedUsername     profile directory key
   * @param runId                  run identifier
   * @param eligibleForLeaderboard new value
   * @return {@code true} when the run existed and was updated
   */
  public boolean setRunLeaderboardEligible(
      String normalizedUsername, UUID runId, boolean eligibleForLeaderboard) {
    return savedRunRepository.updateLeaderboardFlag(
        normalizedUsername, runId, eligibleForLeaderboard);
  }
}
