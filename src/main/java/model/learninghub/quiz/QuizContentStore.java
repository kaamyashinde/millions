package model.learninghub.quiz;

import java.util.List;
import java.util.Optional;
import model.learninghub.learn.LearningItem;

/**
 * Static facade providing all quiz content for the Learning Hub.
 *
 * <p>Contains 5 questions for each of the 10 learning topics (50 questions total).
 * Each {@link Quiz} is linked to a {@link LearningItem} via {@link Quiz#linkedItemId()}.
 *
 * @author kaamyashinde
 * @version 1.0.0
 * @since 04-04-2026
 */
public final class QuizContentStore {

  private QuizContentStore() {
  }

  // ── what-is-a-stock ─────────────────────────────────────────────────────────

  private static final Quiz QUIZ_WHAT_IS_A_STOCK = new Quiz(
      "quiz-what-is-a-stock",
      "what-is-a-stock",
      "What Is a Stock? — Quiz",
      List.of(
          new QuizQuestion(
              "wias-q1",
              "What does owning a single share of a company give you?",
              List.of(
                  new QuizAnswer("a", "A loan to the company"),
                  new QuizAnswer("b", "A fractional ownership stake in that company"),
                  new QuizAnswer("c", "Guaranteed dividends every year"),
                  new QuizAnswer("d", "The right to vote on every business decision")),
              "b",
              "A share represents part-ownership of a company. Shareholders own a slice of the business proportional to how many shares they hold.",
              "res-investopedia-stocks"),
          new QuizQuestion(
              "wias-q2",
              "What is an IPO?",
              List.of(
                  new QuizAnswer("a", "An internal price offer between existing shareholders"),
                  new QuizAnswer("b", "When a private company first sells shares to the public"),
                  new QuizAnswer("c", "A government bond issued to the public"),
                  new QuizAnswer("d", "A type of dividend payment")),
              "b",
              "IPO stands for Initial Public Offering — the first time a private company offers its shares for purchase on a public stock exchange.",
              "res-investopedia-stocks"),
          new QuizQuestion(
              "wias-q3",
              "What is a dividend?",
              List.of(
                  new QuizAnswer("a", "A fee charged when you sell shares"),
                  new QuizAnswer("b", "A penalty for holding shares too long"),
                  new QuizAnswer("c", "A portion of company profits paid to shareholders"),
                  new QuizAnswer("d", "The difference between buy and sell price")),
              "c",
              "Dividends are distributions of a company's profits to its shareholders. Not all companies pay dividends — many reinvest profits back into the business.",
              "res-aksjer-for-alle"),
          new QuizQuestion(
              "wias-q4",
              "A company has 1 000 shares outstanding, each worth 100 NOK. What is its market capitalisation?",
              List.of(
                  new QuizAnswer("a", "1 000 NOK"),
                  new QuizAnswer("b", "100 NOK"),
                  new QuizAnswer("c", "10 000 NOK"),
                  new QuizAnswer("d", "100 000 NOK")),
              "d",
              "Market capitalisation = share price × number of shares outstanding. Here: 100 NOK × 1 000 = 100 000 NOK.",
              "res-investopedia-stocks"),
          new QuizQuestion(
              "wias-q5",
              "Which entity sets the day-to-day strategy of a public company?",
              List.of(
                  new QuizAnswer("a", "The shareholders directly"),
                  new QuizAnswer("b", "The stock exchange"),
                  new QuizAnswer("c", "The company's management team"),
                  new QuizAnswer("d", "The government regulator")),
              "c",
              "Shareholders elect a board of directors, who in turn appoint management. Day-to-day decisions are made by the management team — shareholders vote on major decisions at annual meetings.",
              "res-aksjer-for-alle")));

  // ── how-stock-prices-move ───────────────────────────────────────────────────

