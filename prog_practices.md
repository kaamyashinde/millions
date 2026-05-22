Now I have a thorough picture. Let me put together a comprehensive design patterns analysis — mapping each identified problem to the most fitting pattern from [refactoring.guru](https://refactoring.guru/design-patterns/java), and only recommending patterns that genuinely solve real problems.

---

## Design Patterns Already Well-Used

Before diving into improvements, let me acknowledge what's already working. Your codebase already applies several patterns correctly:

**Strategy Pattern** — `DailyPriceMoveStrategy`, `MarketEventStrategy`, and `TransactionCalculator` are textbook Strategy implementations. The `Exchange` accepts interchangeable pricing and event strategies via constructor injection.

**Composite Pattern** — `Fund` implements `InvestableAsset` and aggregates `FundComponent` entries that each wrap a `Stock` (also `InvestableAsset`). The `getSalesPrice()` method transparently computes the composite value.

**Factory Method** — `SessionWorkspaceFactory` and `SessionServiceFactory` already separate object construction from usage.

**Template Method** (partial) — `Transaction` defines an abstract `commit(Player)` hook overridden by `Purchase` and `Sale`, though the template is incomplete (more on this below).

---

## Patterns That Solve Real Problems

### 1. Observer Pattern — Fixes `PlayerLevel` Bug + Enables Reactive Updates

**Problem:** `Player.playerLevel` is `final`, set once in the constructor, and `setPlayerLevel()` never assigns the computed value back. The player's level is frozen forever.

**How Observer fixes it:**

```java
public interface PlayerObserver {
    void onPlayerStateChanged(Player player);
}

public class PlayerLevelObserver implements PlayerObserver {
    @Override
    public void onPlayerStateChanged(Player player) {
        player.recalculateLevel();
    }
}
```

The `Player` class would maintain a list of observers and notify them whenever `addMoney()`, `withdrawMoney()`, or portfolio changes occur. The level recalculates automatically. This also opens the door for the notification system — when a player achieves a new level, the UI gets notified via the same observer chain.

**Bonus:** The existing `NotificationService` with its `ObservableList` is already Observer-adjacent in the view layer. Extending this pattern into the model layer creates consistency.

**Impact:** Fixes a real bug, eliminates stale state, enables future features (level-up notifications, achievement triggers).

---

### 2. Template Method — Fixes Transaction Commit Duplication

**Problem:** `Purchase.commit()` and `Sale.commit()` both follow the same skeleton — check committed, validate preconditions, execute, archive, mark committed — but each reimplements the entire flow. `Purchase` even calls `calculateTotal()` twice.

**How Template Method fixes it:**

```java
public abstract class Transaction {
    
    public final void commit(Player player) {
        if (this.isCommitted()) {
            throw new AlreadyCommittedException();
        }
        validatePreconditions(player);
        execute(player);
        player.getTransactionArchive().addTransaction(this);
        this.committed = true;
    }

    protected abstract void validatePreconditions(Player player);
    protected abstract void execute(Player player);
}
```

`Purchase` overrides `validatePreconditions` to check sufficient funds, and `execute` to withdraw money + add share. `Sale` overrides them for its logic. The common archive-and-mark-committed flow lives once in the base class.

**Impact:** Eliminates duplicated commit logic, fixes the double `calculateTotal()` call in `Purchase`, prevents future subclasses from forgetting the archive step.

**Where else:** `UserAccountRepository.listUsernames()` and `listAccounts()` share the same directory-walk structure (check root, list children, filter directories, resolve `account.json`, filter exists, transform, sort). A private template method can deduplicate this.

---

### 3. Facade Pattern (Refined) — Split the `SessionService` God Class

**Problem:** `SessionService` (479 lines) handles auth, profiles, game saves, saved runs, preferences, avatars, leaderboard, and deletion. It has too many reasons to change.

**How Facade restructuring fixes it:**

```
SessionService (slim orchestrator / top-level Facade)
├── AuthService          → register, login, logout, PIN validation
├── ProfileService       → display name, avatar, delete profile
├── GamePersistenceService → save/load active session
├── SavedRunService      → save/list/delete runs, leaderboard flag
└── ProfilePreferencesService → welcome seen, UI preferences
```

Each sub-facade owns one cohesion group. `SessionService` remains as a thin composition root that delegates, so existing callers (view classes) can still use a single entry point if convenient.

**Impact:** Single-responsibility per service, easier testing, smaller blast radius for changes.

---

### 4. Strategy Pattern (Extended) — Recommendation Algorithms

**Problem:** `StockRecommendationService` uses a single hardcoded trend-based algorithm. The thresholds and lookback are constants. This isn't extensible for different recommendation strategies.

**How Strategy extends the existing pattern:**

```java
public interface RecommendationStrategy {
    StockRecommendation recommend(List<BigDecimal> historicalPrices);
}

public class TrendRecommendationStrategy implements RecommendationStrategy { ... }
public class MomentumRecommendationStrategy implements RecommendationStrategy { ... }
public class MeanReversionStrategy implements RecommendationStrategy { ... }
```

`StockRecommendationService` would accept a `RecommendationStrategy` via constructor, just like `Exchange` accepts `DailyPriceMoveStrategy`. This aligns with the existing Strategy usage in the project.

**Impact:** Follows the existing project convention, makes recommendations testable and swappable, enables features like "compare recommendation strategies" in the UI.

---

### 5. Builder Pattern — Complex Object Construction

**Problem:** `SessionService` takes 8 constructor parameters. `Exchange.restore()` takes 6 parameters plus internal mutations. These are hard to read and easy to get wrong.

**How Builder helps:**

```java
Exchange exchange = new Exchange.Builder("Oslo Børs")
    .stocks(stocks)
    .funds(funds)
    .day(savedDay)
    .marketEventHistory(events)
    .lastMarketEvent(lastEvent)
    .dailyPriceMoveStrategy(strategy)
    .marketEventStrategy(eventStrategy)
    .build();
```

This eliminates the confusing `Exchange.restore()` static method that sets `day` and `marketEventHistory` via mutation after construction — a pattern that violates the principle of constructing complete objects.

**Impact:** Clearer construction, impossible to mix up parameter order, self-documenting.

---

### 6. State Pattern — Player Level Behavior

**Problem:** `PlayerLevel` is an enum with `qualifies(Player)` that determines the level, but the level is never re-evaluated. Beyond the bug, levels don't actually *do* anything — they're just labels.

**How State pattern enriches this:**

```java
public interface PlayerLevel {
    String displayName();
    BigDecimal maxTradeSize(Player player);
    boolean qualifies(Player player);
    PlayerLevel checkTransition(Player player);
}
```

Each level (Novice, Investor, Speculator) becomes a State that can define level-specific behavior: trading limits, UI badges, or unlocked features. The `checkTransition()` method returns the new appropriate state when conditions change.

Combined with Observer (pattern #1), whenever player state changes, `checkTransition()` is called and the level updates automatically.

**Impact:** Fixes the level bug properly, makes levels meaningful game mechanics rather than dead labels, follows open/closed principle for adding new levels.

---

### 7. Command Pattern — Trade Execution

**Problem:** `Exchange` directly creates `Purchase`/`Sale` objects, commits them, and handles errors — mixing order creation, execution, and error handling. The buy/sell methods are tightly coupled to the concrete transaction classes.

**How Command formalizes this:**

```java
public interface TradeCommand {
    Transaction execute(Player player);
    String describe();
}

public class BuyCommand implements TradeCommand {
    private final Exchange exchange;
    private final String symbol;
    private final BigDecimal quantity;
    
    @Override
    public Transaction execute(Player player) {
        // validation + purchase logic
    }
}
```

The `Exchange` would use a command factory, and commands could be queued, logged, or even undone. This pairs naturally with the existing `TransactionArchive` — the archive becomes a command history.

**Impact:** Decouples `Exchange` from concrete transaction types, enables command logging/audit trail, opens the door for undo functionality or batch execution.

---

### 8. Chain of Responsibility — Validation Pipeline

**Problem:** `SessionService.validateRegistrationInput()` does sequential validation with hardcoded messages. `GuiAppShell` and `UserInterface` then switch on those exact message strings — a fragile, stringly-typed contract.

**How Chain of Responsibility fixes it:**

```java
public sealed interface ValidationResult {
    record Success() implements ValidationResult {}
    record Failure(ValidationError error) implements ValidationResult {}
}

public enum ValidationError {
    INVALID_USERNAME("Username must be 3-32 characters..."),
    INVALID_PIN("PIN must be 4 to 8 digits."),
    NEGATIVE_STARTING_MONEY("Starting money must be non-negative.");
    
    private final String defaultMessage;
}

public interface RegistrationValidator {
    ValidationResult validate(String username, char[] pin, BigDecimal money);
    RegistrationValidator then(RegistrationValidator next);
}
```

Each validator checks one thing and passes to the next. The result is a typed `ValidationError` enum, not a string. Both GUI and CLI can switch on the enum safely — no more brittle string matching.

**Impact:** Eliminates the dangerous stringly-typed error contract between model and view layers, makes validation composable and extensible, DRYs up the duplicate mapping in `GuiAppShell`/`UserInterface`.

---

### 9. Iterator Pattern — Safe Collection Access

**Problem:** `Stock.getHistoricalPrices()` and `Portfolio.getShares()` return internal mutable lists. Any caller can corrupt state.

**How to apply it:** This doesn't need a full custom Iterator — Java's built-in `Collections.unmodifiableList()` or `List.copyOf()` is the right tool. But the principle of the Iterator pattern applies: expose traversal without exposing internal structure.

```java
// Stock.java
public List<BigDecimal> getHistoricalPrices() {
    return Collections.unmodifiableList(this.price);
}

// Portfolio.java
public List<Share> getShares() {
    return Collections.unmodifiableList(this.shares);
}
```

**Impact:** Prevents accidental mutation, protects encapsulation.

---

### 10. Decorator Pattern — Reusable Image Loading

**Problem:** Avatar image loading is duplicated in 4 view classes with the same `Files.isRegularFile` -> `new Image(stream, w, h)` -> catch pattern.

**How Decorator (or a utility, pragmatically) fixes it:**

```java
public class AvatarImageLoader {
    
    public static Image load(Path imagePath, double size) {
        if (imagePath == null || !Files.isRegularFile(imagePath)) {
            return null;
        }
        try (InputStream stream = Files.newInputStream(imagePath)) {
            return new Image(stream, size, size, true, true);
        } catch (IOException e) {
            return null;
        }
    }
}
```

If you want the full Decorator pattern, you could wrap `ImageView` with decorators that add loading behavior, placeholder fallbacks, and error handling.

**Impact:** Eliminates 4-way duplication, single place to change image loading behavior.

---

## Summary: Pattern-to-Problem Map

| Pattern | Problem Solved | Files Affected | Priority |
|---------|---------------|----------------|----------|
| **Observer** | `PlayerLevel` never updates; reactive state | `Player`, `PlayerLevel`, view panels | Critical |
| **Template Method** | Duplicated `commit()` flow; double calculation in `Purchase` | `Transaction`, `Purchase`, `Sale`, `UserAccountRepository` | High |
| **Facade** (split) | `SessionService` god class (479 lines) | `SessionService` → 4-5 new services | High |
| **Chain of Responsibility** | Stringly-typed validation contract between model and view | `SessionService`, `GuiAppShell`, `UserInterface` | High |
| **Strategy** (extended) | Single hardcoded recommendation algorithm | `StockRecommendationService` | Medium |
| **Builder** | Complex constructors (8+ params), mutable `restore()` | `Exchange`, `SessionService` | Medium |
| **State** | Player levels are meaningless labels; broken transitions | `Player`, `PlayerLevel` | Medium |
| **Command** | `Exchange` tightly coupled to transaction types | `Exchange`, `Purchase`, `Sale` | Medium |
| **Iterator** (defensive) | Mutable internal lists exposed | `Stock`, `Portfolio` | Low effort, High value |
| **Decorator** / utility | Avatar loading duplicated 4x | 4 view classes | Low effort, Medium value |

Would you like me to start implementing any of these? I'd suggest beginning with the **Observer + State** combo for `Player`/`PlayerLevel` (fixes the critical bug), then the **Template Method** for `Transaction` (fixes the double calculation), and the **Chain of Responsibility** for validation (eliminates the stringly-typed contract).