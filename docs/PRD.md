# Insomnia — Product Requirements (PRD)

## Overview

Insomnia is a personal, offline Android app for keeping a structured sleep
and wellbeing diary. The user records a *morning* protocol (about the night
just passed) and an *evening* protocol (about the day just passed), then
explores correlations between behaviours (alcohol, medication, naps, social
load, stress) and outcomes (sleep duration, recovery, mood).

It is a single-user app for the author's own device. It is **not** published
to Google Play and has no backend or cloud account.

## Goals

- Make daily logging fast enough to sustain as a habit.
- Capture data losslessly in the most compact on-device form.
- Reveal correlations between any two tracked variables, including across
  days (e.g. last night's alcohol vs this morning's mood).
- Double as an alarm clock so wake/bedtime data is captured automatically
  and the app actively supports a healthy wind-down.

## Non-goals

- No publishing, accounts, multi-user, or cloud sync.
- No medical/clinical claims; this is a self-tracking aid.
- No social or sharing features.

## Platform and stack

- **Language/UI:** Kotlin + Jetpack Compose.
- **Storage:** SQLite via Room.
- **Min/target SDK:** target the latest stable Android; minSdk 26
  (Android 8) for `java.time`. Lower minSdk only with desugaring.
- **Tooling:** ktlint + detekt (style/quality), gitlint (commits). These
  enforce the repository rules in CLAUDE.md.
- The original Python prototype was ported to Kotlin under
  `app/src/main/java/com/insomnia/diary/domain/` and removed; this PRD is
  the source of truth for the model.

## Domain model

### Time and timestamps

- Points in time are stored as full timestamps (date + time), not a bare
  clock time. This removes midnight-wrap ambiguity and lets the app
  sanity-check an entry (e.g. asleep after in-bed, wake after asleep).
- Durations are derived from timestamp differences, never stored as a
  clock time. No silent 24h wrap.
- An entry has no separate `date` field; its day is derived from its
  timestamps. Backfilling past days is allowed.

### Scales

- All subjective intensities use a single **0–100%** integer scale:
  recovery, productivity, stress, and battery (energy remaining).
- Mood is a point on a 2D grid, not a percentage (see below).
- Exhaustion is not a separate field; it is the day-end battery reading.

### Moods and the feeling grid

- A mood is a point on the valence/arousal circumplex: `valence`
  (-1 unpleasant … +1 pleasant) and `arousal` (-1 calm … +1 activated).
- The UI is a 2D grid the user taps to place a point.
- An entry may hold **multiple** moods (e.g. content *and* lonely).
- Named presets (the original `Feeling` set: calm, content, active,
  happy, tense, stressed, angry, exhausted, sad, …) seed a mood library
  and give starting points on the grid.
- The user can add **custom** moods (e.g. "lonely") with a name and grid
  position; custom moods are saved to the library for reuse.

### Morning protocol

- One or more current moods (on waking).
- Recovery (0–100%).
- In-bed timestamp and out-of-bed timestamp.
- Asleep timestamp and wake timestamp.
- Subjective sleep-onset latency (how long it *felt* to fall asleep).
- Awake events: count and total time awake during the night.
- Derived: time-to-asleep, sleep duration (span minus time awake).
- Medication taken before bed (see substances).
- Dream recall: type from {nightmare, anxiety, processing, abstract,
  lucid, no memory} plus optional note.

### Evening protocol

- One or more current moods (before bed).
- Productivity (0–100%).
- Alcohol consumed (see substances).
- Day context (see below).
- Day timeline of events (see below) — the core of the day's record.
- Battery checkpoints across the day (see energy).

### Substances (alcohol and medication)

- Stored losslessly as raw amount + unit per item, e.g.
  (name="Beer", amount=0.5, unit="L"), (name="Ibuprofen", amount=400,
  unit="mg").
- A field accepts none, one, or many items.
- Correlation uses the raw amount per named substance; no normalisation
  in v1 (may be added later).

### Day context

- Structured fields: day-type (workday, vacation, sick, holiday, weekend,
  …) and location.
- Plus free-form tags for anything that does not fit the structure.
- Captures "what the day was to me" for later filtering/correlation.
- People are recorded as event attendees (see day timeline), not here.

### Day timeline (events)

- The waking day is an ordered list of **events**; this single list
  replaces the former separate social-event and stress lists.
- Each event has:
  - a type from an **event-type library**: standard types (work, shower,
    walk, travel, meal, meeting, commute, nap, …) plus custom user types,
    saved for reuse;
  - a start and end time (a range);
  - a **stress range** (min–max %); equal min and max means a flat level;
  - optional **attendees** (people), enabling "who drains me" analysis;
  - an optional note.
- Naps are events of type *nap*; sleep stats locate them by type.
- Per-event energy is not entered; it is derived from battery checkpoints
  over the event's interval (see energy).

### Stress (derived)

- Event stress ranges form a stress band across the day, preserving the
  intra-day shape (when it spiked).
