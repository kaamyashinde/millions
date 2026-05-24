package view.layout;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import view.theme.ThemeStyles;

/**
 * Split-screen layout shell for authentication pages.
 *
 * <p>Divides the screen 50/50 horizontally: a decorative lavender left panel with the
 * application brand, and a white right panel containing an injected form node centered
 * vertically with a navigation link in the top-right corner.
 *
 * <p>This class holds no authentication logic. Login and Register pages each construct
 * their own {@code AuthLayout}, supplying their form content and a nav-link callback.
 */
public class AuthLayout extends HBox implements ResponsiveLayout {

  private final StackPane contentSlot;
  private final VBox leftPanel;
  private final Label statusLabel = new Label();
  private final Button returnButton = new Button("Back to current session");

  /**
   * Builds the split-screen authentication layout shell.
   *
   * @param formContent   the form node to display centered in the right panel
   * @param navLinkText   label text for the navigation link in the top-right corner
   * @param navLinkAction callback invoked when the navigation link is clicked
   */
  public AuthLayout(Node formContent, String navLinkText, Runnable navLinkAction) {
    this(formContent, navLinkText, navLinkAction, null, false, null);
  }

  /**
   * Builds auth layout with optional side content and footer actions.
   *
   * @param formContent form node centered in the right panel
   * @param navLinkText navigation link label in the top-right corner
   * @param navLinkAction invoked when the navigation link is clicked
   * @param sidePanel optional panel displayed beside the form
   * @param showReturnToSession whether to show the return-to-session button
   * @param returnAction invoked when return-to-session is clicked
   */
  public AuthLayout(
      Node formContent,
      String navLinkText,
      Runnable navLinkAction,
      Node sidePanel,
      boolean showReturnToSession,
      Runnable returnAction) {
    ThemeStyles.addStyleClasses(this, "auth-root");
    contentSlot = new StackPane(formContent);
    contentSlot.setAlignment(Pos.CENTER);

    VBox left = buildLeftPanel();
    this.leftPanel = left;
    BorderPane right = buildRightPanel(navLinkText, navLinkAction, sidePanel);

    returnButton.setVisible(showReturnToSession);
    returnButton.setManaged(showReturnToSession);
    ThemeStyles.styleButton(returnButton);
    if (returnAction != null) {
      returnButton.setOnAction(_ -> returnAction.run());
    }

    statusLabel.setWrapText(true);
    statusLabel.setMaxWidth(400);
    ThemeStyles.addStyleClasses(statusLabel, "auth-status-error");

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    HBox bottom = new HBox(12, statusLabel, spacer, returnButton);
    bottom.setAlignment(Pos.CENTER_LEFT);
    bottom.setPadding(new Insets(16, 32, 24, 32));
    right.setBottom(bottom);

    getChildren().addAll(left, right);
  }

  /**
   * Returns the content slot that holds the injected form node.
   *
   * @return the central content StackPane in the right panel
   */
  public StackPane getContentSlot() {
    return contentSlot;
  }

  /**
   * Updates the footer status message.
   *
   * @param message status text
   */
  public void setStatus(String message) {
    statusLabel.setText(message);
  }

  /**
   * Returns the footer status message.
   *
   * @return current status text
   */
  public String getStatus() {
    return statusLabel.getText();
  }

  /**
   * Fires the return-to-session action when that button is visible.
   */
  public void triggerReturnToSession() {
    if (returnButton.isManaged()) {
      returnButton.fire();
    }
  }

  @Override
  public void onWindowResized(double width, double height) {
    boolean showLeft = width >= 800;
    leftPanel.setVisible(showLeft);
    leftPanel.setManaged(showLeft);
  }

  private VBox buildLeftPanel() {
    VBox panel = new VBox();
    ThemeStyles.addStyleClasses(panel, "auth-side-panel");
    panel.setMaxWidth(Double.MAX_VALUE);
    panel.setMinWidth(300);
    HBox.setHgrow(panel, Priority.ALWAYS);

    HBox brand = buildBrandBox();
    VBox.setMargin(brand, new Insets(32, 0, 0, 32));
    panel.getChildren().add(brand);

    return panel;
  }

  private HBox buildBrandBox() {
    StackPane icon = new StackPane();
    icon.setPrefSize(32, 32);
    icon.setMinSize(32, 32);
    icon.setMaxSize(32, 32);
    ThemeStyles.addStyleClasses(icon, "auth-brand-icon");

    Label iconLetter = new Label("M");
    ThemeStyles.addStyleClasses(iconLetter, "auth-brand-letter");
    icon.getChildren().add(iconLetter);

    Label brandLabel = new Label("Millions");
    ThemeStyles.addStyleClasses(brandLabel, "auth-brand-label");

    HBox brand = new HBox(10, icon, brandLabel);
    brand.setAlignment(Pos.CENTER_LEFT);
    return brand;
  }

  private BorderPane buildRightPanel(String navLinkText, Runnable navLinkAction, Node sidePanel) {
    BorderPane panel = new BorderPane();
    ThemeStyles.addStyleClasses(panel, "auth-main-panel");
    panel.setMaxWidth(Double.MAX_VALUE);
    panel.setMinWidth(300);
    HBox.setHgrow(panel, Priority.ALWAYS);

    Label navLinkLabel = new Label(navLinkText);
    ThemeStyles.addStyleClasses(navLinkLabel, "auth-nav-link");
    navLinkLabel.setOnMouseClicked(_ -> navLinkAction.run());

    HBox navRow = new HBox(navLinkLabel);
    navRow.setAlignment(Pos.CENTER_RIGHT);
    navRow.setPadding(new Insets(24, 32, 0, 0));
    panel.setTop(navRow);

    if (sidePanel != null) {
      HBox centerRow = new HBox(24, contentSlot, sidePanel);
      centerRow.setAlignment(Pos.CENTER);
      HBox.setHgrow(contentSlot, Priority.ALWAYS);
      if (sidePanel instanceof Region region) {
        region.setMaxWidth(440);
      }
      panel.setCenter(centerRow);
    } else {
      panel.setCenter(contentSlot);
    }

    return panel;
  }
}
