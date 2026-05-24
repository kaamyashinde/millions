package view.app;

import static view.app.events.WorkspaceEventType.LEADERBOARD_CHANGED;
import static view.app.events.WorkspaceEventType.MARKET_CHANGED;
import static view.app.events.WorkspaceEventType.PORTFOLIO_CHANGED;
import static view.app.events.WorkspaceEventType.PROFILE_CHANGED;
import static view.app.events.WorkspaceEventType.SAVINGS_CHANGED;
import static view.app.events.WorkspaceEventType.TRANSACTIONS_CHANGED;

import controller.WorkspaceController;
import java.util.function.Consumer;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import model.learning.content.LearningItem;
import model.session.ActiveSession;
import model.session.SessionService;
import view.app.events.WorkspaceEventBus;
import view.dialogs.ProfileEditorDialog;
import view.layout.WorkspaceLayout;
import view.pages.funds.FundsPage;
import view.pages.leaderboard.LeaderboardPage;
import view.pages.learning.LearningHubPage;
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
   * @param logoutAction logs out
   * @param persistAction persists session after mutations
   * @param onProfileDeleted invoked when profile is deleted from editor
   * @param onThemeToggle switches between dark and light themes
   * @return workspace layout ready to set as scene root
   */
  public static WorkspaceLayout build(
      ActiveSession session,
      SessionService sessionService,
      Runnable logoutAction,
      Runnable persistAction,
      Runnable onProfileDeleted,
      Runnable onThemeToggle) {
    WorkspaceController workspaceController = new WorkspaceController(session, sessionService);
    WorkspaceEventBus events = new WorkspaceEventBus();

    Runnable onTradeComplete = () -> {
      persistAction.run();
      events.publish(PORTFOLIO_CHANGED, TRANSACTIONS_CHANGED, LEADERBOARD_CHANGED);
    };
    Runnable onSavingsChanged = () -> {
      persistAction.run();
      events.publish(
          SAVINGS_CHANGED,
          PORTFOLIO_CHANGED,
          TRANSACTIONS_CHANGED,
          LEADERBOARD_CHANGED);
    };
    Runnable onProfileSaved = () -> {
      persistAction.run();
      events.publish(PROFILE_CHANGED, LEADERBOARD_CHANGED);
    };

    PlayerPortfolioPage portfolioPage =
        new PlayerPortfolioPage(
            workspaceController.getPortfolio(),
            workspaceController.getTrading(),
            workspaceController.getExitGame(),
            onTradeComplete,
            onProfileDeleted);
    StocksPage stocksPage =
        new StocksPage(
            workspaceController.getStocks(),
            workspaceController.getStockDetail(),
            workspaceController.getTrading(),
            onTradeComplete);
    FundsPage fundsPage =
        new FundsPage(session.exchange(), workspaceController.getTrading(), onTradeComplete);
    SavingsPage savingsPage =
        new SavingsPage(workspaceController.getSavings(), onSavingsChanged);
    TransactionHistoryPage transactionsPage =
        new TransactionHistoryPage(session.exchange(), session.player());
    LeaderboardPage leaderboardPage = new LeaderboardPage(sessionService);
    LearningHubPage learningHubPage =
        new LearningHubPage(workspaceController.getLearningHub(), workspaceController.getQuiz());

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

    registerPageObservers(
        events,
        portfolioPage,
        stocksPage,
        fundsPage,
        savingsPage,
        transactionsPage,
        leaderboardPage);

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
    savingsTab.selectedProperty().addListener((obs, oldVal, sel) -> {
      if (Boolean.TRUE.equals(sel)) {
        savingsPage.refresh();
      }
    });
    leaderboardTab.selectedProperty().addListener((obs, oldVal, sel) -> {
      if (Boolean.TRUE.equals(sel)) {
        leaderboardPage.refresh();
      }
    });

    TabPane tabPane =
        new TabPane(
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
                  onProfileSaved,
                  onProfileDeleted);
            },
            logoutAction,
            onThemeToggle,
            days -> {
              workspaceController.advanceTradingDays(String.valueOf(days));
              persistAction.run();
              events.publish(
                  MARKET_CHANGED,
                  SAVINGS_CHANGED,
                  PORTFOLIO_CHANGED,
                  TRANSACTIONS_CHANGED,
                  LEADERBOARD_CHANGED);
            });

    layoutHolder[0].setSessionSummary(workspaceController.getSessionSummary());
    layoutHolder[0].loadHeaderAvatar(workspaceController.getAvatarPath());
    registerHeaderObservers(events, layoutHolder[0], workspaceController);
    return layoutHolder[0];
  }

  private static void registerPageObservers(
      WorkspaceEventBus events,
      PlayerPortfolioPage portfolioPage,
      StocksPage stocksPage,
      FundsPage fundsPage,
      SavingsPage savingsPage,
      TransactionHistoryPage transactionsPage,
      LeaderboardPage leaderboardPage) {
    Runnable portfolioRefresh = portfolioPage::refresh;
    Runnable stocksRefresh = stocksPage::refresh;
    Runnable fundsRefresh = fundsPage::refresh;
    Runnable savingsRefresh = savingsPage::refresh;
    Runnable transactionsRefresh = transactionsPage::refresh;
    Runnable leaderboardRefresh = leaderboardPage::refresh;

    events.subscribe(PORTFOLIO_CHANGED, portfolioRefresh);
    events.subscribe(MARKET_CHANGED, portfolioRefresh);
    events.subscribe(SAVINGS_CHANGED, portfolioRefresh);
    events.subscribe(PROFILE_CHANGED, portfolioRefresh);
    events.subscribe(MARKET_CHANGED, stocksRefresh);
    events.subscribe(PORTFOLIO_CHANGED, stocksRefresh);
    events.subscribe(MARKET_CHANGED, fundsRefresh);
    events.subscribe(PORTFOLIO_CHANGED, fundsRefresh);
    events.subscribe(SAVINGS_CHANGED, savingsRefresh);
    events.subscribe(MARKET_CHANGED, savingsRefresh);
    events.subscribe(TRANSACTIONS_CHANGED, transactionsRefresh);
    events.subscribe(SAVINGS_CHANGED, transactionsRefresh);
    events.subscribe(LEADERBOARD_CHANGED, leaderboardRefresh);
    events.subscribe(SAVINGS_CHANGED, leaderboardRefresh);
    events.subscribe(PROFILE_CHANGED, leaderboardRefresh);
  }

  private static void registerHeaderObservers(
      WorkspaceEventBus events,
      WorkspaceLayout layout,
      WorkspaceController workspaceController) {
    Runnable refreshSummary = () -> layout.setSessionSummary(workspaceController.getSessionSummary());
    Runnable refreshAvatar = () -> layout.loadHeaderAvatar(workspaceController.getAvatarPath());

    events.subscribe(MARKET_CHANGED, refreshSummary);
    events.subscribe(PROFILE_CHANGED, refreshSummary);
    events.subscribe(PROFILE_CHANGED, refreshAvatar);
  }
}
