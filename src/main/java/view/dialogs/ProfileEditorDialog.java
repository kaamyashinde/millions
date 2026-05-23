package view.dialogs;

import java.nio.file.Path;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import controller.ExitGameController;
import controller.ProfileEditorController;
import model.session.ActiveSession;
import model.session.SessionService;
import util.I18n;
import view.components.image.FileImageLoader;
import view.components.image.ImageLoader;
import view.components.image.ValidatingImageLoader;
import view.theme.ThemePalette;
import view.theme.ThemeStyles;

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
      ExitGameController exitGame,
      Runnable onSaved,
      Runnable onAccountDeleted) {
    show(owner, controller.getSessionService(), exitGame, onSaved, onAccountDeleted);
  }

  /**
   * Shows the profile editor for the active session.
   *
   * @param owner parent window
   * @param sessionService session service
   * @param exitGame exit-game controller for profile deletion
   * @param onSaved invoked after a successful save
   * @param onAccountDeleted invoked after the current profile was deleted
   */
  public static void show(
      Window owner,
      SessionService sessionService,
      ExitGameController exitGame,
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
    preview.setStyle("-fx-border-color: " + ThemePalette.BORDER + "; -fx-border-radius: 8;");

    Path[] pendingImage = {null};
    boolean[] removeAvatar = {false};

    ImageLoader avatarLoader = new ValidatingImageLoader(new FileImageLoader());

    Runnable reloadPreview = () -> {
      if (pendingImage[0] != null) {
        preview.setImage(avatarLoader.load(pendingImage[0], 96));
        if (preview.getImage() != null) {
          return;
        }
      }
      if (removeAvatar[0]) {
        preview.setImage(null);
        return;
      }
      Path avatarPath = sessionService.avatarPath(session.normalizedUsername());
      preview.setImage(avatarLoader.load(avatarPath, 96));
    };
    reloadPreview.run();

    Label status = new Label();
    status.setStyle("-fx-text-fill: " + ThemePalette.ERROR + ";");

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

    Label danger = new Label(I18n.get("exitGame.pin.confirm"));
    danger.setStyle("-fx-font-weight: bold;");

    Label dangerHint = new Label(I18n.get("exitGame.confirm.body"));
    dangerHint.setWrapText(true);
    ThemeStyles.addStyleClasses(dangerHint, "text-secondary");

    Button exitGameButton = new Button(I18n.get("exitGame.pin.confirm"));
    exitGameButton.setStyle("-fx-text-fill: " + ThemePalette.ERROR + ";");
    exitGameButton.setOnAction(_ -> {
      stage.close();
      ExitGameDialog.show(stage.getOwner(), exitGame, onAccountDeleted);
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
        dangerHint,
        exitGameButton);
    root.setPadding(new Insets(16));
    root.setAlignment(Pos.TOP_LEFT);

    Scene scene = new Scene(root, 420, 460);
    ThemeStyles.install(scene);
    ThemeStyles.addStyleClasses(root, "dialog-root");
    ThemeStyles.styleField(nameField);
    ThemeStyles.styleButton(chooseImage);
    ThemeStyles.styleButton(removeImage);
    ThemeStyles.styleAccentButton(save);
    ThemeStyles.styleButton(cancel);
    ThemeStyles.styleButton(exitGameButton);
    stage.setScene(scene);
    stage.showAndWait();
  }
}