  private static final Quiz QUIZ_HOW_STOCK_PRICES_MOVE = new Quiz(
      "quiz-how-stock-prices-move",
      "how-stock-prices-move",
      "How Stock Prices Move — Quiz",
      List.of(
          new QuizQuestion(
              "hspm-q1",
              "What happens to a stock's price when there are more buyers than sellers?",
              List.of(
                  new QuizAnswer("a", "The price falls"),
                  new QuizAnswer("b", "The price stays the same"),
                  new QuizAnswer("c", "The price rises"),
                  new QuizAnswer("d", "Trading is suspended")),
              "c",
              "Stock prices are driven by supply and demand. When demand (buyers) exceeds supply (sellers), buyers must offer higher prices to attract sellers — pushing the price up.",
              "res-investopedia-stocks"),
          new QuizQuestion(
              "hspm-q2",
              "Which single factor most strongly drives a company's long-term share price?",
              List.of(
                  new QuizAnswer("a", "The company's social media following"),
                  new QuizAnswer("b", "The company's earnings (profits)"),
                  new QuizAnswer("c", "The number of employees"),
                  new QuizAnswer("d", "The age of the company")),
              "b",
              "Over the long term, a company's earnings growth is the primary driver of its share price. In the short term, sentiment and news play a larger role, but profits ultimately determine value.",
              "res-investopedia-stocks"),
          new QuizQuestion(
              "hspm-q3",
              "What does 'volatility' describe in the context of a stock?",
              List.of(
                  new QuizAnswer("a", "How frequently a stock pays dividends"),
                  new QuizAnswer("b", "How many people own the stock"),
                  new QuizAnswer("c", "How quickly you can sell the stock"),
                  new QuizAnswer("d", "How much the price swings up and down over time")),
              "d",
              "Volatility measures the degree of price variation over time. A highly volatile stock can move sharply in either direction, indicating higher risk and potential reward.",
              "res-yt-investing-basics"),
          new QuizQuestion(
              "hspm-q4",
              "A company reports earnings far below analyst expectations. What typically happens to its share price?",
              List.of(
                  new QuizAnswer("a", "It rises because the company is honest"),
                  new QuizAnswer("b", "Nothing — only dividends affect the price"),
                  new QuizAnswer("c", "It falls as investors sell in disappointment"),
                  new QuizAnswer("d", "The stock is automatically delisted")),
              "c",
              "Markets react strongly to earnings surprises. When results miss expectations, investors often sell, pushing the price lower — even if the company is still profitable.",
              "res-yt-investing-basics"),
          new QuizQuestion(
              "hspm-q5",
              "What is 'market sentiment'?",
              List.of(
                  new QuizAnswer("a", "The total value of all stocks on an exchange"),
                  new QuizAnswer("b", "A government report on economic conditions"),
                  new QuizAnswer("c",
                      "The overall mood (optimism or pessimism) of investors collectively"),
                  new QuizAnswer("d", "The average dividend yield of the market")),
              "c",
              "Market sentiment refers to the overall attitude of investors toward a market or asset. Positive sentiment (bulls) drives prices up; negative sentiment (bears) drives them down.",
              "res-investopedia-stocks")));

  // ── what-is-investment-risk ─────────────────────────────────────────────────

  private static final Quiz QUIZ_WHAT_IS_INVESTMENT_RISK = new Quiz(
      "quiz-what-is-investment-risk",
      "what-is-investment-risk",
      "What Is Investment Risk? — Quiz",
      List.of(
          new QuizQuestion(
              "wiir-q1",
              "Which statement best describes the risk-return relationship in investing?",
              List.of(
                  new QuizAnswer("a", "Lower risk always leads to higher returns"),
                  new QuizAnswer("b", "Risk and return are unrelated"),
                  new QuizAnswer("c", "Higher potential returns typically come with higher risk"),
                  new QuizAnswer("d", "Government bonds always outperform stocks")),
              "c",
              "The risk-return trade-off is a fundamental principle: to earn higher potential returns, investors must accept higher risk. There is no free lunch in investing.",
              "res-nordnet-academy"),
          new QuizQuestion(
              "wiir-q2",
              "Which type of risk can you reduce by investing in many different companies?",
              List.of(
                  new QuizAnswer("a", "Market (systematic) risk"),
                  new QuizAnswer("b", "Inflation risk"),
                  new QuizAnswer("c", "Interest rate risk"),
                  new QuizAnswer("d", "Company-specific (unsystematic) risk")),
              "d",
              "Diversification reduces unsystematic (company-specific) risk. If one company fails, others in your portfolio can offset the loss. Systematic risk affects the whole market and cannot be diversified away.",
              "res-nordnet-academy"),
          new QuizQuestion(
              "wiir-q3",
              "What is 'liquidity risk'?",
              List.of(
                  new QuizAnswer("a", "The risk that a company runs out of cash"),
                  new QuizAnswer("b",
                      "The risk that you cannot sell an investment quickly at a fair price"),
                  new QuizAnswer("c", "The risk that inflation erodes your returns"),
                  new QuizAnswer("d", "The risk of currency fluctuations")),
              "b",
              "Liquidity risk is the danger of being unable to sell an asset quickly without accepting a significant price discount. Small-cap stocks and real estate often carry higher liquidity risk than large-cap shares.",
              "res-nordnet-academy"),
          new QuizQuestion(
              "wiir-q4",
              "Even a savings account carries some risk. Which risk is it?",
              List.of(
                  new QuizAnswer("a", "Default risk — the bank might go bankrupt"),
                  new QuizAnswer("b", "Volatility risk — the balance fluctuates daily"),
                  new QuizAnswer("c",
                      "Inflation risk — rising costs erode the real value of savings"),
                  new QuizAnswer("d", "Liquidity risk — you cannot withdraw your money")),
              "c",
              "Inflation risk means the purchasing power of your money decreases over time. If inflation is 3% and your savings account pays 1%, you are effectively losing 2% of real value per year.",
              "res-nordnet-academy"),
          new QuizQuestion(
              "wiir-q5",
              "A stock whose price varies by 20% annually is described as having higher __ compared to a stock varying by 4%?",
              List.of(
                  new QuizAnswer("a", "Liquidity"),
                  new QuizAnswer("b", "Yield"),
                  new QuizAnswer("c", "Volatility"),
                  new QuizAnswer("d", "Dividend coverage")),
              "c",
              "Volatility is measured as the standard deviation of returns. A stock swinging 20% per year is far more volatile than one swinging 4%, meaning it carries more short-term price risk.",
              "res-nordnet-academy")));

