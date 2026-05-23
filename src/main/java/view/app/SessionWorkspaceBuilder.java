package view.app;

import model.core.player.Player;

import controller.WorkspaceController;
import java.util.function.Consumer;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import model.learning.content.LearningItem;
import model.session.ActiveSession;
import model.session.SessionService;
import view.dialogs.ProfileEditorDialog;
import view.layout.WorkspaceLayout;
import view.pages.funds.FundsPage;
import view.pages.leaderboard.LeaderboardPage;
import view.pages.learning.LearningHubPage;
import view.pages.notifications.NotificationsPage;
import view.pages.portfolio.PlayerPortfolioPage;
import view.pages.quiz.QuizLauncherPage;
import view.pages.savings.SavingsPage;
import view.pages.stocks.StocksPage;
import view.pages.transactions.TransactionHistoryPage;

/**
 * Builds a {@link WorkspaceLayout} with all session tabs wired to a {@link WorkspaceController}.
 */
public final class SessionWorkspaceBuilder {

  private SessionWorkspaceBuilder() {}

  /**
   * Creates the logged-in workspace for one session.
   *
   * @param session active session
   * @param sessionService session service
   * @param helpAction opens help / welcome
   * @param logoutAction logs out
   * @param switchUserAction begins switch-user flow
   * @param persistAction persists session after mutations
   * @param onProfileDeleted invoked when profile is deleted from editor
   * @return workspace layout ready to set as scene root
   */
  public static WorkspaceLayout build(
      ActiveSession session,
      SessionService sessionService,
      Runnable helpAction,
      Runnable logoutAction,
      Runnable switchUserAction,
      Runnable persistAction,
      Runnable onProfileDeleted) {
    WorkspaceController workspaceController = new WorkspaceController(session, sessionService);

    Runnable refreshAndPersist = () -> {
      workspaceController.refreshAll();
      persistAction.run();
    };

    NotificationsPage notificationsPage =
        new NotificationsPage(workspaceController.getNotificationsTab());
    PlayerPortfolioPage portfolioPage =
        new PlayerPortfolioPage(
            workspaceController.getPortfolio(),
            workspaceController.getTrading(),
            workspaceController.getExitGame(),
            refreshAndPersist,
            onProfileDeleted);
    StocksPage stocksPage =
        new StocksPage(
            workspaceController.getStocks(),
            workspaceController.getStockDetail(),
            workspaceController.getTrading(),
            refreshAndPersist);
    FundsPage fundsPage =
        new FundsPage(session.exchange(), workspaceController.getTrading(), refreshAndPersist);
    SavingsPage savingsPage =
        new SavingsPage(workspaceController.getSavings(), refreshAndPersist);
    TransactionHistoryPage transactionsPage =
        new TransactionHistoryPage(session.exchange(), session.player());
    LeaderboardPage leaderboardPage = new LeaderboardPage(sessionService);
    LearningHubPage learningHubPage =
        new LearningHubPage(workspaceController.getLearningHub(), workspaceController.getQuiz());

    Tab notificationsTab = new Tab("Notifications", notificationsPage);
    Tab playerTab = new Tab("Player", portfolioPage);
    Tab stocksTab = new Tab("Stocks", stocksPage);
    Tab fundsTab = new Tab("Funds", fundsPage);
    Tab savingsTab = new Tab("Savings", savingsPage);
    Tab transactionsTab = new Tab("Transactions", transactionsPage);
    Tab leaderboardTab = new Tab("Leaderboard", leaderboardPage);
    Tab learningTab = new Tab("Learning", learningHubPage);
    Tab quizTab = new Tab("Quizzes");

    for (Tab tab :
        new Tab[] {
          notificationsTab,
          playerTab,
          stocksTab,
          fundsTab,
          savingsTab,
          transactionsTab,
          leaderboardTab,
          learningTab,
          quizTab
        }) {
      tab.setClosable(false);
    }

    playerTab.selectedProperty().addListener((obs, oldVal, sel) -> {
      if (Boolean.TRUE.equals(sel)) {
        portfolioPage.refresh();
      }
    });
    stocksTab.selectedProperty().addListener((obs, oldVal, sel) -> {
      if (Boolean.TRUE.equals(sel)) {
        stocksPage.refresh();
      }
    });
    fundsTab.selectedProperty().addListener((obs, oldVal, sel) -> {
      if (Boolean.TRUE.equals(sel)) {
        fundsPage.refresh();
      }
    });
    transactionsTab.selectedProperty().addListener((obs, oldVal, sel) -> {
      if (Boolean.TRUE.equals(sel)) {
        transactionsPage.refresh();
      }
    });
    leaderboardTab.selectedProperty().addListener((obs, oldVal, sel) -> {
      if (Boolean.TRUE.equals(sel)) {
        leaderboardPage.refresh();
      }
    });

    TabPane tabPane =
        new TabPane(
            notificationsTab,
            playerTab,
            stocksTab,
            fundsTab,
            savingsTab,
            transactionsTab,
            leaderboardTab,
            learningTab,
            quizTab);

    Consumer<LearningItem> openTopicInHub = item -> {
      learningHubPage.openTopic(item.id());
      tabPane.getSelectionModel().select(learningTab);
    };
    QuizLauncherPage quizLauncherPage = new QuizLauncherPage(
        workspaceController.getQuiz(),
        workspaceController.getLearningHub(),
        openTopicInHub);
    quizTab.setContent(quizLauncherPage);

    final WorkspaceLayout[] layoutHolder = new WorkspaceLayout[1];
    layoutHolder[0] =
        new WorkspaceLayout(
            workspaceController.getNotifications(),
            tabPane,
            () -> {
              var window =
                  layoutHolder[0].getScene() != null
                      ? layoutHolder[0].getScene().getWindow()
                      : null;
              ProfileEditorDialog.show(
                  window,
                  workspaceController.createProfileEditorController(),
                  workspaceController.getExitGame(),
                  refreshAndPersist,
                  onProfileDeleted);
            },
            refreshAndPersist,
            helpAction,
            switchUserAction,
            logoutAction);

    layoutHolder[0].setSessionSummary(workspaceController.getSessionSummary());
    layoutHolder[0].loadHeaderAvatar(workspaceController.getAvatarPath());
    return layoutHolder[0];
  }
}
