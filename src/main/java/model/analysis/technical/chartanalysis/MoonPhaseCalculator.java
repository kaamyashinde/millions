package model.analysis.technical.chartanalysis;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * Calculates approximate new-moon and full-moon days within a date range.
 *
 * <p>Uses a fixed reference new moon (2024-01-11) and a synodic cycle of 29.53059 days. A day
 * qualifies as a phase event when its cycle position falls within ±1 day of the phase peak.
 *
 * @author kaamyashinde
 * @version 0.1.0
 * @since 30-03-2026
 */
public class MoonPhaseCalculator {

  /**
   * Lunar phases tracked by this calculator.
   */
  public enum Phase {
    NEW_MOON,
    FULL_MOON
  }

  private static final LocalDate REFERENCE_NEW_MOON = LocalDate.of(2024, 1, 11);
  private static final double CYCLE_DAYS = 29.53059;
  private static final double HALF_CYCLE = CYCLE_DAYS / 2.0; // ≈14.77 days

  private MoonPhaseCalculator() {
  }

  /**
   * Computes phase events for each 1-based day index in {@code [1, totalDays]}.
   *
   * <p>A day is a new-moon day if its cycle position is {@code < 1.0}; it is a full-moon day if
   * {@code |position − 14.77| < 1.0}.
   *
   * @param startDate the calendar date corresponding to day index 1
   * @param totalDays number of trading days to examine
   * @return map of day index → {@link Phase}; days with no phase event are absent
   */
  public static Map<Integer, Phase> compute(LocalDate startDate, int totalDays) {
    Map<Integer, Phase> result = new HashMap<>();
    for (int day = 1; day <= totalDays; day++) {
      LocalDate date = startDate.plusDays(day - 1);
      long daysSinceRef = ChronoUnit.DAYS.between(REFERENCE_NEW_MOON, date);
      double cyclePos = ((daysSinceRef % CYCLE_DAYS) + CYCLE_DAYS) % CYCLE_DAYS;
      if (cyclePos < 1.0) {
        result.put(day, Phase.NEW_MOON);
      } else if (Math.abs(cyclePos - HALF_CYCLE) < 1.0) {
        result.put(day, Phase.FULL_MOON);
      }
    }
    return result;
  }
}
