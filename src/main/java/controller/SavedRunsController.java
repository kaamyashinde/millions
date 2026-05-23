package controller;


import java.util.List;
import model.persistence.ProfileFile;
import model.session.SessionService;

/**
 * Controller for saved playthrough snapshots.
 */
public final class SavedRunsController {

  private final SessionService sessionService;

  public SavedRunsController(SessionService sessionService) {
    this.sessionService = sessionService;
  }

  public List<ProfileFile.SavedRunRow> listRuns() {
    return sessionService.listSavedRuns();
  }

  public ProfileFile.SavedRunRow saveCurrentRun(String label) {
    return sessionService.saveCurrentRun(label);
  }

  public boolean deleteRun(String runId) {
    return sessionService.deleteSavedRun(runId);
  }
}