  // ── stocks-vs-bonds ─────────────────────────────────────────────────────────

  private static final Quiz QUIZ_STOCKS_VS_BONDS = new Quiz(
      "quiz-stocks-vs-bonds",
      "stocks-vs-bonds",
      "Stocks vs. Bonds — Quiz",
      List.of(
          new QuizQuestion(
              "svb-q1",
              "What does a bondholder receive that a shareholder does not automatically get?",
              List.of(
                  new QuizAnswer("a", "Voting rights at company meetings"),
                  new QuizAnswer("b", "A share of company profits"),
                  new QuizAnswer("c", "Fixed interest payments (coupons) and return of principal"),
                  new QuizAnswer("d", "Ownership of the company's assets")),
              "c",
              "Bonds are debt instruments — the bondholder lends money and receives scheduled interest (coupon) payments and the principal back at maturity. Shareholders own equity and receive no guaranteed payments.",
              "res-investopedia-stocks"),
          new QuizQuestion(
              "svb-q2",
              "In a company bankruptcy, who gets paid first?",
              List.of(
                  new QuizAnswer("a", "Common shareholders"),
                  new QuizAnswer("b", "Bondholders (creditors)"),
                  new QuizAnswer("c", "Preferred shareholders"),
                  new QuizAnswer("d", "All are paid equally")),
              "b",
              "In liquidation, creditors (including bondholders) have a higher claim on assets than shareholders. Common shareholders are last in line and often receive nothing.",
              "res-investopedia-stocks"),
          new QuizQuestion(
              "svb-q3",
              "Which asset class typically offers higher long-term returns but with more short-term volatility?",
              List.of(
                  new QuizAnswer("a", "Government bonds"),
                  new QuizAnswer("b", "Corporate bonds"),
                  new QuizAnswer("c", "Stocks"),
                  new QuizAnswer("d", "Both are identical in risk and return")),
              "c",
              "Historically, stocks have delivered higher long-term returns than bonds, but with considerably more year-to-year price swings. Bonds offer more stability but lower expected returns.",
              "res-nordnet-academy"),
          new QuizQuestion(
              "svb-q4",
              "When interest rates rise, what typically happens to the price of existing bonds?",
              List.of(
                  new QuizAnswer("a", "Bond prices rise"),
                  new QuizAnswer("b", "Bond prices fall"),
                  new QuizAnswer("c", "Bond prices are unaffected"),
                  new QuizAnswer("d", "Bond coupon payments increase")),
              "b",
              "There is an inverse relationship between interest rates and bond prices. When new bonds offer higher coupons (due to rising rates), older lower-coupon bonds become less attractive and fall in price.",
              "res-investopedia-stocks"),
          new QuizQuestion(
              "svb-q5",
              "Why might an investor include both stocks and bonds in their portfolio?",
              List.of(
                  new QuizAnswer("a", "Bonds amplify stock returns"),
                  new QuizAnswer("b",
                      "Stocks and bonds often move in opposite directions, reducing overall risk"),
                  new QuizAnswer("c", "It is a legal requirement"),
                  new QuizAnswer("d", "To maximise tax liability")),
              "b",
              "Diversifying across asset classes — stocks for growth potential, bonds for income and stability — can smooth out a portfolio's returns over time, as they often respond differently to economic conditions.",
              "res-nordnet-academy")));

