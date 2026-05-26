package view.pages.learning;

import java.util.List;
import java.util.function.Consumer;

import controller.LearningHubController;
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
import model.learning.content.Difficulty;
import model.learning.content.LearningCategory;
import model.learning.content.LearningItem;
import view.theme.ThemeStyles;

/**
 * Category view for the Learning Hub. Shows all {@link LearningItem}s in a single
 * {@link LearningCategory} as clickable cards, with difficulty filter toggle buttons.
 *
 * @author kaamyashinde
 * @version 2.0.0
 * @since 2026-04-04
 */
public class CategoryView extends BorderPane {

  private final VBox content = new VBox(12);
  private List<LearningItem> items;
  private Consumer<LearningItem> onItemClicked;

  /**
   * Builds the category view.
   *
   * @param learningHub   supplies items for the category
   * @param category      the category whose items are displayed
   * @param onBack        called when the back button is clicked
   * @param onItemClicked called with the clicked item
   */
  public CategoryView(
      LearningHubController learningHub,
      LearningCategory category,
      Runnable onBack,
      Consumer<LearningItem> onItemClicked) {
    setPadding(new Insets(16));
    ThemeStyles.addStyleClasses(this, "learning-root");

    this.items = learningHub.getItemsForCategory(category);
    this.onItemClicked = onItemClicked;

    Button backBtn = new Button("← Back");
    ThemeStyles.styleButton(backBtn);
    backBtn.setOnAction(_ -> onBack.run());

    Label emoji = new Label(category.emoji());
    ThemeStyles.addStyleClasses(emoji, "learning-card-emoji");

    Label name = new Label(category.name());
    name.setFont(Font.font("System", FontWeight.BOLD, 20));
    ThemeStyles.addStyleClasses(name, "learning-card-title");

    Label desc = new Label(category.description());
    ThemeStyles.addStyleClasses(desc, "learning-card-summary");
    desc.setWrapText(true);

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

    content.setPadding(new Insets(4, 0, 16, 0));
    applyFilter(null);

    ScrollPane scroll = new ScrollPane(content);
    scroll.setFitToWidth(true);
    scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    setCenter(scroll);
  }

  private ToggleButton makeFilterToggle(String label, Difficulty difficulty, ToggleGroup group) {
    ToggleButton btn = new ToggleButton(label);
    btn.setToggleGroup(group);
    ThemeStyles.addStyleClasses(btn, "btn", "btn-secondary");
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
      ThemeStyles.addStyleClasses(empty, "empty-state");
      content.getChildren().add(empty);
    } else {
      content.getChildren().addAll(filtered.stream()
          .map(item -> buildItemCard(item, onItemClicked))
          .toList());
    }
  }

  private static javafx.scene.Node buildItemCard(LearningItem item, Consumer<LearningItem> onItemClicked) {
    Label badge = new Label(item.difficulty().name());
    ThemeStyles.applyDifficultyBadge(badge, item.difficulty());

    Label title = new Label(item.title());
    ThemeStyles.addStyleClasses(title, "learning-card-title");
    title.setWrapText(true);

    Label summary = new Label(item.summary());
    ThemeStyles.addStyleClasses(summary, "learning-card-summary");
    summary.setWrapText(true);

    VBox card = new VBox(6, badge, title, summary);
    card.setPadding(new Insets(12));
    card.setMaxWidth(Double.MAX_VALUE);
    ThemeStyles.addStyleClasses(card, "learning-card-accent");

    card.setOnMouseClicked(_ -> onItemClicked.accept(item));
    return card;
  }
}
