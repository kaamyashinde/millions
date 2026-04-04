Below is an abstract plan you can give to Cursor.

Relevant pattern references for the architecture side:

* **Facade** simplifies access to a more complex subsystem. ([Refactoring Guru][1])
* **Strategy** lets you define interchangeable behaviors or algorithms. ([Refactoring Guru][1])
* **Builder** helps construct complex objects step by step. ([Refactoring Guru][1])
* **Observer** fits event-driven UI updates when content, filters, or progress
  change. ([Refactoring Guru][1])

---

# Learning Hub CMS Plan

## Epic

Create a CMS-backed Learning Hub that gives users access to structured educational investing content
during gameplay, with support for text, links, and video resources.

---

## User Story 1 — View learning content

As a player, I want access to educational resources so that I can learn investing concepts while
using the application.

### Acceptance Criteria

* A dedicated Learning Hub section exists in the UI.
* Users can browse learning content without leaving the main application flow.
* Learning content is organized into clear categories.
* Each learning item contains at least:

    * title
    * short explanation
    * full content or expanded explanation
    * related resources
* Resource types supported:

    * article links
    * videos
    * optional downloadable/reference material
* “Aksjer for alle” is included as a core beginner resource.
* The Learning Hub is accessible at any time during gameplay.

### Tasks

* Define Learning Hub entry point in the application navigation.
* Define page layout for category overview and content detail view.
* Create reusable content card/component structure.
* Add support for external resources and embedded media.
* Add fallback handling for missing or unavailable content.
* Ensure the page works across desktop-first layout and any required responsive breakpoints.

---

## User Story 2 — Organize content by category and level

As a player, I want content grouped by topic and difficulty so that I can find material relevant to
my level and goals.

### Acceptance Criteria

* Content is grouped into meaningful categories.
* Beginner-focused content is clearly separated from more advanced material.
* Each learning item has category metadata.
* Each learning item has difficulty metadata.
* Users can browse by category.
* Users can filter content by difficulty level.

### Suggested categories

* Basics
* How Investing Works
* Risk and Diversification
* Market Understanding
* Strategies
* Practical Learning / Simulation

### Acceptance Criteria for category design

* Categories are understandable without prior finance knowledge.
* Categories are not overly granular in the first version.
* Every content item belongs to at least one category.
* The structure supports future expansion without redesign.

### Tasks

* Define initial taxonomy for topics and difficulty.
* Create content metadata model.
* Design category overview page.
* Add filter and sorting behavior.
* Validate that beginner content appears first and is easy to access.
* Review taxonomy to avoid overlap between categories.

---

## User Story 3 — Manage learning content through a CMS

As a content maintainer, I want to manage learning content outside the application code so that
content can be updated without changing the frontend implementation.

### Acceptance Criteria

* Learning content is stored in a CMS-like structure, not hardcoded directly in UI components.
* Content entries can be created, edited, removed, and reordered.
* Resource links can be attached to a content entry.
* Multiple resource types can be associated with the same content entry.
* Content metadata is consistent across all entries.
* The frontend consumes content from a structured source.

### Tasks

* Define content entity structure.
* Define resource entity structure.
* Define category and difficulty metadata fields.
* Define relationship between content items and resources.
* Establish validation rules for required fields.
* Create example content entries for pilot topics.
* Add content loading states and error states in the frontend.

---

## User Story 4 — Present content in a way that supports learning

As a player, I want each topic page to be clear and digestible so that I can learn quickly without
being overwhelmed.

### Acceptance Criteria

* Each topic page includes:

    * title
    * short intro/summary
    * key concepts
    * optional example
    * related resources
* The page hierarchy is visually clear.
* Long content is split into readable sections.
* Related links are grouped separately from the main explanation.
* Video content is clearly marked.
* Beginner explanations are concise and jargon-light.

### Recommended topic page structure

1. Header

    * title
    * difficulty
    * category
2. Quick summary
3. Key concepts
4. Expanded explanation
5. Example or scenario
6. Related resources
7. Suggested next topics

### Tasks

* Create detail page wireframe.
* Define section hierarchy for topic pages.
* Create UI components for summary, concepts, examples, and resources.
* Add “related topics” section.
* Add visual markers for type and difficulty.
* Test readability with realistic content length.

---

## User Story 5 — Provide curated external learning resources

