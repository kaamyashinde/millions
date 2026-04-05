package model.core.market.fund;

import java.math.BigDecimal;
import java.util.List;
import model.core.market.InvestableAsset;
import model.utils.Validator;

/**
 * Composite investable asset whose price is derived from weighted underlying stocks.
 */
public class Fund implements InvestableAsset {

  private final String symbol;
  private final String name;
  private final List<FundComponent> components;

  /**
   * Creates a new fund with weighted stock components.
   *
   * @param symbol     unique trading symbol for the fund
   * @param name       display name shown to users
   * @param components weighted stock allocations that define the fund
   */
  public Fund(String symbol, String name, List<FundComponent> components) {
    Validator.checkNotNull(symbol, "Symbol");
    Validator.checkNotNull(name, "Name");
    Validator.checkNotNull(components, "Components");
    if (components.isEmpty()) {
      throw new IllegalArgumentException("Fund must contain at least one component.");
    }
    this.symbol = symbol.toUpperCase();
    this.name = name;
    this.components = List.copyOf(components);
    validateWeights(this.components);
  }

  /**
   * Returns the unique fund symbol.
   *
   * @return uppercase trading symbol
   */
  @Override
  public String getSymbol() {
    return symbol;
  }

  /**
   * Returns the display name for the fund.
   *
   * @return fund name
   */
  @Override
  public String getDisplayName() {
    return name;
  }

  /**
   * Returns the current fund price derived from its weighted holdings.
   *
   * @return weighted current value of all components
   */
  @Override
  public BigDecimal getSalesPrice() {
    return components.stream()
        .map(FundComponent::currentValueContribution)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  /**
   * Returns the asset type label used in listings and portfolio output.
   *
   * @return the string {@code Fund}
   */
  @Override
  public String getAssetType() {
    return "Fund";
  }

  /**
   * Returns the immutable list of fund components.
   *
   * @return weighted stock components
   */
  public List<FundComponent> getComponents() {
    return components;
  }

  private static void validateWeights(List<FundComponent> components) {
    BigDecimal totalWeight = components.stream()
        .map(FundComponent::weight)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    if (totalWeight.compareTo(BigDecimal.ONE) != 0) {
      throw new IllegalArgumentException("Fund weights must sum to exactly 1.0");
    }
  }
}
