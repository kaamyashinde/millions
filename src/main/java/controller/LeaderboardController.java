package controller;

import static util.Validator.checkNotNull;

import java.util.List;
import model.session.SessionService;
import model.session.leaderboard.LocalLeaderboardService.LeaderboardRow;

/**
 * Supplies local leaderboard rows ranked by net worth.
 *
 * <p>Rows are loaded through {@link SessionService#leaderboardService()} and consumed by
 * {@link view.pages.leaderboard.LeaderboardPage}.
 *
 * @author kevindmazali
 * @contributor kaamyashinde
 * @version 1.0.0
 * @since 2026-05-02
 */
public class LeaderboardController {

  private final SessionService sessionService;

  /**
   * Creates a leaderboard controller backed by the session service.
   *
   * @param sessionService session API for leaderboard data
   */
  public LeaderboardController(SessionService sessionService) {
    checkNotNull(sessionService, "sessionService");
    this.sessionService = sessionService;
  }

  /**
   * Exposes the session service used for leaderboard loading.
   *
   * @return session API used to load leaderboard rows
   */
  public SessionService getSessionService() {
    return sessionService;
  }

  /**
   * Loads leaderboard rows from local profile data.
   *
   * @return leaderboard rows sorted by net worth (newest eligible runs)
   */
  public List<LeaderboardRow> listRows() {
    return sessionService.leaderboardService().loadRows();
  }
}
