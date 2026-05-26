package view.components.toast;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

/**
 * Enum representing the different modes (severity levels) of a toast notification. Each mode
 * defines its own symbol, icon shape, and optional hex metadata ({@link #getColorHex()}).
 *
 * @author kaamyashinde
 * @version 1.0.0
 * @since 2026-03-29
 */
public enum ToastMode {
  /** Error notification style. */
  ERROR("#EF4444", "i") {
    @Override
    public Shape createShape() {
      double r = ICON_SIZE / 2.0;
      Polygon hex = new Polygon();
      for (int i = 0; i < 6; i++) {
        double angle = Math.toRadians(-30 + 60 * i);
        hex.getPoints().addAll(r * Math.cos(angle), r * Math.sin(angle));
      }
      hex.setFill(Color.TRANSPARENT);
      hex.setStroke(strokeColor());
      hex.setStrokeWidth(STROKE);
      return hex;
    }
  },

  /** Warning notification style. */
  WARNING("#F59E0B", "i") {
    @Override
    public Shape createShape() {
      double h = ICON_SIZE / 2.0;
      Polygon triangle = new Polygon(
          0.0, -h,
          h, h,
          -h, h
      );
      triangle.setFill(Color.TRANSPARENT);
      triangle.setStroke(strokeColor());
      triangle.setStrokeWidth(STROKE);
      return triangle;
    }
  },

  /** Informational notification style. */
  INFO("#0EA5A4", "i") {
    @Override
    public Shape createShape() {
      Rectangle rect = new Rectangle(ICON_SIZE, ICON_SIZE);
      rect.setArcWidth(6);
      rect.setArcHeight(6);
      rect.setFill(Color.TRANSPARENT);
      rect.setStroke(strokeColor());
      rect.setStrokeWidth(STROKE);
      return rect;
    }
  },

  /** Success notification style. */
  SUCCESS("#22C55E", "✓") {
    @Override
    public Shape createShape() {
      Circle circle = new Circle(ICON_SIZE / 2.0);
      circle.setFill(Color.TRANSPARENT);
      circle.setStroke(strokeColor());
      circle.setStrokeWidth(STROKE);
      return circle;
    }
  };

  static final double ICON_SIZE = 40;
  static final double STROKE = 2;

  private final String colorHex;
  private final String symbol;

  ToastMode(String colorHex, String symbol) {
    this.colorHex = colorHex;
    this.symbol = symbol;
  }

  Color strokeColor() {
    return Color.web(colorHex);
  }

  /**
   * Builds and returns the outline shape for this mode with a default stroke.
   *
   * @return the styled shape
   */
  public abstract Shape createShape();

  /**
   * Returns the hex color associated with this toast mode.
   *
   * @return the color hex string for this mode
   */
  public String getColorHex() {
    return colorHex;
  }

  /**
   * Returns the symbol rendered inside this mode's icon.
   *
   * @return the icon symbol for this mode
   */
  public String getSymbol() {
    return symbol;
  }
}
