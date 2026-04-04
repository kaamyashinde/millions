package view;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import model.session.ActiveSession;
import model.session.SessionService;
import view.components.notification.NotificationService;
import view.components.notification.ToastTray;

/**
 * Displays one logged-in user's session-scoped JavaFX workspace.
 */
public class SessionWorkspaceView extends StackPane {

  private final ActiveSession session;
  private final SessionService sessionService;
  private final NotificationService notifications;
  private final PlayerPortfolioPanel playerPanel;
  private final StocksListPanel stocksPanel;
  private final FundsListPanel fundsPanel;
  private final NotificationsPanel notificationsPanel;
  private final RegularSavingsPanel savingsPanel;
  private final SavedRunsPanel savedRunsPanel;
  private final LeaderboardPanel leaderboardPanel;
  private final Label sessionSummaryLabel = new Label();
  private final ImageView headerAvatar = new ImageView();

  /**
   * Builds the logged-in workspace for one active session.
   *
   * @param session active session supplying the current player and exchange
   * @param sessionService session service for profile and leaderboard actions
   * @param notifications session-scoped notification service
   * @param notificationsPanel notifications tab bound to the notification service
   * @param playerPanel player summary tab
   * @param stocksPanel stocks listing tab
   * @param fundsPanel funds listing tab
   * @param savedRunsPanel saved playthroughs tab
   * @param helpAction callback to open help / welcome content
   * @param leaderboardPanel local leaderboard tab
   * @param logoutAction callback invoked when the user logs out
   * @param switchUserAction callback invoked when the user wants to switch profiles
   * @param persistAction callback invoked after a successful model mutation
   * @param onProfileAccountDeleted callback after the current profile was deleted from the editor
   */
  public SessionWorkspaceView(
      ActiveSession session,
      SessionService sessionService,
      NotificationService notifications,
      NotificationsPanel notificationsPanel,
      PlayerPortfolioPanel playerPanel,
      StocksListPanel stocksPanel,
      FundsListPanel fundsPanel,
      SavedRunsPanel savedRunsPanel,
      Runnable helpAction,
      LeaderboardPanel leaderboardPanel,
      Runnable logoutAction,
      Runnable switchUserAction,
      Runnable persistAction,
      Runnable onProfileAccountDeleted) {
    this.session = session;
    this.sessionService = sessionService;
    this.notifications = notifications;
    this.playerPanel = playerPanel;
    this.stocksPanel = stocksPanel;
    this.fundsPanel = fundsPanel;
    this.notificationsPanel = notificationsPanel;
    this.savedRunsPanel = savedRunsPanel;
    sessionSummaryLabel.setStyle("-fx-text-fill: #bdbdbd;");
    this.leaderboardPanel = leaderboardPanel;
    this.savingsPanel = new RegularSavingsPanel(
        session.exchange(),
        session.player(),
        notifications,
        () -> {
          refreshAll();
          persistAction.run();
        });

    BorderPane content = new BorderPane();
    content.setPadding(new Insets(16));
    content.setStyle("-fx-background-color: #121212;");

    Text heading = new Text("Millions");
    heading.setFont(Font.font("System", FontWeight.BOLD, 26));

    headerAvatar.setFitWidth(40);
    headerAvatar.setFitHeight(40);
    headerAvatar.setPreserveRatio(true);
    headerAvatar.setSmooth(true);

    Button profileButton = new Button("Profile");
    Button refreshButton = new Button("Refresh all");
    Button helpButton = new Button("Help");
    Button switchUserButton = new Button("Compare / switch user");
    Button logoutButton = new Button("Log out");
    styleButton(profileButton);
    styleButton(refreshButton);
    styleButton(helpButton);
    styleButton(switchUserButton);
    styleButton(logoutButton);
    profileButton.setOnAction(_ -> ProfileEditorDialog.show(
        getScene().getWindow(),
        sessionService,
        () -> {
          refreshAll();
          persistAction.run();
        },
        onProfileAccountDeleted));
    refreshButton.setOnAction(_ -> refreshAll());
    helpButton.setOnAction(_ -> helpAction.run());
    switchUserButton.setOnAction(_ -> switchUserAction.run());
    logoutButton.setOnAction(_ -> logoutAction.run());

    HBox actions = new HBox(10, profileButton, refreshButton, helpButton, switchUserButton, logoutButton);
    actions.setAlignment(Pos.CENTER_RIGHT);

    HBox topRow = new HBox(16, heading, headerAvatar, sessionSummaryLabel, actions);
    topRow.setAlignment(Pos.CENTER_LEFT);
    HBox.setMargin(actions, new Insets(0, 0, 0, 16));
    topRow.setFillHeight(true);

    TabPane tabs = createTabs(persistAction);
    VBox center = new VBox(14, topRow, tabs);
    content.setCenter(center);

    ToastTray tray = new ToastTray(notifications.getItems());
    StackPane toastArea = new StackPane(tray);
    toastArea.setAlignment(Pos.TOP_RIGHT);
    toastArea.setPadding(new Insets(16, 16, 0, 0));
    toastArea.setMouseTransparent(true);

    getChildren().addAll(content, toastArea);
    refreshAll();
  }

