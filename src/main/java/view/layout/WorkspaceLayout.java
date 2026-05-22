package view.layout;

import java.nio.file.Path;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import view.components.image.FileImageLoader;
import view.components.image.ImageLoader;
import view.components.image.ValidatingImageLoader;
import view.components.notification.NotificationService;
import view.components.notification.ToastTray;
import view.theme.ThemePalette;
import view.theme.ThemeStyles;

/**
 * Logged-in workspace shell: header bar, tabbed content, and floating toast overlay.
 */
public class WorkspaceLayout extends StackPane {

  private final Label sessionSummaryLabel = new Label();
  private final ImageView headerAvatar = new ImageView();
  private final ImageLoader avatarLoader = new ValidatingImageLoader(new FileImageLoader());
  private final NotificationService notifications;

  /**
   * Builds the workspace layout.
   *
   * @param notifications session-scoped notification service for the toast tray
   * @param tabs tab pane content (non-closable tabs supplied by caller)
   * @param onProfile opens the profile editor
   * @param onRefresh refreshes all session-bound panels
   * @param onHelp opens help / welcome content
   * @param onSwitchUser begins the compare / switch-user flow
   * @param onLogout logs out the current user
   */
  public WorkspaceLayout(
      NotificationService notifications,
      TabPane tabs,
      Runnable onProfile,
      Runnable onRefresh,
      Runnable onHelp,
      Runnable onSwitchUser,
      Runnable onLogout) {
    this.notifications = notifications;

    BorderPane content = new BorderPane();
    content.setPadding(new Insets(16));
    content.setStyle(ThemeStyles.workspaceBackground());

    Text heading = new Text("Millions");
    heading.setFont(Font.font("System", FontWeight.BOLD, 26));
    heading.setStyle("-fx-fill: " + ThemePalette.ACCENT + ";");

    headerAvatar.setFitWidth(40);
    headerAvatar.setFitHeight(40);
    headerAvatar.setPreserveRatio(true);
    headerAvatar.setSmooth(true);

    sessionSummaryLabel.setStyle(ThemeStyles.mutedText());

    Button profileButton = new Button("Profile");
    Button refreshButton = new Button("Refresh all");
    Button helpButton = new Button("Help");
    Button switchUserButton = new Button("Compare / switch user");
    Button logoutButton = new Button("Log out");
    ThemeStyles.styleButton(profileButton);
    ThemeStyles.styleButton(refreshButton);
    ThemeStyles.styleButton(helpButton);
    ThemeStyles.styleButton(switchUserButton);
    ThemeStyles.styleButton(logoutButton);

    profileButton.setOnAction(_ -> onProfile.run());
    refreshButton.setOnAction(_ -> onRefresh.run());
    helpButton.setOnAction(_ -> onHelp.run());
    switchUserButton.setOnAction(_ -> onSwitchUser.run());
    logoutButton.setOnAction(_ -> onLogout.run());

    HBox actions =
        new HBox(10, profileButton, refreshButton, helpButton, switchUserButton, logoutButton);
    actions.setAlignment(Pos.CENTER_RIGHT);

    HBox topRow = new HBox(16, heading, headerAvatar, sessionSummaryLabel, actions);
    topRow.setAlignment(Pos.CENTER_LEFT);
    HBox.setMargin(actions, new Insets(0, 0, 0, 16));

    VBox center = new VBox(14, topRow, tabs);
    content.setCenter(center);

    ToastTray tray = new ToastTray(notifications.getItems());
    StackPane toastArea = new StackPane(tray);
    toastArea.setAlignment(Pos.TOP_RIGHT);
    toastArea.setPadding(new Insets(16, 16, 0, 0));
    toastArea.setMouseTransparent(true);

    getChildren().addAll(content, toastArea);
  }

  /**
   * Updates the header summary line.
   *
   * @param text summary text (player name, username, trading day)
   */
  public void setSessionSummary(String text) {
    sessionSummaryLabel.setText(text);
  }

  /**
   * Loads the header avatar from disk when present.
   *
   * @param avatarPath path to the profile avatar image
   */
  public void loadHeaderAvatar(Path avatarPath) {
    headerAvatar.setImage(avatarLoader.load(avatarPath, 40));
  }

  /**
   * Returns the session-scoped notification service.
   *
   * @return notification service backing the toast tray
   */
  public NotificationService getNotificationService() {
    return notifications;
  }

  /**
   * Clears notifications before the workspace is discarded.
   */
  public void dispose() {
    notifications.clear();
  }
}
