package view.pages.auth;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import view.layout.AuthLayout;
import view.theme.ThemePalette;
import view.theme.ThemeStyles;

/**
 * Registration page for the authentication flow.
 */
public class RegisterPage extends AuthLayout {

  private final TextField usernameField = new TextField();
  private final PasswordField pinField = new PasswordField();
  private final TextField startingMoneyField = new TextField();

  /**
   * @param registerAction registration submit handler
   * @param toLogin navigate to login
   * @param sidePanel optional leaderboard panel
   * @param helpAction optional help handler
   * @param showReturnToSession show return button
   * @param returnAction return to active session
   */
  public RegisterPage(
      RegisterAction registerAction,
      Runnable toLogin,
      Node sidePanel,
      Runnable helpAction,
      boolean showReturnToSession,
      Runnable returnAction) {
    super(buildFormShell(), "Login", toLogin, sidePanel, helpAction, showReturnToSession, returnAction);
    VBox form = (VBox) getContentSlot().getChildren().get(0);
    wireForm(form, registerAction);
  }

  private static VBox buildFormShell() {
    return new VBox(16);
  }

  private void wireForm(VBox form, RegisterAction registerAction) {
    Label heading = new Label("Create your profile");
    heading.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + ThemePalette.TEXT_PRIMARY + ";");

    Label subheading = new Label("Choose a username, PIN, and starting balance.");
    subheading.setStyle("-fx-font-size: 13px; -fx-text-fill: " + ThemePalette.TEXT_SECONDARY + ";");

    usernameField.setPromptText("Username");
    pinField.setPromptText("PIN (4–8 digits)");
    startingMoneyField.setPromptText("Starting money");
    ThemeStyles.styleField(usernameField);
    ThemeStyles.styleField(pinField);
    ThemeStyles.styleField(startingMoneyField);
    usernameField.setMaxWidth(Double.MAX_VALUE);
    pinField.setMaxWidth(Double.MAX_VALUE);
    startingMoneyField.setMaxWidth(Double.MAX_VALUE);

    Button registerButton = new Button("Create profile");
    registerButton.setMaxWidth(Double.MAX_VALUE);
    ThemeStyles.styleAccentButton(registerButton);
    registerButton.setOnAction(
        _ ->
            registerAction.run(
                usernameField.getText(), pinField.getText(), startingMoneyField.getText()));

    VBox fields = new VBox(10, usernameField, pinField, startingMoneyField);
    fields.setMaxWidth(360);

    VBox content = new VBox(8, heading, subheading, new VBox(12, fields, registerButton));
    form.setAlignment(Pos.CENTER);
    form.setMaxWidth(360);
    form.setPadding(new Insets(0, 32, 0, 32));
    form.getChildren().setAll(content);
  }

  public void setStatus(String message) {
    super.setStatus(message);
  }

  public String getStatus() {
    return super.getStatus();
  }

  public void setValues(String username, String pin, String startingMoney) {
    usernameField.setText(username);
    pinField.setText(pin);
    startingMoneyField.setText(startingMoney);
  }

  @FunctionalInterface
  public interface RegisterAction {
    void run(String username, String pin, String startingMoney);
  }
}