  /**
   * Refreshes all session-bound panels and header text.
   */
  public void refreshAll() {
    sessionSummaryLabel.setText(
        "Logged in as "
            + session.player().getName()
            + " ("
            + session.username()
            + ") | Trading day "
            + session.exchange().getDay());
    loadHeaderAvatar();
    playerPanel.refresh();
    stocksPanel.refresh();
    fundsPanel.refresh();
    savedRunsPanel.refresh();
    leaderboardPanel.refresh();
  }

  private void loadHeaderAvatar() {
    headerAvatar.setImage(null);
    var path = sessionService.avatarPath(session.normalizedUsername());
    if (!Files.isRegularFile(path)) {
      return;
    }
    try (InputStream in = Files.newInputStream(path)) {
      headerAvatar.setImage(new Image(in, 40, 40, true, true));
    } catch (IOException exception) {
      headerAvatar.setImage(null);
    }
  }

  /**
   * Clears notifications and timers before the workspace is discarded.
   */
  public void dispose() {
    notifications.clear();
  }

  /**
   * Returns the session-scoped notification service.
   *
   * @return active notification service
   */
  public NotificationService getNotificationService() {
    return notifications;
  }

  /**
   * Returns the player panel for assertions and refresh checks.
   *
   * @return player portfolio panel
   */
  public PlayerPortfolioPanel getPlayerPanel() {
    return playerPanel;
  }

  /**
   * Returns the login username (not the display name).
   *
   * @return active session login name
   */
  public String getDisplayedUsername() {
    return session.username();
  }

  private TabPane createTabs(Runnable persistAction) {
    Tab notificationsTab = new Tab("Notifications", notificationsPanel);
    Tab playerTab = new Tab("Player", playerPanel);
    Tab stocksTab = new Tab("Stocks", stocksPanel);
    Tab fundsTab = new Tab("Funds", fundsPanel);
    Tab savingsTab = new Tab("Savings", savingsPanel);
    Tab savedRunsTab = new Tab("Saved runs", savedRunsPanel);
    Tab leaderboardTab = new Tab("Leaderboard", leaderboardPanel);

    notificationsTab.setClosable(false);
    playerTab.setClosable(false);
    stocksTab.setClosable(false);
    fundsTab.setClosable(false);
    savingsTab.setClosable(false);
    savedRunsTab.setClosable(false);
    leaderboardTab.setClosable(false);

    playerTab.selectedProperty().addListener((obs, oldValue, selected) -> {
      if (Boolean.TRUE.equals(selected)) {
        playerPanel.refresh();
      }
    });
    stocksTab.selectedProperty().addListener((obs, oldValue, selected) -> {
      if (Boolean.TRUE.equals(selected)) {
        stocksPanel.refresh();
      }
    });
    fundsTab.selectedProperty().addListener((obs, oldValue, selected) -> {
      if (Boolean.TRUE.equals(selected)) {
        fundsPanel.refresh();
      }
    });
    savedRunsTab.selectedProperty().addListener((obs, oldValue, selected) -> {
      if (Boolean.TRUE.equals(selected)) {
        savedRunsPanel.refresh();
        persistAction.run();
      }
    });
    leaderboardTab.selectedProperty().addListener((obs, oldValue, selected) -> {
      if (Boolean.TRUE.equals(selected)) {
        leaderboardPanel.refresh();
      }
    });

    return new TabPane(
        notificationsTab, playerTab, stocksTab, fundsTab, savingsTab, savedRunsTab, leaderboardTab);
  }

  private static void styleButton(Button button) {
    button.setStyle(
        "-fx-border-radius: 6;"
            + "-fx-background-radius: 6;"
            + "-fx-cursor: hand;");
  }
}
