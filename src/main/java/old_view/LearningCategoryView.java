package old_view;

import java.util.List;
import java.util.function.Consumer;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import model.learninghub.Difficulty;
import model.learninghub.LearningCategory;
import model.learninghub.LearningContentStore;
import model.learninghub.LearningItem;

/**
 * Category view for the Learning Hub. Shows all {@link LearningItem}s in a single
 * {@link LearningCategory} as clickable cards, with difficulty filter toggle buttons.
 *
 * @author kaamyashinde
 * @version 1.1.0
 * @since 04-04-2026
 */
public class LearningCategoryView extends BorderPane {

  private static final String COLOR_BG_CARD = "#1e1e1e";
  private static final String COLOR_BORDER_ACCENT = "#2196F3";
  private static final String COLOR_HEADING = "#e0e0e0";
  private static final String COLOR_SUBTITLE = "#9e9e9e";
  private static final String COLOR_DIFFICULTY_BEGINNER = "#4CAF50";
  private static final String COLOR_DIFFICULTY_INTERMEDIATE = "#FFA500";
  private static final String COLOR_DIFFICULTY_ADVANCED = "#FF4444";

  private final VBox content = new VBox(12);
  private List<LearningItem> items;
  private Consumer<LearningItem> onItemClicked;

  /**
   * Builds the category view.
   *
   * @param category      the category whose items are displayed
   * @param onBack        called when the back button is clicked
   * @param onItemClicked called with the clicked item
   */
  public LearningCategoryView(
      LearningCategory category, Runnable onBack, Consumer<LearningItem> onItemClicked) {
    setPadding(new Insets(16));

    this.items = LearningContentStore.getItemsByCategory(category);
    this.onItemClicked = onItemClicked;

    // ── TOP: back button + category header ───────────────────────────────────
    Button backBtn = new Button("← Back");
    backBtn.setStyle("-fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;");
    backBtn.setOnAction(_ -> onBack.run());

    Label emoji = new Label(category.emoji());
    emoji.setStyle("-fx-font-size: 22;");

    Label name = new Label(category.name());
    name.setFont(Font.font("System", FontWeight.BOLD, 20));
    name.setStyle("-fx-text-fill: " + COLOR_HEADING + ";");

    Label desc = new Label(category.description());
    desc.setStyle("-fx-text-fill: " + COLOR_SUBTITLE + "; -fx-font-size: 12;");
    desc.setWrapText(true);

    // ── Filter bar ───────────────────────────────────────────────────────────
    ToggleGroup filterGroup = new ToggleGroup();
    ToggleButton btnAll = makeFilterToggle("All", null, filterGroup);
    ToggleButton btnBeginner = makeFilterToggle("Beginner", Difficulty.BEGINNER, filterGroup);
    ToggleButton btnIntermediate = makeFilterToggle("Intermediate", Difficulty.INTERMEDIATE, filterGroup);
    ToggleButton btnAdvanced = makeFilterToggle("Advanced", Difficulty.ADVANCED, filterGroup);
    btnAll.setSelected(true);

    HBox filterBar = new HBox(8, btnAll, btnBeginner, btnIntermediate, btnAdvanced);
    filterBar.setPadding(new Insets(0, 0, 8, 0));

    VBox header = new VBox(4, backBtn, emoji, name, desc, filterBar);
    header.setPadding(new Insets(0, 0, 16, 0));
    setTop(header);

    // ── CENTER: item cards ───────────────────────────────────────────────────
    content.setPadding(new Insets(4, 0, 16, 0));
    applyFilter(null);

    ScrollPane scroll = new ScrollPane(content);
    scroll.setFitToWidth(true);
    scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
    scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    setCenter(scroll);
  }

  private ToggleButton makeFilterToggle(String label, Difficulty difficulty, ToggleGroup group) {
    ToggleButton btn = new ToggleButton(label);
    btn.setToggleGroup(group);
    btn.setStyle(
        "-fx-background-radius: 4;"
        + "-fx-border-radius: 4;"
        + "-fx-cursor: hand;"
        + "-fx-font-size: 11;");
    btn.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
      if (isNowSelected) {
        applyFilter(difficulty);
      }
      if (!isNowSelected && group.getSelectedToggle() == null) {
        btn.setSelected(true);
      }
    });
    return btn;
  }

  private void applyFilter(Difficulty difficulty) {
    content.getChildren().clear();
    List<LearningItem> filtered = (difficulty == null)
        ? items
        : items.stream().filter(i -> i.difficulty() == difficulty).toList();
    if (filtered.isEmpty()) {
      Label empty = new Label("No " + difficulty.name().toLowerCase() + " topics in this category.");
      empty.setStyle("-fx-text-fill: " + COLOR_SUBTITLE + ";");
      content.getChildren().add(empty);
    } else {
      for (LearningItem item : filtered) {
        content.getChildren().add(buildItemCard(item, onItemClicked));
      }
    }
  }

  private static javafx.scene.Node buildItemCard(LearningItem item, Consumer<LearningItem> onItemClicked) {
    String badgeColor = difficultyColor(item.difficulty());

    Label badge = new Label(item.difficulty().name());
    badge.setStyle(
        "-fx-background-color: " + badgeColor + "22;"
            + "-fx-text-fill: " + badgeColor + ";"
            + "-fx-background-radius: 4;"
            + "-fx-padding: 2 6 2 6;"
            + "-fx-font-size: 10;");

    Label title = new Label(item.title());
    title.setStyle("-fx-text-fill: " + COLOR_HEADING + "; -fx-font-weight: bold;");
    title.setWrapText(true);

    Label summary = new Label(item.summary());
    summary.setStyle("-fx-text-fill: " + COLOR_SUBTITLE + "; -fx-font-size: 11;");
    summary.setWrapText(true);

    VBox card = new VBox(6, badge, title, summary);
    card.setPadding(new Insets(12));
    card.setMaxWidth(Double.MAX_VALUE);
    card.setStyle(
        "-fx-background-color: " + COLOR_BG_CARD + ";"
            + "-fx-border-color: " + COLOR_BORDER_ACCENT + ";"
            + "-fx-border-radius: 8;"
            + "-fx-background-radius: 8;"
            + "-fx-cursor: hand;");

    card.setOnMouseClicked(_ -> onItemClicked.accept(item));
    return card;
  }

  private static String difficultyColor(Difficulty difficulty) {
    return switch (difficulty) {
      case BEGINNER -> COLOR_DIFFICULTY_BEGINNER;
      case INTERMEDIATE -> COLOR_DIFFICULTY_INTERMEDIATE;
      case ADVANCED -> COLOR_DIFFICULTY_ADVANCED;
    };
  }
}