  // ── compound-interest ───────────────────────────────────────────────────────

  private static final Quiz QUIZ_COMPOUND_INTEREST = new Quiz(
      "quiz-compound-interest",
      "compound-interest",
      "Compound Interest — Quiz",
      List.of(
          new QuizQuestion(
              "ci-q1",
              "What is 'compound interest'?",
              List.of(
                  new QuizAnswer("a", "Interest calculated only on the original principal"),
                  new QuizAnswer("b",
                      "Interest earned on both the principal and previously earned interest"),
                  new QuizAnswer("c", "A fixed interest payment regardless of balance"),
                  new QuizAnswer("d", "Interest charged for withdrawing money early")),
              "b",
              "Compound interest means you earn returns on your returns. Over time, this causes your investment to grow exponentially rather than linearly — it is the 'eighth wonder of the world' according to many investors.",
              "res-nordnet-academy"),
          new QuizQuestion(
              "ci-q2",
              "You invest 1 000 NOK at 10% annual return. Approximately how much will you have after 10 years with compounding?",
              List.of(
                  new QuizAnswer("a", "2 000 NOK"),
                  new QuizAnswer("b", "1 100 NOK"),
                  new QuizAnswer("c", "2 594 NOK"),
                  new QuizAnswer("d", "5 000 NOK")),
              "c",
              "With compound growth, 1 000 NOK × 1.10^10 ≈ 2 594 NOK. Simple interest would only give 2 000 NOK. The extra 594 NOK is the power of compounding.",
              "res-yt-investing-basics"),
          new QuizQuestion(
              "ci-q3",
              "Why does starting to invest early make such a large difference?",
              List.of(
                  new QuizAnswer("a", "Early investors pay lower fees"),
                  new QuizAnswer("b", "Stock markets only grow when young investors participate"),
                  new QuizAnswer("c",
                      "More time allows compounding to multiply returns over a longer period"),
                  new QuizAnswer("d", "Early investors receive government subsidies")),
              "c",
              "Time is the most powerful ingredient in compounding. Each additional year multiplies your total return. Starting 10 years earlier can more than double your final portfolio value.",
              "res-nordnet-academy"),
          new QuizQuestion(
              "ci-q4",
              "What is the 'Rule of 72'?",
              List.of(
                  new QuizAnswer("a", "You must diversify into at least 72 stocks"),
                  new QuizAnswer("b",
                      "Divide 72 by the annual return to estimate how many years to double your money"),
                  new QuizAnswer("c", "You should invest 72% of your salary"),
                  new QuizAnswer("d", "A stock index rebalances every 72 days")),
              "b",
              "The Rule of 72 is a quick mental maths shortcut: 72 ÷ annual return % = approximate years to double. At 8% return, 72 ÷ 8 = 9 years to double your investment.",
              "res-yt-investing-basics"),
          new QuizQuestion(
              "ci-q5",
              "Which of the following benefits most from compound interest over a long period?",
              List.of(
                  new QuizAnswer("a", "A savings account with monthly withdrawals"),
                  new QuizAnswer("b",
                      "A stock portfolio where dividends are reinvested and left to grow"),
                  new QuizAnswer("c", "A fixed-term deposit with annual withdrawals of interest"),
                  new QuizAnswer("d", "Cash kept under a mattress")),
              "b",
              "Reinvesting dividends and letting them compound is the textbook example of compounding in action. Each reinvested dividend buys more shares, which in turn pay more dividends.",
              "res-nordnet-academy")));

  // ── diversification-basics ──────────────────────────────────────────────────

