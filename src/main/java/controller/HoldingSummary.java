package controller;

import java.math.BigDecimal;

/**
 * Aggregated view of a player's holdings for one tradable symbol.
 *
 * <p>{@link PortfolioController} builds these rows from FIFO {@link model.core.asset.Share} lots
 * so the view can display one row per symbol.
 *
 * @param symbol tradable symbol shared by the grouped lots
 * @param displayName human-readable asset name
 * @param assetType display category such as stock or fund
 * @param totalQuantity total held quantity across all lots
 * @param avgPurchasePrice weighted average purchase price
 * @param currentPrice latest listed sales price
 *
 * @author kaamyashinde
 * @version 1.0.0
 * @since 2026-05-24
 */
public record HoldingSummary(
    String symbol,
    String displayName,
    String assetType,
    BigDecimal totalQuantity,
    BigDecimal avgPurchasePrice,
    BigDecimal currentPrice) {}
