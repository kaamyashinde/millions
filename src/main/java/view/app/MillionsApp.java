package view.app;

import model.core.player.Portfolio;
import model.learning.quiz.Quiz;

import java.math.BigDecimal;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import java.util.Optional;
import model.session.ActiveSession;
import model.exception.auth.AuthenticationException;
import model.exception.auth.DuplicateUsernameException;
import model.exception.market.MarketDataImportException;
import model.session.SessionService;
import model.session.SessionServiceFactory;
import controller.WorkspaceController;
import view.dialogs.WelcomeDialog;
import view.layout.WorkspaceLayout;
import view.pages.auth.LoginPage;
import view.pages.auth.RegisterPage;
import view.pages.funds.FundsPage;
import view.pages.leaderboard.LeaderboardPage;
import view.pages.learning.LearningHubPage;
import view.pages.notifications.NotificationsPage;
import view.pages.portfolio.PlayerPortfolioPage;
import view.pages.quiz.QuizLauncherPage;
import view.pages.saved.SavedRunsPage;
import view.pages.savings.SavingsPage;
import view.pages.stocks.StocksPage;
import view.pages.transactions.TransactionHistoryPage;
import view.theme.ThemeStyles;
import view.validation.AuthFormValidation;

/**
 * JavaFX entry point for the Millions application.
 *
 * <p>Boots the session service and displays the login page as the first screen.
 * Navigation between login and register swaps the scene root in place.
 * After a successful login or registration the application proceeds to the main workspace.
 */
public class MillionsApp extends Application {

  private static final String EXCHANGE_NAME = "NYSE";
  private static final String MARKET_DATA_RESOURCE = "/data/demo-stocks.csv";
  private static final int WINDOW_WIDTH = 1100;
  private static final int WINDOW_HEIGHT = 720;
  private static final int MIN_WINDOW_WIDTH = 800;
  private static final int MIN_WINDOW_HEIGHT = 600;

  private SessionService sessionService;
  private Stage primaryStage;
  private Scene scene;
  private WorkspaceController currentWorkspace;

  @Override
  public void start(Stage stage) {
    this.primaryStage = stage;
    this.sessionService = createSessionService();

    LoginPage loginPage = buildLoginPage();
    loginPage.setMinWidth(MIN_WINDOW_WIDTH);
    loginPage.setMinHeight(MIN_WINDOW_HEIGHT);
    scene = new Scene(loginPage, WINDOW_WIDTH, WINDOW_HEIGHT);
    ThemeStyles.install(scene);

    stage.setScene(scene);
    stage.setTitle("Millions");
    stage.setMinWidth(MIN_WINDOW_WIDTH);
    stage.setMinHeight(MIN_WINDOW_HEIGHT);
    stage.setOnCloseRequest(_ -> sessionService.saveActiveSession());
    stage.show();
  }

  /**
   * Launches the Millions JavaFX application.
   *
   * @param args command-line arguments
   */
  public static void main(String[] args) {
    launch(args);
  }

  private LoginPage buildLoginPage() {
    LoginPage loginPage = new LoginPage(
        this::handleLogin,
        () -> setSceneRoot(buildRegisterPage()));
    return loginPage;
  }

  private RegisterPage buildRegisterPage() {
    RegisterPage registerPage = new RegisterPage(
        this::handleRegistration,
        () -> setSceneRoot(buildLoginPage()));
    return registerPage;
  }

  private void setSceneRoot(Region root) {
    root.setMinWidth(MIN_WINDOW_WIDTH);
    root.setMinHeight(MIN_WINDOW_HEIGHT);
    scene.setRoot(root);
  }

  private void handleLogin(String username, String pin) {
    LoginPage loginPage = (LoginPage) scene.getRoot();
    try {
      ActiveSession session = sessionService.login(username, pin.toCharArray());
      onSessionStarted(session);
    } catch (AuthenticationException e) {
      loginPage.setStatus("Invalid username or PIN.");
    } catch (IllegalArgumentException e) {
      loginPage.setStatus(mapValidationMessage(e.getMessage()));
    } catch (RuntimeException e) {
      loginPage.setStatus("Could not load profile. Please try again.");
    }
  }

  private void handleRegistration(
      String username,
      String pin,
      String startingMoneyText,
      Optional<java.nio.file.Path> marketDataFile) {
    RegisterPage registerPage = (RegisterPage) scene.getRoot();
    try {
      BigDecimal startingMoney = new BigDecimal(startingMoneyText.trim());
      ActiveSession session = sessionService.register(
          username, pin.toCharArray(), startingMoney, marketDataFile);
      onSessionStarted(session);
    } catch (NumberFormatException e) {
      registerPage.setStatus("Starting money must be a valid number.");
    } catch (DuplicateUsernameException e) {
      registerPage.setStatus("That username is already taken.");
    } catch (MarketDataImportException e) {
      registerPage.setMarketDataStatus(e.getMessage());
    } catch (IllegalArgumentException e) {
      registerPage.setStatus(mapValidationMessage(e.getMessage()));
    }
  }

