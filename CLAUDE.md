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
