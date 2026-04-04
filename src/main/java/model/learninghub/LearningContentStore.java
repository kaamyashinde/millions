package model.learninghub;

import java.util.Comparator;
import java.util.List;

/**
 * Central store (Facade) for all Learning Hub content. Acts as the single source of truth for
 * categories, items, and resources — the UI never contains hardcoded copy.
 *
 * <p>All content is held as {@code private static final} fields. The class is non-instantiable.
 *
 * @author kaamyashinde
 * @version 1.0.0
 * @since 04-04-2026
 */
public final class LearningContentStore {

  private LearningContentStore() {
  }

  // ── Categories ──────────────────────────────────────────────────────────────

  private static final LearningCategory CAT_BASICS =
      new LearningCategory("basics", "Basics", "Core concepts every investor should know", "📖");

  private static final LearningCategory CAT_HOW_INVESTING_WORKS =
      new LearningCategory("how-investing-works", "How Investing Works",
          "Understand how money grows in markets", "💡");

  private static final LearningCategory CAT_RISK =
      new LearningCategory("risk-diversification", "Risk & Diversification",
          "Manage uncertainty and spread your exposure", "⚖️");

  private static final LearningCategory CAT_MARKET =
      new LearningCategory("market-understanding", "Market Understanding",
          "Read the market and understand price movements", "📈");

  private static final LearningCategory CAT_STRATEGIES =
      new LearningCategory("strategies", "Strategies",
          "Proven approaches to building a portfolio", "🎯");

  private static final LearningCategory CAT_PRACTICAL =
      new LearningCategory("practical-learning", "Practical Learning",
          "Hands-on tools and real-world exercises", "🛠️");

  // ── Items ────────────────────────────────────────────────────────────────────

  private static final LearningItem ITEM_WHAT_IS_STOCK =
      new LearningItem("what-is-a-stock", "What Is a Stock?", "what-is-a-stock",
          "Learn what owning a share of a company actually means.",
          CAT_BASICS, Difficulty.BEGINNER, true);

  private static final LearningItem ITEM_HOW_PRICES_MOVE =
      new LearningItem("how-stock-prices-move", "How Stock Prices Move", "how-stock-prices-move",
          "Discover the forces of supply and demand that drive price changes.",
          CAT_HOW_INVESTING_WORKS, Difficulty.BEGINNER, true);

  private static final LearningItem ITEM_WHAT_IS_RISK =
      new LearningItem("what-is-investment-risk", "What Is Investment Risk?",
          "what-is-investment-risk",
          "Understand why all investments carry some level of uncertainty.",
          CAT_RISK, Difficulty.BEGINNER, true);

  private static final LearningItem ITEM_STOCK_VS_BOND =
      new LearningItem("stocks-vs-bonds", "Stocks vs. Bonds", "stocks-vs-bonds",
          "Compare the two most common asset classes side by side.",
          CAT_BASICS, Difficulty.BEGINNER, false);

  private static final LearningItem ITEM_COMPOUND_INTEREST =
      new LearningItem("compound-interest", "The Power of Compound Interest",
          "compound-interest",
          "See how reinvesting returns accelerates portfolio growth over time.",
          CAT_HOW_INVESTING_WORKS, Difficulty.BEGINNER, false);

  private static final LearningItem ITEM_DIVERSIFICATION =
      new LearningItem("diversification-basics", "Diversification Basics",
          "diversification-basics",
          "Why spreading investments across assets reduces overall risk.",
          CAT_RISK, Difficulty.INTERMEDIATE, false);

  private static final LearningItem ITEM_READING_CHARTS =
      new LearningItem("reading-stock-charts", "Reading Stock Charts", "reading-stock-charts",
          "Interpret candlestick charts and identify key price patterns.",
          CAT_MARKET, Difficulty.INTERMEDIATE, false);

  private static final LearningItem ITEM_INDEX_FUNDS =
      new LearningItem("index-funds-explained", "Index Funds Explained", "index-funds-explained",
          "Low-cost, passive investing through broad market indices.",
          CAT_STRATEGIES, Difficulty.BEGINNER, false);

