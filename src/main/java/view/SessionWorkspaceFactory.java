package view;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import model.Stock;
import model.session.ActiveSession;
import model.session.SessionService;
import view.components.notification.NotificationService;
import view.components.toast.ToastMode;

/**
 * Builds one session-scoped GUI workspace from an {@link ActiveSession}.
 */
public class SessionWorkspaceFactory {

  private static final int TICKER_PREVIEW_MAX = 8;

  /**
   * Creates a new session workspace with fresh views and notifications.
   *
   * @param session active session supplying player and exchange state
   * @param sessionService session service for avatars and leaderboard
   * @param logoutAction callback invoked when the user logs out
   * @param switchUserAction callback invoked when the user wants to switch profiles
   * @param persistAction callback invoked after a successful model mutation
   * @param onProfileAccountDeleted callback after the active profile was deleted from the editor
   * @return fresh workspace bound to the supplied session
   */
  public SessionWorkspaceView create(
      ActiveSession session,
      SessionService sessionService,
      Runnable logoutAction,
      Runnable switchUserAction,
      Runnable persistAction,
      Runnable onProfileAccountDeleted) {
    NotificationService notifications = new NotificationService();
    NotificationsPanel notificationsPanel = new NotificationsPanel(notifications);
    Path avatarPath = sessionService.avatarPath(session.normalizedUsername());
    PlayerPortfolioPanel playerPanel = new PlayerPortfolioPanel(session.exchange(), session.player(), avatarPath);
    StocksListPanel stocksPanel = new StocksListPanel(session.exchange());
    FundsListPanel fundsPanel = new FundsListPanel(session.exchange());
    LeaderboardPanel leaderboardPanel = new LeaderboardPanel(sessionService);

    showLoadedNotifications(notifications, session);

    return new SessionWorkspaceView(
        session,
        sessionService,
        notifications,
        notificationsPanel,
        playerPanel,
        stocksPanel,
        fundsPanel,
        leaderboardPanel,
        logoutAction,
        switchUserAction,
        persistAction,
        onProfileAccountDeleted);
  }

  private void showLoadedNotifications(NotificationService notifications, ActiveSession session) {
    List<Stock> stocks = session.exchange().findStocks("");
    String tickers = stocks.stream()
        .map(Stock::getSymbol)
        .limit(TICKER_PREVIEW_MAX)
        .collect(Collectors.joining(", "));
    if (stocks.size() > TICKER_PREVIEW_MAX) {
      tickers = tickers + ", …";
    }
    notifications.show(
        ToastMode.INFO,
        "Game loaded",
        session.exchange().getName()
            + " · "
            + stocks.size()
            + " stock(s): "
            + tickers);
    notifications.show(
        ToastMode.SUCCESS,
        "Player ready",
        session.player().getName() + " · balance " + session.player().getMoney().toPlainString());
  }
}
