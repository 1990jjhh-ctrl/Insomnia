# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working
with code in this repository.

## Commands

Requires Java 17+ and the Android SDK on `$PATH` (or `ANDROID_HOME` set).

```
./gradlew assembleDebug          # build the debug APK
./gradlew ktlintCheck            # style
./gradlew detekt                 # quality
./gradlew test                   # unit tests
./gradlew lint                   # Android lint
./gradlew ktlintFormat           # auto-fix style issues
```

**Commit linting (always available):**

```
gitlint                          # lint the most recent commit message
gitlint --commit-msg <file>      # lint a specific message file
```

The commit-msg hook runs gitlint automatically on every commit (see
CLAUDE.md `## Commits` section for setup).

## Architecture

The project is in early development. Only the domain model is implemented;
Room, the UI layer, and the build system are still to be added.

See `docs/PRD.md` for the full product spec; it is the source of truth for
scope, model, and phasing (v1 diary → v2 analytics → v3 polish).

### Domain model

Package: `app/src/main/java/com/insomnia/diary/domain/`

- **`Protocol.kt`** — `sealed interface Protocol` with two implementors:
  `MorningProtocol` (the night just passed) and `EveningProtocol` (the day
  just passed). Derived durations (`sleepDuration`, `timeToFallAsleep`) are
  computed properties, never stored.

- **`DayEvent.kt`** — `DayEvent` is one entry in the day's event timeline.
  `EventType` wraps a label and an `isCustom` flag; standard types are
  seeded by `EventTypePreset`. `StressRange` stores a min–max stress band
  (equal min/max means flat). Per-event energy is not stored here; it is
  derived from battery checkpoints over the event's time span.

- **`Mood.kt`** — `Mood` holds a label and a (valence, arousal) point on
  the circumplex (both axes –1..+1). `FeelingPreset` seeds the reusable
  mood library with named starting points.

- **`Tracking.kt`** — shared value types: `Percentage` (0–100 inline
  class), `Substance` (name + raw amount + unit), `Dream`, `DreamRecall`,
  `AwakeEvents`, and `BatteryCheckpoint`.

Key invariants are enforced in `init` blocks: `Percentage` in [0, 100];
`StressRange` min ≤ max; `DayEvent` end not before start; `Mood`
coordinates in [–1, 1].

---

# Project Rules

These rules are binding for any agent or contributor working in this
repository. Follow them for every file you create or modify.

## Text files (.md, .txt, and prose)

- Body lines: max 80 characters.
- Titles / headings: max 60 characters.
- Break lines at logical positions (after clauses, not mid-word).

## Code and scripts (all languages)

- Max 1000 lines per file. Split anything longer into logical modules.
- Max 100 characters per line. Break at logical positions; use sensible
  alignment.
- Max 100 lines per code entity (function, class, etc.). Keep entities
  compact and focused on a single task. Split into logical entities.
- Max 3 levels of indentation. Beyond that, extract.
  - Apply a "disgust meter": the deeper the nesting, the higher the
    disgust. When tempted by a fourth level, debate whether extraction
    reads better than nesting — it almost always does.
- Names must explain what the variable holds or what the entity does.

## Documentation in code

- NEVER document _what_ the code does — code documents itself.
- ONLY document the _why_ and _how to use it_.
  - Example: a function's docstring describes how to call and use it,
    not a line-by-line account of its behavior.

## Project & stack

- Insomnia is a personal, offline Android app. See `docs/PRD.md` for the
  product spec — treat it as the source of truth for scope and model.
- App: Kotlin + Jetpack Compose. Storage: SQLite via Room.

## Kotlin / Android

- Strictly typed: lean on the type system; avoid `!!` and unchecked casts.
  Prefer immutable `val` and data classes for the domain model.
- Style/quality enforced by ktlint and detekt; code must pass both.
- Follow the official Kotlin coding conventions and Compose API
  guidelines.

## Python (auxiliary scripts only)

- The app is Kotlin. Any auxiliary Python (tooling/scripts) must be
  strictly typed (pass mypy --strict) and follow PEP 8:
  https://peps.python.org/pep-0008/

## Commits

- Commit messages are linted by gitlint; see `.gitlint`.
- Keep the subject in the imperative mood and within its length limit.
- The commit-msg hook is tracked at `.claude/hooks/commit-msg`. After a
  fresh clone, enable it once with `git config core.hooksPath.claude/hooks` and ensure gitlint is installed (`pip install gitlint`).
