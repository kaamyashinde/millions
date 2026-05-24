package controller;

import java.math.BigDecimal;

/**
 * Aggregated view of a player's holdings for one tradable symbol.
 */
public record HoldingSummary(
    String symbol,
    String displayName,
    String assetType,
    BigDecimal totalQuantity,
    BigDecimal avgPurchasePrice,
    BigDecimal currentPrice) {}