  private static final Quiz QUIZ_DIVERSIFICATION_BASICS = new Quiz(
      "quiz-diversification-basics",
      "diversification-basics",
      "Diversification Basics — Quiz",
      List.of(
          new QuizQuestion(
              "db-q1",
              "What is the primary purpose of diversification?",
              List.of(
                  new QuizAnswer("a", "To guarantee positive returns"),
                  new QuizAnswer("b",
                      "To reduce the impact of any single investment performing poorly"),
                  new QuizAnswer("c", "To increase the overall risk of a portfolio"),
                  new QuizAnswer("d", "To concentrate on the best-performing sector")),
              "b",
              "Diversification spreads risk across multiple assets so that a poor performance by one does not devastate the whole portfolio. It does not eliminate risk, but it reduces unnecessary concentration.",
              "res-nordnet-academy"),
          new QuizQuestion(
              "db-q2",
              "An investor holds only shares in one oil company. Which risk are they most exposed to?",
              List.of(
                  new QuizAnswer("a", "Inflation risk"),
                  new QuizAnswer("b", "Currency risk"),
                  new QuizAnswer("c", "Company-specific (unsystematic) risk"),
                  new QuizAnswer("d", "Liquidity risk")),
              "c",
              "Concentrating in a single company means any bad news specific to that company — a scandal, poor earnings, or management failure — hits your entire portfolio.",
              "res-aksjer-for-alle"),
          new QuizQuestion(
              "db-q3",
              "Which type of risk CANNOT be eliminated through diversification?",
              List.of(
                  new QuizAnswer("a", "Company-specific risk"),
                  new QuizAnswer("b", "Industry-specific risk"),
                  new QuizAnswer("c", "Systematic (market) risk"),
                  new QuizAnswer("d", "Management risk")),
              "c",
              "Systematic risk (market risk) affects all investments — a global recession, for example, hits virtually every asset class. Diversification reduces unsystematic risk but cannot remove systematic risk.",
              "res-nordnet-academy"),
          new QuizQuestion(
              "db-q4",
              "A well-diversified portfolio might include which combination?",
              List.of(
                  new QuizAnswer("a", "Only technology stocks from one country"),
                  new QuizAnswer("b",
                      "Stocks, bonds, and assets from multiple sectors and regions"),
                  new QuizAnswer("c", "Only the ten largest companies in one index"),
                  new QuizAnswer("d", "Cash and gold only")),
              "b",
              "True diversification spans asset classes (stocks, bonds, real estate), sectors (technology, healthcare, energy), and geographies (domestic, international, emerging markets).",
              "res-nordnet-academy"),
          new QuizQuestion(
              "db-q5",
              "What is 'correlation' in the context of portfolio diversification?",
              List.of(
                  new QuizAnswer("a", "The fee charged to manage a diversified fund"),
                  new QuizAnswer("b", "A measure of how similarly two assets move in price"),
                  new QuizAnswer("c", "The number of assets in a portfolio"),
                  new QuizAnswer("d", "The annual return of a portfolio")),
              "b",
              "Correlation measures how closely two assets move together. Low or negative correlation between assets means they tend to move differently, which is the key to effective diversification.",
              "res-nordnet-academy")));

  // ── reading-stock-charts ────────────────────────────────────────────────────

  private static final Quiz QUIZ_READING_STOCK_CHARTS = new Quiz(
      "quiz-reading-stock-charts",
      "reading-stock-charts",
      "Reading Stock Charts — Quiz",
      List.of(
          new QuizQuestion(
              "rsc-q1",
              "On a candlestick chart, what does the body of a candle represent?",
              List.of(
                  new QuizAnswer("a", "The highest and lowest prices of the day"),
                  new QuizAnswer("b", "The difference between opening and closing price"),
                  new QuizAnswer("c", "The trading volume for the day"),
                  new QuizAnswer("d", "The dividend paid that day")),
              "b",
              "The body of a candlestick shows the range between the opening and closing price. A green/white body means the close was higher than the open (bullish); red/black means the opposite (bearish).",
              "res-yt-investing-basics"),
          new QuizQuestion(
              "rsc-q2",
              "What does a 'moving average' show on a stock chart?",
              List.of(
                  new QuizAnswer("a", "The average trading volume over a period"),
                  new QuizAnswer("b", "The smoothed average price over a defined number of days"),
                  new QuizAnswer("c", "The predicted future price"),
                  new QuizAnswer("d", "The total return including dividends")),
              "b",
              "A moving average smooths out day-to-day price fluctuations to show the underlying trend. A 50-day moving average, for example, shows the average closing price over the past 50 trading days.",
              "res-investopedia-stocks"),
          new QuizQuestion(
              "rsc-q3",
              "What does high trading volume during a price increase generally suggest?",
              List.of(
                  new QuizAnswer("a", "The price rise is likely to reverse soon"),
                  new QuizAnswer("b", "The move has weak conviction behind it"),
                  new QuizAnswer("c",
                      "Many investors support the move, giving it stronger conviction"),
                  new QuizAnswer("d", "The stock is about to be delisted")),
              "c",
              "Volume confirms price moves. A price rise on high volume suggests broad market participation and conviction. A rise on low volume may be less reliable and more likely to reverse.",
              "res-yt-investing-basics"),
          new QuizQuestion(
              "rsc-q4",
              "What is a 'support level' on a price chart?",
              List.of(
                  new QuizAnswer("a", "The highest price a stock has ever reached"),
                  new QuizAnswer("b",
                      "A price level where the stock tends to find buying interest and stop falling"),
                  new QuizAnswer("c", "The average analyst price target"),
                  new QuizAnswer("d", "The price at which the company buys back its own shares")),
              "b",
              "A support level is a price zone where demand has historically been strong enough to prevent further decline. Traders watch these levels as potential entry points or as warning signs if they are broken.",
              "res-investopedia-stocks"),
          new QuizQuestion(
              "rsc-q5",
              "What does the term 'trend' mean in technical analysis?",
              List.of(
                  new QuizAnswer("a", "A sudden one-day price spike"),
                  new QuizAnswer("b", "The general direction in which a price is moving over time"),
                  new QuizAnswer("c", "A stock that is popular on social media"),
                  new QuizAnswer("d", "A guaranteed future price direction")),
              "b",
              "A trend describes the general price direction over time: uptrend (higher highs and higher lows), downtrend (lower highs and lower lows), or sideways. Technical analysts look for trends to guide trading decisions.",
              "res-investopedia-stocks")));