- The headline daily stress is a **duration-weighted average** (each event
  weighted by its length); the daily **peak** is also kept.
- Band semantics for the UI: 0% ≈ nonfunctional; 10–30% low activity;
  30–50% normal; 50–70% rising stress, first symptoms; 70–100% unable to
  think straight; 100% ≈ losing consciousness.

### Energy (battery)

- Energy is tracked as **battery checkpoints**: timestamped readings of
  battery remaining on the same 0–100% scale (0% empty, 100% full).
- The user drops a few readings across the day rather than rating every
  event; drain or recharge is derived between consecutive checkpoints.
- An event's energy cost is the battery change over its interval, so
  events and battery correlate through time with no extra input.
- The bedtime checkpoint is the day-end energy level and stands in for a
  separate exhaustion field.

## Features

### Logging UI

- Compose forms for the two protocols, optimised for speed.
- Mood entry via the tappable valence/arousal grid with preset and
  custom-mood shortcuts.
- Build the day timeline by adding typed events (stress range,
  attendees) and dropping battery checkpoints; both kept to few taps.
- Add/edit/delete past entries; browse history (calendar/list).
- Entry-level sanity checks on timestamps before saving.

### Alarm clock and reminders

The app doubles as an alarm clock; alarms can also act as protocol
reminders.

- **Bedtime alarm:** fires ~1h before the intended sleep time. It prompts
  the user to reflect on the day and fill the evening protocol, and warns
  that continued doomscrolling is counterproductive — time to wind down.
- **Wake alarm:** a normal waking alarm. On dismissal the app opens and:
  - proposes *left-bed time* = now,
  - proposes *went-to-bed time* = 1h after yesterday's bedtime alarm,
  - sets *wake time* = when the last alarm rang / snooze was cancelled,
  - asks for subjective sleep-onset latency.
  All proposed values are adjustable.
- **Snooze:** supported for both bedtime and wake alarms.
- Feasibility: exact alarms need `USE_EXACT_ALARM`/`SCHEDULE_EXACT_ALARM`
  (Android 12+) and `AlarmManager.setAlarmClock`; the alarm screen uses a
  full-screen intent. Reminders are local notifications.

### Wind-down phone lock

- After the bedtime alarm is stopped and the evening protocol is filled,
  the app can lock the phone once a user-set timer elapses, to discourage
  late-night use.
- Feasibility: a normal app cannot lock the device. This requires the
  **Device Admin** permission (granted once) so the app may call
  `DevicePolicyManager.lockNow()`. Documented as an explicit opt-in.

### Analytics

- **Configurable lag:** choose variable A, variable B, and a time offset
  (same day, evening→next morning, or an N-day lag) to test whether X
  today affects Y later.
- **Visualisations:** scatter + trend line (with correlation
  coefficient), time series over a date range, a correlation matrix
  (heatmap across all variables), and distributions (histogram/box plot,
  optionally split by a filter).
- **Filters:** restrict any analysis by substance, event type, attendee,
  day-type or tag, e.g. alcohol days vs non-alcohol days, or days with a
  given meeting.

### Backup and export

- Manual export, user-triggered, written to a file the user copies off
  device.
- Formats: full **SQLite DB** file (lossless restore) and **JSON**
  (structured, human-readable, re-importable).
- Import/restore from those files.

### Privacy

- The app locks on open with **biometric or PIN** (BiometricPrompt),
  protecting data even when the phone is unlocked.
- All data stays on-device; no network permission required for core use.

## Storage notes

- Normalised Room schema: entries, moods (+ a mood library), substances,
  events (+ an event-type library) with attendees, battery checkpoints,
  day-context tags, and alarm settings.
- Timestamps stored as epoch values; enums as stable string codes.
- Compactness comes from normalisation and integer/epoch encoding while
  remaining fully reconstructible (lossless).

## Non-functional requirements

- Fully offline; no account or connectivity needed.
- Responsive logging (sub-second form interactions).
- Strict typing and the repository code rules (see CLAUDE.md): file/line/
  entity size limits, ≤3 indents, why-not-what documentation.

## Phasing

- **v1 — usable diary:** domain model, logging UI (mood grid, event
  timeline, battery checkpoints), Room storage, history/edit/backfill,
  export/import, alarms (bedtime + wake) with auto-proposed timestamps,
  app lock.
- **v2 — analytics:** scatter/time-series/matrix/distribution,
  configurable lag, and filters.
- **v3 — polish:** wind-down Device-Admin lock, refinements, optional
  substance normalisation.

## Open questions and assumptions

- Substance normalisation (standard drinks / active mg) is deferred to a
  later version; v1 stores raw amounts only.
- Battery-checkpoint cadence (how often the user logs % left, and whether
  the app prompts) to be tuned during v1 UI design.
- Exact minSdk depends on whether desugaring is preferred over requiring
  Android 8.

## Out of scope

- Cloud sync, accounts, sharing, Play Store distribution.
- Wearable/sensor integration (possible future work, not planned).
