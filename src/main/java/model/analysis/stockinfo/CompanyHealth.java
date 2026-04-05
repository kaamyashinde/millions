package model.analysis.stockinfo;

/**
 * Qualitative label for mock company financial health shown in stock views.
 */
public enum CompanyHealth {

  /**
   * Healthy mock fundamentals (sufficient profit margin).
   */
  STRONG("Strong"),

  /**
   * Stressed mock fundamentals (low or negative margin).
   */
  WEAK("Weak");

  private final String displayLabel;

  CompanyHealth(String displayLabel) {
    this.displayLabel = displayLabel;
  }

  /**
   * Returns the short label shown in tables and detail panels.
   *
   * @return user-facing health text
   */
  public String displayLabel() {
    return displayLabel;
  }
}