  // ── index-funds-explained ───────────────────────────────────────────────────

  private static final Quiz QUIZ_INDEX_FUNDS_EXPLAINED = new Quiz(
      "quiz-index-funds-explained",
      "index-funds-explained",
      "Index Funds Explained — Quiz",
      List.of(
          new QuizQuestion(
              "ife-q1",
              "What does an index fund aim to do?",
              List.of(
                  new QuizAnswer("a", "Beat the market through active stock picking"),
                  new QuizAnswer("b", "Invest only in government bonds"),
                  new QuizAnswer("c", "Replicate the performance of a specific market index"),
                  new QuizAnswer("d", "Invest only in dividend-paying stocks")),
              "c",
              "An index fund passively tracks a market index (e.g., S&P 500, Oslo Børs All-Share) by holding all or a representative sample of the index's securities in the same weights.",
              "res-nordnet-academy"),
          new QuizQuestion(
              "ife-q2",
              "Why are index funds generally cheaper than actively managed funds?",
              List.of(
                  new QuizAnswer("a", "They invest in fewer companies"),
                  new QuizAnswer("b",
                      "They require no fund manager to pick stocks, so costs are lower"),
                  new QuizAnswer("c", "They are only sold to professional investors"),
                  new QuizAnswer("d", "They are sponsored by governments")),
              "b",
              "Active funds pay managers to research and select stocks, increasing costs. Index funds follow a rules-based approach requiring minimal human intervention, resulting in much lower annual fees (expense ratios).",
              "res-nordnet-academy"),
          new QuizQuestion(
              "ife-q3",
              "What is an ETF (Exchange-Traded Fund)?",
              List.of(
                  new QuizAnswer("a", "A bond that pays interest monthly"),
                  new QuizAnswer("b", "An index fund that trades on a stock exchange like a share"),
                  new QuizAnswer("c", "A government savings scheme"),
                  new QuizAnswer("d", "A fund that only invests in emerging markets")),
              "b",
              "An ETF is a type of index fund that is listed and traded on a stock exchange, just like individual shares. This gives investors flexibility to buy and sell throughout the trading day at market prices.",
              "res-aksjer-for-alle"),
          new QuizQuestion(
              "ife-q4",
              "Studies show that over long periods, most actively managed funds:",
              List.of(
                  new QuizAnswer("a", "Consistently outperform index funds after fees"),
                  new QuizAnswer("b", "Match index funds exactly"),
                  new QuizAnswer("c", "Underperform index funds after fees"),
                  new QuizAnswer("d", "Are guaranteed to deliver positive returns")),
              "c",
              "Academic research consistently shows that the majority of actively managed funds underperform their benchmark index after accounting for fees over long periods. This is a core argument for passive index investing.",
              "res-nordnet-academy"),
          new QuizQuestion(
              "ife-q5",
              "What does it mean that an index fund provides 'instant diversification'?",
              List.of(
                  new QuizAnswer("a", "You receive your money back instantly"),
                  new QuizAnswer("b",
                      "The fund splits your investment across all companies in the index in one purchase"),
                  new QuizAnswer("c", "All your money is converted to foreign currency"),
                  new QuizAnswer("d", "You are protected from all losses")),
              "b",
              "Buying a single index fund share gives you exposure to all the companies within that index. For example, a global index fund might spread your investment across thousands of companies worldwide.",
              "res-nordnet-academy")));

