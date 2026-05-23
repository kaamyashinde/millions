package model.session;

import java.math.BigDecimal;

/**
 * Summary of a completed exit-game flow after liquidating holdings and deleting the profile.
 *
 * @param symbolsSold number of distinct symbols liquidated
 * @param transactionCount total sale transactions executed
 * @param finalCash player cash balance after liquidation (before profile deletion)
 */
public record ExitGameResult(int symbolsSold, int transactionCount, BigDecimal finalCash) {}