  private static final LearningItem ITEM_DOLLAR_COST =
      new LearningItem("dollar-cost-averaging", "Dollar-Cost Averaging",
          "dollar-cost-averaging",
          "Invest a fixed amount regularly to reduce timing risk.",
          CAT_STRATEGIES, Difficulty.BEGINNER, false);

  private static final LearningItem ITEM_PAPER_TRADING =
      new LearningItem("paper-trading", "Paper Trading: Practice Without Risk",
          "paper-trading",
          "Simulate real trades in a risk-free environment before going live.",
          CAT_PRACTICAL, Difficulty.BEGINNER, false);

  // ── Resources ────────────────────────────────────────────────────────────────

  private static final LearningResource RES_AKSJER_FOR_ALLE =
      new LearningResource("res-aksjer-for-alle", "Aksjer for alle", "Oslo Børs",
          LearningResourceType.ARTICLE,
          "https://www.oslobors.no/Oslo-Boers/Handel/Aksjer-og-egenkapitalbevis/Aksjer-for-alle",
          "Oslo Børs's beginner guide to buying and owning shares.");

  private static final LearningResource RES_INVESTOPEDIA =
      new LearningResource("res-investopedia-stocks", "Stocks Overview", "Investopedia",
          LearningResourceType.ARTICLE,
          "https://www.investopedia.com/terms/s/stock.asp",
          "Comprehensive reference covering stock types, markets, and valuation.");

  private static final LearningResource RES_NORDNET_ACADEMY =
      new LearningResource("res-nordnet-academy", "Nordnet Academy", "Nordnet",
          LearningResourceType.ARTICLE,
          "https://www.nordnet.no/magasinet/investering/",
          "Norwegian-language investing guides from a leading Nordic broker.");

  private static final LearningResource RES_YOUTUBE_BASICS =
      new LearningResource("res-yt-investing-basics", "Investing Basics (Video)", "YouTube",
          LearningResourceType.VIDEO,
          "https://www.youtube.com/watch?v=Xn7KWR9EOGQ",
          "A short, beginner-friendly introduction to investing in the stock market.");

  // ── Public API ───────────────────────────────────────────────────────────────

  /**
   * Returns all six learning categories in display order.
   *
   * @return immutable list of {@link LearningCategory}
   */
  public static List<LearningCategory> getCategories() {
    return List.of(
        CAT_BASICS,
        CAT_HOW_INVESTING_WORKS,
        CAT_RISK,
        CAT_MARKET,
        CAT_STRATEGIES,
        CAT_PRACTICAL);
  }

  /**
   * Returns all items belonging to the given category.
   *
   * @param category the category to filter by
   * @return immutable list of matching {@link LearningItem}s
   */
  public static List<LearningItem> getItemsByCategory(LearningCategory category) {
    return getAllItems().stream()
        .filter(item -> item.category().equals(category))
        .toList();
  }

  /**
   * Returns items marked as featured, sorted with {@link Difficulty#BEGINNER} first (by ordinal).
   *
   * @return immutable list of featured {@link LearningItem}s
   */
  public static List<LearningItem> getFeaturedItems() {
    return getAllItems().stream()
        .filter(LearningItem::featured)
        .sorted(Comparator.comparingInt(item -> item.difficulty().ordinal()))
        .toList();
  }

  /**
   * Returns all available learning resources.
   *
   * @return immutable list of {@link LearningResource}
   */
  public static List<LearningResource> getResources() {
    return List.of(
        RES_AKSJER_FOR_ALLE,
        RES_INVESTOPEDIA,
        RES_NORDNET_ACADEMY,
        RES_YOUTUBE_BASICS);
  }

  // ── Private helpers ──────────────────────────────────────────────────────────

  private static List<LearningItem> getAllItems() {
    return List.of(
        ITEM_WHAT_IS_STOCK,
        ITEM_HOW_PRICES_MOVE,
        ITEM_WHAT_IS_RISK,
        ITEM_STOCK_VS_BOND,
        ITEM_COMPOUND_INTEREST,
        ITEM_DIVERSIFICATION,
        ITEM_READING_CHARTS,
        ITEM_INDEX_FUNDS,
        ITEM_DOLLAR_COST,
        ITEM_PAPER_TRADING);
  }
}
