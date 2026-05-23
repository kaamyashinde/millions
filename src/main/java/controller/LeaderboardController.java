package controller;

import static model.utils.Validator.checkNotNull;

import java.util.List;
import model.session.leaderboard.LocalLeaderboardService.LeaderboardRow;
import model.session.SessionService;

/**
 * Supplies local leaderboard rows ranked by net worth.
 */
public class LeaderboardController {

  private final SessionService sessionService;

  /**
   * @param sessionService session API for leaderboard data
   */
  public LeaderboardController(SessionService sessionService) {
    checkNotNull(sessionService, "sessionService");
    this.sessionService = sessionService;
  }

  public SessionService getSessionService() {
    return sessionService;
  }

  /**
   * @return leaderboard rows sorted by net worth (newest eligible runs)
   */
  public List<LeaderboardRow> listRows() {
    return sessionService.leaderboardService().loadRows();
  }
}