  private void onSessionStarted(ActiveSession session) {
    if (currentWorkspace != null) {
      currentWorkspace.dispose();
    }
    currentWorkspace = new WorkspaceController(session, sessionService);
    WorkspaceLayout workspace = buildWorkspace(currentWorkspace);
    setSceneRoot(workspace);
    primaryStage.setTitle("Millions — " + session.username());
  }

  private WorkspaceLayout buildWorkspace(WorkspaceController ctrl) {
    TabPane tabs = buildWorkspaceTabs(ctrl);
    WorkspaceLayout[] ref = new WorkspaceLayout[1];
    WorkspaceLayout workspace = new WorkspaceLayout(
        ctrl.getNotifications(),
        tabs,
        () -> { /* profile editor: placeholder */ },
        ctrl::refreshAll,
        () -> WelcomeDialog.show(primaryStage),
        () -> switchUser(ctrl, ref[0]),
        () -> logout(ctrl));
    ref[0] = workspace;
    workspace.setSessionSummary(ctrl.getSessionSummary());
    workspace.loadHeaderAvatar(ctrl.getAvatarPath());
    return workspace;
  }

  private TabPane buildWorkspaceTabs(WorkspaceController ctrl) {
    ActiveSession session = ctrl.getSession();
    SessionService svc = ctrl.getSessionService();

    Runnable refreshAndPersist = () -> {
      ctrl.refreshAll();
      sessionService.saveActiveSession();
    };

    PlayerPortfolioPage portfolioPage = new PlayerPortfolioPage(
        ctrl.getPortfolio(), ctrl.getTrading(), refreshAndPersist);
    StocksPage stocksPage = new StocksPage(
        ctrl.getStocks(), ctrl.getStockDetail(), ctrl.getTrading(), refreshAndPersist);
    FundsPage fundsPage = new FundsPage(session.exchange(), ctrl.getTrading(), refreshAndPersist);
    SavingsPage savingsPage = new SavingsPage(ctrl.getSavings(), refreshAndPersist);
    TransactionHistoryPage transactionsPage =
        new TransactionHistoryPage(session.exchange(), session.player());
    SavedRunsPage savedRunsPage = new SavedRunsPage(svc, ctrl::refreshAll);
    LeaderboardPage leaderboardPage = new LeaderboardPage(svc);
    LearningHubPage learningHubPage =
        new LearningHubPage(ctrl.getLearningHub(), ctrl.getQuiz());
    NotificationsPage notificationsPage = new NotificationsPage(ctrl.getNotificationsTab());

    Tab portfolioTab = makeTab("Portfolio", portfolioPage);
    Tab stocksTab = makeTab("Stocks", stocksPage);
    Tab fundsTab = makeTab("Funds", fundsPage);
    Tab savingsTab = makeTab("Savings", savingsPage);
    Tab transactionsTab = makeTab("Transactions", transactionsPage);

    portfolioTab.selectedProperty().addListener((obs, oldVal, sel) -> {
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
    Tab savedRunsTab = makeTab("Saved Runs", savedRunsPage);
    Tab leaderboardTab = makeTab("Leaderboard", leaderboardPage);
    Tab learningTab = makeTab("Learning Hub", learningHubPage);
    Tab quizTab = new Tab("Quiz");
    quizTab.setClosable(false);
    Tab notificationsTab = makeTab("Notifications", notificationsPage);

    TabPane tabs = new TabPane(
        portfolioTab, stocksTab, fundsTab, savingsTab, transactionsTab,
        savedRunsTab, leaderboardTab, learningTab, quizTab, notificationsTab);
    tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

    java.util.function.Consumer<model.learning.content.LearningItem> openTopicInHub =
        item -> {
          learningHubPage.openTopic(item.id());
          tabs.getSelectionModel().select(learningTab);
        };
    quizTab.setContent(new QuizLauncherPage(
        ctrl.getQuiz(), ctrl.getLearningHub(), openTopicInHub));
    return tabs;
  }

  private static Tab makeTab(String label, javafx.scene.Node content) {
    Tab tab = new Tab(label, content);
    tab.setClosable(false);
    return tab;
  }

  private void logout(WorkspaceController ctrl) {
    sessionService.saveActiveSession();
    ctrl.dispose();
    sessionService.logout();
    currentWorkspace = null;
    scene.setRoot(buildLoginPage());
    primaryStage.setTitle("Millions");
  }

  private void switchUser(WorkspaceController ctrl, WorkspaceLayout currentView) {
    sessionService.saveActiveSession();
    LoginPage loginPage = new LoginPage(
        this::handleLogin,
        () -> scene.setRoot(buildRegisterPage()),
        null, null, true,
        () -> scene.setRoot(currentView));
    scene.setRoot(loginPage);
    primaryStage.setTitle("Millions");
  }

  private static SessionService createSessionService() {
    return SessionServiceFactory.createLocalProfileSessionService(
        SessionServiceFactory.defaultProfilesRoot(),
        MARKET_DATA_RESOURCE,
        MillionsApp.class,
        EXCHANGE_NAME);
  }

  private static String mapValidationMessage(String message) {
    return AuthFormValidation.mapValidationMessage(message);
  }
}
