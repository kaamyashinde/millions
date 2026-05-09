package view.dialogs;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import controller.ProfileEditorController;
import model.session.ActiveSession;
import model.session.AuthenticationException;
import model.session.SessionService;

/**
 * Modal editor for display name, avatar image, and profile deletion.
 */
public final class ProfileEditorDialog {

  private ProfileEditorDialog() {
  }

  /**
   * Shows the profile editor for the active session.
   *
   * @param owner parent window
   * @param sessionService session service
   * @param onSaved invoked after a successful save (e.g. refresh UI)
   * @param onAccountDeleted invoked after the current profile was deleted
   */
  public static void show(
      Window owner,
      ProfileEditorController controller,
      Runnable onSaved,
      Runnable onAccountDeleted) {
    show(owner, controller.getSessionService(), onSaved, onAccountDeleted);
  }

  /**
   * Shows the profile editor for the active session.
   *
   * @param owner parent window
   * @param sessionService session service
   * @param onSaved invoked after a successful save
   * @param onAccountDeleted invoked after the current profile was deleted
   */
  public static void show(
      Window owner,
      SessionService sessionService,
      Runnable onSaved,
      Runnable onAccountDeleted) {
    ActiveSession session = sessionService.getActiveSession().orElseThrow();
    Stage stage = new Stage();
    stage.initOwner(owner);
    stage.initModality(Modality.WINDOW_MODAL);
    stage.setTitle("Profile");

    TextField nameField = new TextField(session.player().getName());
    nameField.setPromptText("Display name");

    ImageView preview = new ImageView();
    preview.setFitWidth(96);
    preview.setFitHeight(96);
    preview.setPreserveRatio(true);
    preview.setSmooth(true);
    preview.setStyle("-fx-border-color: #d7dce5; -fx-border-radius: 8;");

    Path[] pendingImage = {null};
    boolean[] removeAvatar = {false};

    Runnable reloadPreview = () -> {
      preview.setImage(null);
      try {
        if (pendingImage[0] != null && Files.isRegularFile(pendingImage[0])) {
          try (InputStream in = Files.newInputStream(pendingImage[0])) {
            preview.setImage(new Image(in, 96, 96, true, true));
          }
          return;
        }
        if (removeAvatar[0]) {
          return;
        }
        Path avatarPath = sessionService.avatarPath(session.normalizedUsername());
        if (Files.isRegularFile(avatarPath)) {
          try (InputStream in = Files.newInputStream(avatarPath)) {
            preview.setImage(new Image(in, 96, 96, true, true));
          }
        }
      } catch (IOException exception) {
        preview.setImage(null);
      }
    };
    reloadPreview.run();

    Label status = new Label();
    status.setStyle("-fx-text-fill: #c62828;");

    Button chooseImage = new Button("Choose image…");
    chooseImage.setOnAction(_ -> {
      FileChooser chooser = new FileChooser();
      chooser.setTitle("Profile image");
      chooser.getExtensionFilters().add(
          new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
      java.io.File file = chooser.showOpenDialog(stage);
      if (file != null) {
        pendingImage[0] = file.toPath();
        removeAvatar[0] = false;
        reloadPreview.run();
        status.setText("");
      }
    });

    Button removeImage = new Button("Remove image");
    removeImage.setOnAction(_ -> {
      pendingImage[0] = null;
      removeAvatar[0] = true;
      preview.setImage(null);
      status.setText("");
    });

    Button save = new Button("Save");
    save.setDefaultButton(true);
    save.setOnAction(_ -> {
      status.setText("");
      try {
        sessionService.updateDisplayName(nameField.getText());
        if (removeAvatar[0]) {
          sessionService.clearAvatar();
        } else if (pendingImage[0] != null) {
          sessionService.saveAvatarFromFile(pendingImage[0]);
        }
        onSaved.run();
        stage.close();
      } catch (IllegalArgumentException exception) {
        status.setText(exception.getMessage());
      } catch (RuntimeException exception) {
        status.setText(exception.getMessage() != null ? exception.getMessage() : "Could not save profile.");
      }
    });

    Button cancel = new Button("Cancel");
    cancel.setCancelButton(true);
    cancel.setOnAction(_ -> stage.close());

    Label danger = new Label("Delete this profile");
    danger.setStyle("-fx-font-weight: bold;");

    PasswordField deletePin = new PasswordField();
    deletePin.setPromptText("PIN to confirm delete");

    Button delete = new Button("Delete profile");
    delete.setStyle("-fx-text-fill: #b71c1c;");
    delete.setOnAction(_ -> {
      status.setText("");
      char[] pin = deletePin.getText().toCharArray();
      try {
        sessionService.deleteActiveProfile(pin);
        java.util.Arrays.fill(pin, '0');
        stage.close();
        onAccountDeleted.run();
      } catch (AuthenticationException exception) {
        java.util.Arrays.fill(pin, '0');
        status.setText("Invalid PIN.");
      } catch (RuntimeException exception) {
        java.util.Arrays.fill(pin, '0');
        status.setText(
            exception.getMessage() != null ? exception.getMessage() : "Could not delete profile.");
      }
    });

    GridPane form = new GridPane();
    form.setHgap(10);
    form.setVgap(8);
    form.addRow(0, new Label("Display name"), nameField);
    form.addRow(1, new Label("Photo"), preview);

    HBox imageActions = new HBox(8, chooseImage, removeImage);
    VBox root = new VBox(14,
        form,
        imageActions,
        status,
        new HBox(10, save, cancel),
        danger,
        deletePin,
        delete);
    root.setPadding(new Insets(16));
    root.setAlignment(Pos.TOP_LEFT);

    stage.setScene(new Scene(root, 420, 460));
    stage.showAndWait();
  }
}
