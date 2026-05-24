package view.layout;

import controller.WorkspaceController;
import java.nio.file.Path;
import java.util.function.IntConsumer;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import view.components.image.FileImageLoader;
import view.components.image.ImageLoader;
import view.components.image.ValidatingImageLoader;
import util.Validator;
import view.components.notification.NotificationService;
import view.components.notification.ToastTray;
import view.components.toast.ToastMode;
import view.theme.ThemeManager;
import view.theme.ThemeStyles;

/**
 * Logged-in workspace shell: header bar, tabbed content, and floating toast overlay.
 */
public class WorkspaceLayout extends StackPane implements ResponsiveLayout {

  private final Label sessionSummaryLabel = new Label();
  private final TabPane tabs;
  private final ImageView headerAvatar = new ImageView();
  private final ImageLoader avatarLoader = new ValidatingImageLoader(new FileImageLoader());
  private final NotificationService notifications;

  /**
   * Builds the workspace layout.
   *
   * @param notifications session-scoped notification service for the toast tray
   * @param tabs tab pane content (non-closable tabs supplied by caller)
   * @param onProfile opens the profile editor
   * @param onLogout logs out the current user
   * @param onThemeToggle switches between dark and light themes
   * @param onSkipTradingDays advances the exchange by the validated number of trading days
   */
  public WorkspaceLayout(
      NotificationService notifications,
      TabPane tabs,
      Runnable onProfile,
      Runnable onLogout,
      Runnable onThemeToggle,
      IntConsumer onSkipTradingDays) {
    this.notifications = notifications;
    this.tabs = tabs;
    ThemeStyles.addStyleClasses(this, "workspace-root");

    BorderPane content = new BorderPane();
    content.setPadding(new Insets(16));
    ThemeStyles.addStyleClasses(content, "workspace-content");

    Text heading = new Text("Millions");
    heading.setFont(Font.font("System", FontWeight.BOLD, 26));
    ThemeStyles.addStyleClasses(heading, "heading-accent");

    headerAvatar.setFitWidth(40);
    headerAvatar.setFitHeight(40);
    headerAvatar.setPreserveRatio(true);
    headerAvatar.setSmooth(true);

    ThemeStyles.addStyleClasses(sessionSummaryLabel, "muted-text");

    Button profileButton = new Button("Profile");
    Button logoutButton = new Button("Log Out");
    ThemeStyles.styleButton(profileButton);
    ThemeStyles.styleButton(logoutButton);

    profileButton.setOnAction(_ -> onProfile.run());
    logoutButton.setOnAction(_ -> onLogout.run());

    Button themeToggleButton = new Button();
    themeToggleButton.setOnAction(_ -> onThemeToggle.run());
    themeToggleButton.textProperty().bind(
        Bindings.createStringBinding(
            () -> ThemeManager.getInstance().getTheme() == ThemeManager.Theme.DARK
                ? "Light Mode"
                : "Dark Mode",
            ThemeManager.getInstance().themeProperty()));
    ThemeStyles.styleButton(themeToggleButton);

    Label daysLabel = new Label("Days:");
    ThemeStyles.addStyleClasses(daysLabel, "muted-text");
    TextField daysField = new TextField("1");
    daysField.setPromptText("1");
    daysField.setPrefWidth(60);
    ThemeStyles.styleField(daysField);
    Button skipDaysButton = new Button("Skip trading days");
    ThemeStyles.styleButton(skipDaysButton);
    skipDaysButton.setOnAction(_ -> {
      try {
        int days =
            Validator.parsePositiveInt(
                daysField.getText(),
                "trading days",
                WorkspaceController.MAX_SKIP_TRADING_DAYS);
        onSkipTradingDays.accept(days);
      } catch (IllegalArgumentException ex) {
        notifications.show(ToastMode.WARNING, "Invalid days", ex.getMessage());
      }
    });
    HBox skipDaysBox = new HBox(8, daysLabel, daysField, skipDaysButton);
    skipDaysBox.setAlignment(Pos.CENTER_LEFT);
    ThemeStyles.addStyleClasses(skipDaysBox, "workspace-skip-days");

    HBox actions =
        new HBox(10, profileButton, themeToggleButton, logoutButton);
    actions.setAlignment(Pos.CENTER_RIGHT);
    ThemeStyles.addStyleClasses(actions, "workspace-actions");

    HBox topRow =
        new HBox(16, heading, headerAvatar, sessionSummaryLabel, skipDaysBox, actions);
    topRow.setAlignment(Pos.CENTER_LEFT);
    ThemeStyles.addStyleClasses(topRow, "workspace-header");
    HBox.setMargin(actions, new Insets(0, 0, 0, 16));

    VBox center = new VBox(14, topRow, tabs);
    content.setCenter(center);

    ToastTray tray = new ToastTray(notifications.getItems());
    StackPane toastArea = new StackPane(tray);
    toastArea.setAlignment(Pos.TOP_RIGHT);
    toastArea.setPadding(new Insets(16, 16, 0, 0));
    toastArea.setMouseTransparent(true);
    ThemeStyles.addStyleClasses(toastArea, "toast-overlay");

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

  @Override
  public void onWindowResized(double width, double height) {
    boolean showSummary = width >= 950;
    sessionSummaryLabel.setVisible(showSummary);
    sessionSummaryLabel.setManaged(showSummary);
    for (Tab tab : tabs.getTabs()) {
      if (tab.getContent() instanceof ResponsiveLayout layout) {
        layout.onWindowResized(width, height);
      }
    }
  }
}