As a player, I want trusted external sources so that I can continue learning beyond the app.

### Acceptance Criteria

* External sources are curated, not user-generated.
* Each resource includes:

    * title
    * source name
    * type
    * short reason why it is relevant
* “Aksjer for alle” is included in the beginner section.
* Other sources are grouped by relevance and learning level.
* External resources do not overwhelm the main learning experience.

### Suggested starter source groups

#### Core beginner sources

* Oslo Børs — Aksjer for alle
* Investopedia beginner guides

#### Supplementary sources

* Nordnet educational material
* Official investor education pages
* Selected explanatory videos

### Tasks

* Collect and shortlist candidate sources.
* Define evaluation criteria for source quality.
* Tag each source by category and difficulty.
* Write short source descriptions for the UI.
* Decide which links open externally and which media can be embedded.
* Review sources for redundancy and overlap.

---

## User Story 6 — Support progression and discoverability

As a player, I want help navigating what to learn next so that I can progress naturally.

### Acceptance Criteria

* Users can move from beginner topics to related next-step topics.
* Each topic can show recommended follow-up topics.
* The system avoids dead-end pages with no onward navigation.
* The homepage highlights featured beginner content first.

### Tasks

* Define content sequencing rules.
* Add “next up” and “related topics” relationships.
* Create featured content section for the Learning Hub landing page.
* Design empty-state and first-time-user state.
* Add recently viewed or continue-learning section if time permits.

---

# Suggested information architecture

## Landing page

* Hero/introduction
* Featured beginner topics
* Categories grid
* Continue learning / recently viewed
* Highlighted external resource: Aksjer for alle

## Category page

* Category description
* Topic cards
* Difficulty filters
* Optional media/resource highlights

## Topic detail page

* Main explanation
* Key concepts
* Example/scenario
* Related resources
* Suggested next topics

---

# Suggested content model

## Content item

* id
* title
* slug
* summary
* body
* category
* difficulty
* key concepts
* example/scenario
* related topics
* featured flag
* display order
* status

## Resource item

* id
* title
* source
* type
* url or embed reference
* description
* category tags
* difficulty tags
* featured flag

---

# Recommended design patterns

Use these only where they solve a real problem.

## 1. Facade

Use a facade between the UI and the CMS/content retrieval layer so the frontend talks to one clean
interface instead of multiple content/resource/filter/progress services. Refactoring Guru describes
Facade as providing a simplified interface to a complex subsystem. ([Refactoring Guru][1])

## 2. Strategy

Use Strategy for interchangeable sorting/filtering/recommendation rules, such as:

* sort by beginner-first
* sort by featured
* recommend next topics by category
* recommend next topics by difficulty
  Refactoring Guru describes Strategy as defining a family of algorithms and making them
  interchangeable. ([Refactoring Guru][1])

## 3. Builder

Use Builder when constructing complex learning page view models from multiple pieces of content and
resource metadata. Refactoring Guru describes Builder as constructing complex objects step by
step. ([Refactoring Guru][1])

## 4. Observer

Use Observer if the UI has reactive updates for filters, progress tracking, or content state
changes. Refactoring Guru describes Observer as a subscription mechanism for notifying multiple
objects about events. ([Refactoring Guru][1])

## Avoid

* Overengineering with many patterns early
* Singleton as a default architecture choice
* Deep inheritance-heavy content models unless they solve a real problem

---

# Suggested rollout plan

## Phase 1 — Foundation

* Define taxonomy
* Define CMS content model
* Build landing page, category page, topic page
* Add starter content and resources

## Phase 2 — Content quality

* Add curated beginner and intermediate resources
* Improve summaries, examples, and related topics
* Add filtering and sorting

## Phase 3 — Learning flow

* Add progression logic
* Add recently viewed / continue learning
* Add featured pathways such as “Start here”

## Phase 4 — Polish

* Improve responsiveness
* Improve empty/error/loading states
* Refine visual hierarchy and usability

---

# Questions that matter before this is finalized

1. Is the Learning Hub meant to be a standalone page, a modal, or a panel inside gameplay?
2. Will there be admin-only content management, or is the CMS only an internal structured store for
   now?
3. Do you want progress tracking in version 1?
4. Will quizzes exist, or only articles/videos/links?
5. Should external links open in-app or in a new tab?

[1]: https://refactoring.guru/design-patterns/java "Design Patterns in Java"
