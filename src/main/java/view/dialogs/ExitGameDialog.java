package view.dialogs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import controller.ExitGameController;
import model.exception.auth.AuthenticationException;
import model.exception.auth.RegistrationValidationException;
import util.I18n;
import view.theme.ThemeStyles;

/**
 * Confirms exit-game intent, collects PIN, and deletes the profile after liquidating holdings.
 */
public final class ExitGameDialog {

  private ExitGameDialog() {}

  /**
   * Shows confirmation, then PIN entry, then runs exit-game deletion.
   *
   * @param owner parent window
   * @param controller exit-game controller
   * @param onDeleted invoked after successful profile deletion
   */
  public static void show(Window owner, ExitGameController controller, Runnable onDeleted) {
    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
    confirm.setTitle(I18n.get("exitGame.confirm.title"));
    confirm.setHeaderText(I18n.get("exitGame.confirm.header"));
    String body = I18n.get("exitGame.confirm.body");
    if (controller.hasHoldings()) {
      body = body + "\n\n" + I18n.format("exitGame.confirm.holdings", controller.countHeldSymbols());
    }
    confirm.setContentText(body);
    ThemeStyles.installOnDialog(confirm);
    if (confirm.showAndWait().filter(r -> r == ButtonType.OK).isEmpty()) {
      return;
    }
    showPinDialog(owner, controller, onDeleted);
  }

  private static void showPinDialog(Window owner, ExitGameController controller, Runnable onDeleted) {
    Stage stage = new Stage();
    stage.initOwner(owner);
    stage.initModality(Modality.WINDOW_MODAL);
    stage.setTitle(I18n.get("exitGame.pin.title"));

    Label intro = new Label(I18n.get("exitGame.pin.intro"));
    intro.setWrapText(true);

    PasswordField pinField = new PasswordField();
    pinField.setPromptText(I18n.get("exitGame.pin.prompt"));

    Label status = new Label();
    ThemeStyles.addStyleClasses(status, "text-error");

    Button confirm = new Button(I18n.get("exitGame.pin.confirm"));
    confirm.setDefaultButton(true);
    ThemeStyles.addStyleClasses(confirm, "text-error");
    confirm.setOnAction(_ -> {
      status.setText("");
      char[] pin = pinField.getText().toCharArray();
      try {
        controller.exitGameAndDeleteProfile(pin);
        java.util.Arrays.fill(pin, '0');
        stage.close();
        onDeleted.run();
      } catch (AuthenticationException exception) {
        java.util.Arrays.fill(pin, '0');
        status.setText(I18n.get("exitGame.invalidPin"));
      } catch (RegistrationValidationException exception) {
        java.util.Arrays.fill(pin, '0');
        status.setText(I18n.get("auth.invalidPin"));
      } catch (RuntimeException exception) {
        java.util.Arrays.fill(pin, '0');
        status.setText(
            exception.getMessage() != null ? exception.getMessage() : I18n.get("exitGame.failed"));
      }
    });

    Button cancel = new Button(I18n.get("exitGame.pin.cancel"));
    cancel.setCancelButton(true);
    cancel.setOnAction(_ -> stage.close());

    VBox root = new VBox(12, intro, pinField, status, confirm, cancel);
    root.setPadding(new Insets(16));
    root.setAlignment(Pos.TOP_LEFT);

    Scene scene = new Scene(root, 400, 200);
    ThemeStyles.install(scene);
    ThemeStyles.addStyleClasses(root, "dialog-root");
    ThemeStyles.styleField(pinField);
    ThemeStyles.styleButton(confirm);
    ThemeStyles.styleButton(cancel);
    stage.setScene(scene);
    stage.showAndWait();
  }
}