  // ── dollar-cost-averaging ───────────────────────────────────────────────────

  private static final Quiz QUIZ_DOLLAR_COST_AVERAGING = new Quiz(
      "quiz-dollar-cost-averaging",
      "dollar-cost-averaging",
      "Dollar-Cost Averaging — Quiz",
      List.of(
          new QuizQuestion(
              "dca-q1",
              "What is dollar-cost averaging (DCA)?",
              List.of(
                  new QuizAnswer("a", "Investing a lump sum at the lowest price of the year"),
                  new QuizAnswer("b",
                      "Investing a fixed amount at regular intervals regardless of price"),
                  new QuizAnswer("c", "Converting all savings to US dollars"),
                  new QuizAnswer("d",
                      "Selling shares whenever the price averages out to your purchase price")),
              "b",
              "DCA means investing a consistent amount (e.g., 500 NOK per month) on a regular schedule. This automatically buys more shares when prices are low and fewer when prices are high.",
              "res-nordnet-academy"),
          new QuizQuestion(
              "dca-q2",
              "What is the main benefit of DCA during a falling market?",
              List.of(
                  new QuizAnswer("a", "You stop buying, protecting you from losses"),
                  new QuizAnswer("b", "You automatically buy more shares at lower prices"),
                  new QuizAnswer("c", "You receive a government bonus for continued investing"),
                  new QuizAnswer("d", "Your existing shares increase in value")),
              "b",
              "When prices fall, your fixed investment amount buys more shares. This lowers your average cost per share over time — a process called 'averaging down' — so you benefit more when prices recover.",
              "res-nordnet-academy"),
          new QuizQuestion(
              "dca-q3",
              "Which psychological benefit does DCA offer?",
              List.of(
                  new QuizAnswer("a", "It guarantees you buy at the absolute lowest price"),
                  new QuizAnswer("b",
                      "It removes the need to time the market, reducing emotional decision-making"),
                  new QuizAnswer("c", "It eliminates all investment risk"),
                  new QuizAnswer("d", "It doubles your return compared to lump-sum investing")),
              "b",
              "One of DCA's greatest advantages is removing the temptation to time the market. By investing automatically on schedule, investors avoid making fear-driven or greed-driven decisions.",
              "res-nordnet-academy"),
          new QuizQuestion(
              "dca-q4",
              "An investor uses DCA and buys 3 months in a row at prices of 100, 50, and 100 NOK per share. What is their average cost per share?",
              List.of(
                  new QuizAnswer("a", "83.33 NOK"),
                  new QuizAnswer("b", "75 NOK"),
                  new QuizAnswer("c", "100 NOK"),
                  new QuizAnswer("d", "50 NOK")),
              "b",
              "With a fixed monthly investment (say 100 NOK): Month 1 = 1 share at 100, Month 2 = 2 shares at 50, Month 3 = 1 share at 100. Total: 4 shares for 300 NOK → average 75 NOK per share. Lower than the simple average of 83.33.",
              "res-nordnet-academy"),
          new QuizQuestion(
              "dca-q5",
              "In what scenario might a lump-sum investment outperform DCA?",
              List.of(
                  new QuizAnswer("a", "When markets are volatile"),
                  new QuizAnswer("b",
                      "When markets consistently rise throughout the investment period"),
                  new QuizAnswer("c", "When interest rates are rising"),
                  new QuizAnswer("d", "When you are investing for less than one month")),
              "b",
              "If markets rise steadily, a lump-sum investment deployed immediately captures more of the gains. DCA is most advantageous in volatile or declining markets. Over the long run, both strategies tend to produce similar results.",
              "res-nordnet-academy")));

  // ── paper-trading ───────────────────────────────────────────────────────────

