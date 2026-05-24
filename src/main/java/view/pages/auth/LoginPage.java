package view.pages.auth;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.VBox;
import java.util.Optional;
import java.util.function.Function;
import view.layout.AuthLayout;
import view.theme.ThemeStyles;
import view.validation.AuthFormValidation;

/**
 * Login page for the authentication flow.
 */
public class LoginPage extends AuthLayout {

  private final TextField usernameField = new TextField();
  private final PasswordField pinField = new PasswordField();

  /**
   * @param loginAction login submit handler
   * @param toRegister navigate to register
   */
  public LoginPage(LoginAction loginAction, Runnable toRegister) {
    this(loginAction, toRegister, null, null, false, null);
  }

  /**
   * @param loginAction login submit handler
   * @param toRegister navigate to register
   * @param sidePanel optional leaderboard panel
   * @param helpAction optional help handler
   * @param showReturnToSession show return button
   * @param returnAction return to active session
   */
  public LoginPage(
      LoginAction loginAction,
      Runnable toRegister,
      Node sidePanel,
      Runnable helpAction,
      boolean showReturnToSession,
      Runnable returnAction) {
    super(
        buildFormShell(),
        "Register",
        toRegister,
        sidePanel,
        helpAction,
        showReturnToSession,
        returnAction);
    VBox form = (VBox) getContentSlot().getChildren().get(0);
    wireForm(form, loginAction);
  }

  private static VBox buildFormShell() {
    VBox form = new VBox(16);
    ThemeStyles.addStyleClasses(form, "auth-form");
    return form;
  }

  private void wireForm(VBox form, LoginAction loginAction) {
    Label heading = new Label("Welcome Back");
    ThemeStyles.addStyleClasses(heading, "heading-lg");

    Label subheading = new Label("Enter your username and PIN to log in.");
    ThemeStyles.addStyleClasses(subheading, "text-subheading");

    usernameField.setPromptText("Username");
    ThemeStyles.styleField(usernameField);
    usernameField.setMaxWidth(Double.MAX_VALUE);

    pinField.setPromptText("PIN (4–8 digits)");
    ThemeStyles.styleField(pinField);
    pinField.setMaxWidth(Double.MAX_VALUE);
    AuthFormValidation.restrictPinInput(pinField);

    Label usernameErrorLabel = createFieldErrorLabel();
    Label pinErrorLabel = createFieldErrorLabel();
    wireLiveValidation(usernameField, usernameErrorLabel, AuthFormValidation::usernameError);
    wireLiveValidation(pinField, pinErrorLabel, AuthFormValidation::pinError);

    Button loginButton = new Button("Log In");
    loginButton.setMaxWidth(Double.MAX_VALUE);
    ThemeStyles.styleAccentButton(loginButton);
    loginButton.setOnAction(_ -> loginAction.run(usernameField.getText(), pinField.getText()));
    loginButton.disableProperty().bind(
        Bindings.createBooleanBinding(
            () -> AuthFormValidation.usernameError(usernameField.getText()).isPresent()
                || AuthFormValidation.pinError(pinField.getText()).isPresent()
                || usernameField.getText().isBlank()
                || pinField.getText().isBlank(),
            usernameField.textProperty(),
            pinField.textProperty()));

    VBox usernameRow = new VBox(4, usernameField, usernameErrorLabel);
    VBox pinRow = new VBox(4, pinField, pinErrorLabel);
    VBox fields = new VBox(10, usernameRow, pinRow);
    fields.setMaxWidth(360);
    ThemeStyles.addStyleClasses(fields, "auth-form-fields");

    VBox content = new VBox(8, heading, subheading, new VBox(12, fields, loginButton));
    ThemeStyles.addStyleClasses(content, "auth-form-content");
    form.setAlignment(Pos.CENTER);
    form.setMaxWidth(360);
    form.setPadding(new Insets(0, 32, 0, 32));
    form.getChildren().setAll(content);
  }

  /**
   * @param message status message shown in the auth footer
   */
  public void setStatus(String message) {
    super.setStatus(message);
  }

  public String getStatus() {
    return super.getStatus();
  }

  public void setValues(String username, String pin) {
    usernameField.setText(username);
    pinField.setText(pin);
  }

  private static Label createFieldErrorLabel() {
    Label label = new Label();
    ThemeStyles.addStyleClasses(label, "auth-field-error");
    label.setWrapText(true);
    label.setVisible(false);
    label.setManaged(false);
    return label;
  }

  private static void wireLiveValidation(
      TextInputControl field,
      Label errorLabel,
      Function<String, Optional<String>> validator) {
    field.textProperty().addListener((obs, oldText, text) -> {
      if (text.isBlank()) {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        field.getStyleClass().remove("field-invalid");
        return;
      }
      Optional<String> error = validator.apply(text);
      if (error.isPresent()) {
        errorLabel.setText(error.get());
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        if (!field.getStyleClass().contains("field-invalid")) {
          field.getStyleClass().add("field-invalid");
        }
      } else {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        field.getStyleClass().remove("field-invalid");
      }
    });
  }

  @FunctionalInterface
  public interface LoginAction {
    void run(String username, String pin);
  }
}
