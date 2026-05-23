package controller;

import static util.Validator.checkNotNull;

import java.util.List;
import java.util.UUID;
import model.persistence.savedrun.SavedRunRecord;
import model.session.SessionService;

/**
 * Manages saved playthrough snapshots for the saved-runs tab.
 */
public class SavedRunsController {

  private final SessionService sessionService;

  /**
   * @param sessionService session API for run persistence
   */
  public SavedRunsController(SessionService sessionService) {
    checkNotNull(sessionService, "sessionService");
    this.sessionService = sessionService;
  }

  public List<SavedRunRecord> listRuns() {
    return sessionService.listSavedRuns();
  }

  public SavedRunRecord saveCurrentRun(String label) {
    return sessionService.saveCurrentRun(label);
  }

  public boolean deleteRun(UUID runId) {
    return sessionService.deleteSavedRun(runId);
  }

  public boolean setLeaderboardEligible(UUID runId, boolean eligible) {
    return sessionService.setRunLeaderboardEligible(runId, eligible);
  }
}
