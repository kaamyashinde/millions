package view;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import view.components.toast.Toast;
import view.components.toast.ToastMode;

/**
 * A small demo application that showcases the Toast notification component. Each button triggers
 * a different severity level. Toasts auto-dismiss after 3 seconds with a fade-out.
 */
public class ToastDemoApp extends Application {

  private final StackPane toastArea = new StackPane();

  @Override
  public void start(Stage stage) {
    Text heading = new Text("Toast Demo");
    heading.setFont(Font.font("System", FontWeight.BOLD, 26));
    heading.setFill(Color.web("#e0e0e0"));

    Button errBtn = makeButton("Error", ToastMode.ERROR,
        "Something went wrong!", "Check the logs for more details.", "Dismiss");
    Button warnBtn = makeButton("Warning", ToastMode.WARNING,
        "Low balance", "Your portfolio is below the threshold.", null);
    Button infoBtn = makeButton("Info", ToastMode.INFO,
        "Market opens soon", "Trading resumes in 30 minutes.", null);
    Button successBtn = makeButton("Success", ToastMode.SUCCESS,
        "Trade executed", "10 AAPL shares purchased.", "View");

    HBox buttons = new HBox(14, errBtn, warnBtn, infoBtn, successBtn);
    buttons.setAlignment(Pos.CENTER);

    VBox center = new VBox(28, heading, buttons);
    center.setAlignment(Pos.CENTER);
    center.setPadding(new Insets(40));

    toastArea.setAlignment(Pos.TOP_RIGHT);
    toastArea.setPadding(new Insets(16, 16, 0, 0));
    toastArea.setMouseTransparent(true);

    StackPane root = new StackPane(center, toastArea);
    root.setStyle("-fx-background-color: #121212;");

    stage.setScene(new Scene(root, 620, 380));
    stage.setTitle("Toast Notification Demo");
    stage.show();
  }

  private Button makeButton(String label, ToastMode mode, String title, String description,
      String actionLabel) {
    Button btn = new Button(label);
    btn.setPrefWidth(110);
    btn.setStyle(
        "-fx-background-color: #2a2a2a;"
            + "-fx-text-fill: " + mode.getColorHex() + ";"
            + "-fx-border-color: " + mode.getColorHex() + ";"
            + "-fx-border-radius: 6;"
            + "-fx-background-radius: 6;"
            + "-fx-cursor: hand;"
    );
    btn.setOnAction(e -> showToast(mode, title, description, actionLabel));
    return btn;
  }

  private void showToast(ToastMode mode, String title, String description, String actionLabel) {
    toastArea.getChildren().clear();

    Toast toast = new Toast(mode, title, description, actionLabel, null);
    toast.setStyle(
        "-fx-background-color: #1e1e1e;"
            + "-fx-background-radius: 8;"
            + "-fx-border-color: " + mode.getColorHex() + ";"
            + "-fx-border-radius: 8;"
            + "-fx-border-width: 1.5;"
    );
    toast.setOpacity(1.0);
    toastArea.getChildren().add(toast);

    PauseTransition pause = new PauseTransition(Duration.seconds(3));
    pause.setOnFinished(ev -> {
      FadeTransition fade = new FadeTransition(Duration.millis(400), toast);
      fade.setFromValue(1.0);
      fade.setToValue(0.0);
      fade.setOnFinished(f -> toastArea.getChildren().clear());
      fade.play();
    });
    pause.play();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
