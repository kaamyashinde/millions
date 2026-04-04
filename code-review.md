# Code Review: Cohesion, Coupling, and Design Patterns

> Critical evaluation of the **Millions** codebase (~170 source files).

---

## Table of Contents

1. [Design Patterns Inventory](#1-design-patterns-inventory)
2. [Cohesion Analysis](#2-cohesion-analysis)
3. [Coupling Analysis](#3-coupling-analysis)
4. [Design Pattern Overkill Evaluation](#4-design-pattern-overkill-evaluation)
5. [Other Observations](#5-other-observations)
6. [Summary Scorecard](#6-summary-scorecard)
7. [Recommendations](#7-recommendations)

---

## 1. Design Patterns Inventory

The codebase uses **11 distinct GoF / classic patterns**:

| Pattern | Where | Classes |
|---|---|---|
| **Strategy** | Recommendations, price moves, market events, chart tools | `RecommendationStrategy`, `DailyPriceMoveStrategy`, `MarketEventStrategy`, `ChartTool` |
| **Command** | Trade execution | `TradeCommand`, `BuyCommand`, `SellCommand`, `BuyUpToBudgetCommand`, `SellByQuantityCommand`, `SellUpToTargetNetCommand` |
| **Template Method** | Transaction lifecycle, chart tool base | `Transaction.commit()`, `AbstractChartTool` |
| **Observer** | Player state changes, level-up notifications | `PlayerObserver`, `PlayerLevelObserver`, `LevelUpNotificationObserver` |
| **Decorator** | Image loading pipeline | `ImageLoader` -> `ImageLoaderDecorator` -> `ValidatingImageLoader`, `FallbackImageLoader`, `FileImageLoader` |
| **Builder** | Exchange construction | `Exchange.Builder` |
| **Factory** | Session wiring, workspace creation | `SessionServiceFactory`, `SessionWorkspaceFactory` |
| **Facade** | Session coordination, learning content | `SessionService`, `LearningContentStore` |
| **State** | Player progression tiers | `PlayerLevel` (sealed) -> `NoviceLevel`, `InvestorLevel`, `SpeculatorLevel` |
| **Chain of Responsibility** | Registration validation | `RegistrationValidator.then()` chain |
| **Singleton** | Level instances | `NoviceLevel.INSTANCE`, `InvestorLevel.INSTANCE`, `SpeculatorLevel.INSTANCE` |

That is a lot of patterns for a project of this size. The question is whether each one earns its complexity.

---

## 2. Cohesion Analysis

### Strong Cohesion (Good)

- **`Stock`, `Share`, `Player`** — Focused domain entities with clear single responsibilities. Each manages its own state and exposes a clean API.
- **`PurchaseCalculator` / `SaleCalculator`** — Each calculator does exactly one thing: compute financial details for one transaction type.
- **`TrendRecommendationStrategy`, `MeanReversionStrategy`, `MomentumRecommendationStrategy`** — Each strategy encapsulates one algorithm. Easy to test in isolation.
- **`MarketEvent` (record)** — Immutable value object. No behaviour leakage.
- **`ActiveSession` (record)** — Clean data carrier, no logic.
- **`ValidationResult` (sealed)** — `Success`/`Failure` discriminated union. Textbook use of modern Java.
- **`AuthService`** — Focused on registration and login. Good single-responsibility boundary.
- **Individual `TradeCommand` implementations** — Each command class has one job.

### Weak Cohesion (Problematic)

**`Exchange` — God class** \
This is the biggest cohesion violation in the project. `Exchange` currently handles:

1. Asset registry (stocks, funds, combined asset map)
2. Search/lookup (`findStocks`, `findFunds`, `findAssets`, `getStock`, `getFund`, `getAsset`)
3. Trade execution delegation (`buy`, `buyUpToBudget`, `sell`, `sellByQuantity`, `sellUpToTargetNet`)
4. Market simulation (`advance`, `advanceOneDay`)
5. Market analytics (`getGainers`, `getLosers`)
6. Event history management (`getMarketEventHistory`, `getMarketEventsForStock`)

This class has **~520 lines** and **6 distinct responsibilities**. It should be broken into focused services (e.g., `AssetRegistry`, `TradeExecutor`, `MarketSimulator`).

**`SessionService` — Overloaded facade** \
Although it delegates to five sub-services, the facade itself exposes **18+ public methods** spanning authentication, profile management, game persistence, saved runs, leaderboard queries, and preferences. The "thin facade" intent is good, but the surface area is still too wide. A caller importing `SessionService` gains access to unrelated concerns.

**`GameStateMapper` (~280 lines)** \
This does bidirectional mapping for players, exchanges, shares, transactions, savings plans, and market events. It is doing two jobs (domain-to-snapshot AND snapshot-to-domain) for six entity types. Could be split by entity type or by direction.

**`Portfolio`** \
Mixes basic collection management (`addShare`, `removeShare`, `getShares`) with complex FIFO sale slicing logic (`buildNextFifoSaleSlice`, `buildNextFifoSliceForTargetNet`, `removeFifoSliceForSale`) and financial valuation (`getNetWorth`, `calculateShareValue`). The FIFO logic is a separate responsibility that could be extracted.

**`LearningContentStore`** \
All content is hardcoded as `private static final` fields. This is essentially a data dump masquerading as a class. If a new article is added, you edit Java source code instead of a data file. Not cohesive in the sense of "class does one thing well" — it's a rigid catalogue with no separation of data from lookup logic.

---

## 3. Coupling Analysis

### Low Coupling (Good)

- **Strategy interfaces** (`RecommendationStrategy`, `DailyPriceMoveStrategy`, `MarketEventStrategy`) decouple algorithms from their consumers. Swapping strategies requires zero changes to `Exchange` or `StockRecommendationService`.
- **`InvestableAsset` interface** abstracts over `Stock` and `Fund`, allowing polymorphic treatment in `Share`, `Portfolio`, and `Exchange`.
- **`SessionServiceFactory`** centralises wiring. No domain class knows how persistence is assembled.
- **Records and sealed types** enforce immutability and closed hierarchies without leaking internals.
- **Consistent use of `BigDecimal`** avoids floating-point coupling issues in financial calculations.

### High Coupling (Problematic)

**`Exchange` hard-wires all command instantiations** \
The Command pattern is supposed to decouple execution from the caller. But `Exchange.buy()` does `new BuyCommand(this, symbol, quantity).execute(player)` — the exchange creates the command and immediately executes it. The command is never passed to anyone else. The `describe()` method on `TradeCommand` appears unused. The decoupling benefit is zero.

```java
// Exchange.java:258-259 — command created and executed in one line
public Transaction buy(String symbol, BigDecimal quantity, Player player) {
    return new BuyCommand(this, symbol, quantity).execute(player).getFirst();
}
```

**Unsafe downcast in `Purchase` and `Sale`** \
Both transaction subclasses pass a typed calculator to the parent, then cast it back:

```java
// Purchase.java:27-28
super(share, day, new PurchaseCalculator(share));
this.purchaseCalc = (PurchaseCalculator) this.getCalculator();
```

This defeats the purpose of the `TransactionCalculator` abstraction. The parent stores a `TransactionCalculator`, but the child secretly knows the concrete type. If anyone changes the parent's storage, this cast breaks silently. The child should own its calculator directly instead of round-tripping through the parent.

**`Player` constructor auto-registers `PlayerLevelObserver`** \
Hidden coupling — constructing a `Player` secretly creates an observer. Callers cannot opt out or substitute. This is a disguised method call pretending to be an extensible pattern.

**`PlayerLevel.checkTransition()` default method knows all implementations** \
The interface's default method directly references `SpeculatorLevel.INSTANCE` and `InvestorLevel.INSTANCE`. This means adding a new level requires modifying the interface (or overriding the default everywhere), which violates the Open/Closed Principle.

**`Portfolio` depends on `SaleCalculator` for net worth** \
A data-oriented class (`Portfolio`) creates transaction calculator instances to compute value. This couples the portfolio to the specific commission/tax model in `SaleCalculator`. If valuation rules change, `Portfolio` breaks.

**`SessionService.listLeaderboardEntries()` restores full game states** \
To show a simple leaderboard, this method loads and fully deserializes every user's exchange and player objects. This is an extremely heavy operation for what should be a lightweight query.

---

## 4. Design Pattern Overkill Evaluation

### Justified Patterns

| Pattern | Verdict | Why |
|---|---|---|
| **Strategy** (recommendations, price moves, market events) | **Justified** | Algorithms genuinely vary and need to be independently testable. Multiple implementations exist. |
| **Template Method** (`Transaction.commit()`) | **Justified** | Clean lifecycle: guard double-commit -> validate -> execute -> archive. Prevents subclasses from skipping steps. |
| **Builder** (`Exchange.Builder`) | **Justified** | 9 constructor parameters, sensible defaults, immutable result. Builder is the right tool. |
| **Factory** (`SessionServiceFactory`) | **Justified** | Genuinely complex wiring of 8+ dependencies. Without it, every entry point duplicates assembly code. |
| **Sealed types** (`PlayerLevel`, `ValidationResult`) | **Justified** | Modern Java feature used appropriately. Exhaustive pattern matching, compile-time safety. |
| **Facade** (`SessionService`) | **Justified in concept** | The sub-services extraction was a good move. The facade surface area is just too wide (see cohesion). |

### Overkill / Questionable Patterns

**Command Pattern for trades — OVERKILL** \
The commands are created and immediately executed inside `Exchange`. They are never:
- queued for later
- logged via `describe()`
- batched
- undone
- passed to external callers

The `describe()` method on `TradeCommand` is dead code — nothing calls it. This is **pattern for pattern's sake**. The same logic worked fine as private methods on `Exchange` or as simple helper classes. The 6-class hierarchy (1 interface + 5 implementations) adds navigational overhead with no practical benefit.

**Decorator for image loading — OVERKILL** \
The decorator chain is: `FallbackImageLoader` wraps `ValidatingImageLoader` wraps `FileImageLoader`. This is **5 classes** (1 interface, 1 abstract decorator, 3 concrete) to express:

```
if path is null or missing -> return fallback
else -> load from file, or return fallback on IOException
```

That is a 3-line `if` statement distributed across 5 files. The decorators are never dynamically composed or recomposed at runtime. The chain is always the same. A single `ImageLoader` implementation with guarded logic would be simpler and equally testable.

**Observer for player level recalculation — OVERKILL** \
There is exactly **one** observer (`PlayerLevelObserver`), and it is hardcoded in the `Player` constructor. It calls `player.recalculateLevel()`, which `Player` already exposes publicly. This is a self-referential cycle disguised as a pattern. Just calling `recalculateLevel()` at the end of `addMoney()` and `withdrawMoney()` would be simpler and more transparent.

Note: the Observer pattern _is_ used legitimately elsewhere — `LevelUpNotificationObserver` registered by the view layer is a good use. The problem is specifically `PlayerLevelObserver`.

**Chain of Responsibility for validation — BORDERLINE** \
The chain is statically assembled with exactly 3 links and never changes:

```java
new UsernameValidator().then(new PinValidator()).then(new StartingMoneyValidator());
```

It reads well and is testable, but it is also a glorified sequential `if` chain. With only 3 validators and no dynamic composition, a simple `validateRegistration(username, pin, money)` method calling each validator in sequence would be equally clean. Not terrible, but arguably over-engineered for 3 checks.

**Singleton + separate `PlayerLevels` utility — UNNECESSARY COMPLEXITY** \
The singleton `INSTANCE` fields on each level class require a separate `PlayerLevels` utility class to avoid circular class-loading issues. This is complexity caused by the pattern choice. An enum with abstract methods would have been simpler and avoided the problem entirely.

---

## 5. Other Observations

### Typo
`Transaction.commited` should be `committed` (double t). This propagates to `isCommited()`. Minor but affects API consistency.

### Inconsistent validation approaches
- `model.utils.Validator` provides `checkNotNull()` and `requirePositive()` used across the model.
- `model.session.validation.*` provides a Chain-of-Responsibility validation pipeline.
- Some classes use `Objects.requireNonNull()` directly.

Three different validation styles in one project.

### Incomplete MVC separation
Only **1 controller** class exists (`RegularSavingsPanelController`). All other view panels appear to interact with model objects directly. This suggests the controller layer was started but never completed, leaving the architecture in an inconsistent state.

### CLI/GUI coexistence
The `cli.UserInterface` class exists alongside the full JavaFX GUI. If the CLI is dead code, it should be removed. If it is maintained, it duplicates interaction logic without sharing controllers.

### Redundant `checkNotNullOnShare()` in `Portfolio`
```java
private static void checkNotNullOnShare(Share share) {
    checkNotNull(share, "Share");
}
```
This private method wraps a one-liner that is already readable. It adds a layer of indirection with no benefit.

### `LearningContentStore` is not extensible
All learning content is Java source code. Adding a new article requires recompiling the application. A data-driven approach (JSON/YAML files loaded at startup) would be more maintainable.

---

## 6. Summary Scorecard

| Dimension | Grade | Notes |
|---|---|---|
| **Model cohesion** (core entities) | **B+** | `Stock`, `Share`, `Player`, calculators are clean. `Exchange` and `Portfolio` need decomposition. |
| **Session/persistence cohesion** | **B-** | Good sub-service extraction, but `SessionService` facade and `GameStateMapper` are too wide. |
| **View cohesion** | **B** | Panels are focused. Missing controllers leave some business logic in view code. |
| **Coupling between model classes** | **B** | Strategy interfaces are good. `Exchange`<->`TradeCommand` coupling negates the Command pattern. |
| **Coupling across layers** | **B-** | `Portfolio` depends on `SaleCalculator`; `SessionService.listLeaderboardEntries()` is excessively heavy. |
| **Design pattern usage** | **C+** | 11 patterns for ~170 files is pattern-heavy. ~4 patterns (Command, Decorator, Observer for level, Singleton+utility) add complexity without clear payoff. |

**Overall: B-** — The architecture has a solid foundation and good intentions, but several patterns are used ceremonially rather than solving real problems.

---

## 7. Recommendations

### High Priority

1. **Decompose `Exchange`** — Extract an `AssetRegistry` (lookup), a `MarketSimulator` (advance + events), and move trade methods to caller-side or a `TradeExecutor`. The exchange should be a market, not a god object.

2. **Simplify trade execution** — Either have callers create commands directly (justifying the Command pattern with real decoupling), or remove the command classes and use simple methods. The current setup has the worst of both worlds: ceremony of commands with coupling of direct calls.

3. **Fix the `Purchase`/`Sale` downcast** — Let each subclass own its typed calculator directly as a field. Remove the upcast through the parent. The parent `Transaction` can still expose `getCalculator()` via the interface type for polymorphic callers.

### Medium Priority

4. **Replace image decorator chain with a single class** — A `ProfileImageLoader` with inline null/existence checks is simpler and equally testable.

5. **Remove `PlayerLevelObserver` auto-registration** — Call `recalculateLevel()` directly in `addMoney()` and `withdrawMoney()`. Keep the Observer pattern for the legitimate use in `LevelUpNotificationObserver`.

6. **Make `PlayerLevel` an enum** — The sealed interface + singletons + `PlayerLevels` utility adds three types to avoid a class-loading problem that enums don't have. An enum with abstract methods is simpler.

7. **Extract FIFO logic from `Portfolio`** — Move `buildNextFifoSaleSlice`, `buildNextFifoSliceForTargetNet`, and `removeFifoSliceForSale` into a `FifoLotManager` or similar. `Portfolio` should be a simple collection.

### Low Priority

8. **Narrow `SessionService` facade** — Group related methods into sub-facades (e.g., `sessionService.runs().save(...)`, `sessionService.profile().updateDisplayName(...)`). This keeps the delegation benefit while reducing API surface per object.

9. **Externalise learning content** — Move `LearningContentStore` data to JSON or YAML resource files loaded at startup.

10. **Complete or remove the controller layer** — Either add controllers for all panels (making the MVC consistent) or remove `RegularSavingsPanelController` and acknowledge the project uses a Model-View pattern.

11. **Standardise validation** — Pick one approach (`Validator.checkNotNull()` or `Objects.requireNonNull()`) and use it consistently.

12. **Fix the `commited` typo** — Rename to `committed` / `isCommitted()`.