  private static final Quiz QUIZ_PAPER_TRADING = new Quiz(
      "quiz-paper-trading",
      "paper-trading",
      "Paper Trading — Quiz",
      List.of(
          new QuizQuestion(
              "pt-q1",
              "What is paper trading?",
              List.of(
                  new QuizAnswer("a", "Trading shares printed on physical paper certificates"),
                  new QuizAnswer("b",
                      "Simulated investing with virtual money to practise without real risk"),
                  new QuizAnswer("c", "A government-regulated trading test for new investors"),
                  new QuizAnswer("d", "Trading only in paper and publishing companies")),
              "b",
              "Paper trading is the practice of simulating buy and sell decisions without using real money. It lets beginners learn market mechanics, test strategies, and build confidence before risking actual capital.",
              "res-yt-investing-basics"),
          new QuizQuestion(
              "pt-q2",
              "What is a key limitation of paper trading compared to real investing?",
              List.of(
                  new QuizAnswer("a", "You cannot track your performance in paper trading"),
                  new QuizAnswer("b",
                      "Paper trading does not simulate the emotional pressure of real money on the line"),
                  new QuizAnswer("c", "Paper trading always shows worse results than real trading"),
                  new QuizAnswer("d", "Paper trading uses outdated price data")),
              "b",
              "The biggest gap between paper and real trading is psychology. Knowing you can 'reset' removes the fear and greed that profoundly affect real trading decisions — skills developed in paper trading may not fully transfer.",
              "res-yt-investing-basics"),
          new QuizQuestion(
              "pt-q3",
              "Which of the following is a good use of paper trading?",
              List.of(
                  new QuizAnswer("a", "Generating actual income from the stock market"),
                  new QuizAnswer("b",
                      "Testing a new investment strategy before committing real money"),
                  new QuizAnswer("c",
                      "Guaranteed practice that will make you profitable in real markets"),
                  new QuizAnswer("d", "Replacing real investing entirely for the long term")),
              "b",
              "Paper trading is excellent for testing strategies, understanding order types, and learning how markets work — all without financial risk. It is a stepping stone, not a substitute for real investing.",
              "res-yt-investing-basics"),
          new QuizQuestion(
              "pt-q4",
              "If a paper trader consistently profits in simulation, what should they conclude?",
              List.of(
                  new QuizAnswer("a", "Their strategy will definitely work with real money"),
                  new QuizAnswer("b",
                      "Their strategy shows promise, but real trading will test it more rigorously"),
                  new QuizAnswer("c", "They should immediately invest all their savings"),
                  new QuizAnswer("d", "Paper profits equal real profits")),
              "b",
              "Paper trading success is promising but not guaranteed to translate. Real trading involves emotions, slippage, transaction costs, and taxes that simulation often cannot perfectly replicate.",
              "res-yt-investing-basics"),
          new QuizQuestion(
              "pt-q5",
              "Which concept does paper trading help you practice that is crucial in real investing?",
              List.of(
                  new QuizAnswer("a", "Filing tax returns on investment gains"),
                  new QuizAnswer("b", "Applying for a brokerage account"),
                  new QuizAnswer("c",
                      "Entering and exiting positions according to a plan, not impulse"),
                  new QuizAnswer("d", "Receiving dividends from companies")),
              "c",
              "Discipline — sticking to your strategy and not reacting emotionally to short-term price moves — is one of the most important investing skills. Paper trading helps you build and test this discipline in a safe environment.",
              "res-yt-investing-basics")));

  // ── All quizzes ─────────────────────────────────────────────────────────────

  private static List<Quiz> getAllQuizzes() {
    return List.of(
        QUIZ_WHAT_IS_A_STOCK,
        QUIZ_HOW_STOCK_PRICES_MOVE,
        QUIZ_WHAT_IS_INVESTMENT_RISK,
        QUIZ_STOCKS_VS_BONDS,
        QUIZ_COMPOUND_INTEREST,
        QUIZ_DIVERSIFICATION_BASICS,
        QUIZ_READING_STOCK_CHARTS,
        QUIZ_INDEX_FUNDS_EXPLAINED,
        QUIZ_DOLLAR_COST_AVERAGING,
        QUIZ_PAPER_TRADING);
  }

  // ── Public API ───────────────────────────────────────────────────────────────

  /**
   * Returns the quiz linked to the given {@link LearningItem#id()}, or empty if none exists.
   *
   * @param itemId the learning item ID to look up
   * @return the matching quiz, or {@link Optional#empty()}
   */
  public static Optional<Quiz> getQuizForItem(String itemId) {
    return getAllQuizzes().stream()
        .filter(q -> q.linkedItemId().equals(itemId))
        .findFirst();
  }

  /**
   * Returns all quizzes in the store.
   *
   * @return immutable list of all quizzes
   */
  public static List<Quiz> getAllQuizzesPublic() {
    return getAllQuizzes();
  }
}
