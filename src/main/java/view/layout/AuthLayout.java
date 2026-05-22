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
import view.theme.ThemePalette;
import view.theme.ThemeStyles;

/**
 * Split-screen layout shell for authentication pages with optional side panel and footer actions.
 */
public class AuthLayout extends HBox {

  private final StackPane contentSlot;
  private final Label statusLabel = new Label();
  private final Button returnButton = new Button("Back to current session");

  /**
   * Builds the split-screen authentication layout shell.
   *
   * @param formContent form node centered in the right panel
   * @param navLinkText navigation link label in the top-right corner
   * @param navLinkAction invoked when the navigation link is clicked
   */
  public AuthLayout(Node formContent, String navLinkText, Runnable navLinkAction) {
    this(formContent, navLinkText, navLinkAction, null, null, false, null);
  }

  /**
   * Builds auth layout with optional leaderboard side panel and footer actions.
   *
   * @param formContent form node
   * @param navLinkText top-right nav link text
   * @param navLinkAction nav link handler
   * @param sidePanel optional panel east of the form (e.g. player leaderboard)
   * @param helpAction optional help handler; {@code null} hides Help
   * @param showReturnToSession whether to show return-to-session button
   * @param returnAction invoked when return is clicked
   */
  public AuthLayout(
      Node formContent,
      String navLinkText,
      Runnable navLinkAction,
      Node sidePanel,
      Runnable helpAction,
      boolean showReturnToSession,
      Runnable returnAction) {
    contentSlot = new StackPane(formContent);
    contentSlot.setAlignment(Pos.CENTER);

    VBox left = buildLeftPanel();
    BorderPane right = buildRightPanel(navLinkText, navLinkAction, sidePanel, helpAction);

    returnButton.setVisible(showReturnToSession);
    returnButton.setManaged(showReturnToSession);
    ThemeStyles.styleButton(returnButton);
    if (returnAction != null) {
      returnButton.setOnAction(_ -> returnAction.run());
    }

    statusLabel.setWrapText(true);
    statusLabel.setStyle("-fx-text-fill: " + ThemePalette.ERROR + "; -fx-font-size: 13px;");
    statusLabel.setMaxWidth(400);

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    Button helpButton = new Button("Help");
    ThemeStyles.styleButton(helpButton);
    helpButton.setVisible(helpAction != null);
    helpButton.setManaged(helpAction != null);
    if (helpAction != null) {
      helpButton.setOnAction(_ -> helpAction.run());
    }

    HBox bottom = new HBox(12, statusLabel, spacer, helpButton, returnButton);
    bottom.setAlignment(Pos.CENTER_LEFT);
    bottom.setPadding(new Insets(16, 32, 24, 32));
    right.setBottom(bottom);

    getChildren().addAll(left, right);
  }

  /**
   * @return content slot holding the form
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
   * @return footer status text
   */
  public String getStatus() {
    return statusLabel.getText();
  }

  /**
   * Simulates pressing the return button when it is visible (for tests).
   */
  public void triggerReturnToSession() {
    if (returnButton.isManaged()) {
      returnButton.fire();
    }
  }

  private VBox buildLeftPanel() {
    VBox panel = new VBox();
    panel.setStyle("-fx-background-color: " + ThemePalette.ACCENT_LIGHT + ";");
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
    icon.setStyle("-fx-background-color: " + ThemePalette.ACCENT + "; -fx-background-radius: 8;");

    Label iconLetter = new Label("M");
    iconLetter.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 16px;");
    icon.getChildren().add(iconLetter);

    Label brandLabel = new Label("Millions");
    brandLabel.setStyle(
        "-fx-text-fill: " + ThemePalette.ACCENT + "; -fx-font-size: 20px; -fx-font-weight: bold;");

    HBox brand = new HBox(10, icon, brandLabel);
    brand.setAlignment(Pos.CENTER_LEFT);
    return brand;
  }

  private BorderPane buildRightPanel(
      String navLinkText, Runnable navLinkAction, Node sidePanel, Runnable helpAction) {
    BorderPane panel = new BorderPane();
    panel.setStyle("-fx-background-color: " + ThemePalette.BACKGROUND + ";");
    panel.setMaxWidth(Double.MAX_VALUE);
    panel.setMinWidth(300);
    HBox.setHgrow(panel, Priority.ALWAYS);

    Label navLinkLabel = new Label(navLinkText);
    navLinkLabel.setStyle(
        "-fx-text-fill: " + ThemePalette.ACCENT + ";"
            + "-fx-underline: true;"
            + "-fx-cursor: hand;"
            + "-fx-font-size: 13px;");
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
