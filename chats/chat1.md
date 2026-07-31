# Focus Log Android App

_Started 2026-07-31 20:33 UTC_

---

## User

FOCUS LOG — a minimal focus time tracker (Android)

CONCEPT
A punch-clock for deep work. The user selects a minimum focus time,
taps start, and physically puts the phone away. Sessions accumulate
into a permanent log. The app is a ledger with a big button — not a
productivity suite. No accounts, no cloud, no notifications, no
gamification.

DESIGN LANGUAGE
Brutalist-minimal. Near-black background, one accent color used
sparingly. Huge monospace digits. Generous spacing, no icons where a
word will do, no cards or shadows. Everything reachable in one or two
taps. Should feel like precision instrumentation, not a lifestyle app.

SCREEN 1 — CLOCK (home)
Idle state:
- Minimum-time selector: large stepper or preset chips
  (30m / 45m / 60m / 90m / custom). Remembers last choice.
- Optional tag: a single row of recent-tag chips (e.g. "maths",
  "thesis") + a "+" for a new tag. Skippable — never required.
- One dominant START button filling the lower third.
- Footer, small text: "Today 2h 40m · This week 11h 05m".

Running state:
- Elapsed time in giant digits, counting up. Neutral/red-tinted until
  the minimum is reached, then flips to green. Silent — no sound,
  no vibration, no notification, ever.
- Below the digits: the minimum ("min 60:00") and the active tag.
- Two actions:
  - Before minimum: only "TERMINATE" (understated, not alarming).
    Ends the session, recorded with status = failed.
  - After minimum: "STOP" becomes the primary action. Ends the
    session, recorded with status = completed.
- Footer totals remain visible.

SCREEN 2 — STATS (swipe or one tap from home)
- Totals row: Today · This week · This month · This year · All time.
- Charts (drawn natively, minimal styling, no chart library):
  - Focus time by hour of day (when do I work best)
  - Focus time by day of week
  - Weekly totals over time (am I trending up)
  - Optional: per-tag totals for the current month
- History list: date, start–end, duration, tag, status
  (completed / failed). Long-press or swipe an entry to delete
  (no confirmation) or edit its tag. No other editing.

DATA MODEL (source of truth)
- Append-style local log, one record per session:
  { start_epoch_ms, end_epoch_ms, duration_ms, minimum_ms,
    tag (nullable), status: completed | failed }
- Active session = a stored start timestamp + chosen minimum + tag.
  Its existence IS the running state. No background service; elapsed
  time is computed from the wall clock whenever the screen is on.
  Sessions survive app kills and reboots.
- All totals and charts are derived from the log at read time.
  Nothing aggregated is ever stored.
- Sessions attribute to the day they started. Failed attempts are
  stored fully and excluded from totals for now (this is a display
  choice, easily changed later).
- Export button: share the raw log as plain text (CSV/JSONL) via the
  standard Android share sheet.

EXPLICIT EXCLUSIONS
No streaks, goals, reminders, sounds, cloud sync, accounts, widgets,
settings screen, onboarding, or third-party libraries.

## Assistant

_[tool: questions_v2]_

## User

Questions answered:
- delete_gesture: Long-press reveals DELETE / EDIT TAG actions
- nav: Both
- fidelity: Fully functional — real ticking clock, sessions persist locally, stats derived live
- frame: Both screens side by side for review
- accent: Define accent, fail-red, and success-green as named variables/design tokens used throughout; start with cold cyan as accent. Accent must stay visually distinct from the red/green session-state colors.
- seed: Yes — a few weeks of plausible sessions
- custom_min: The minimum-time number is directly manipulable: tap it to type a value, drag/scrub it to adjust. Presets remain as chips.
- digits: JetBrains Mono
- extra: Absolutely silent app — no sounds, vibrations, notifications, or "session complete" moments, ever. The elapsed digits are neutral/red-tinted before the minimum and flip to green after; TERMINATE (before minimum) is understated, not alarming, and STOP (after minimum) becomes the primary action. Footer on the clock screen always shows "Today · This week". Tagging is optional and skippable at start — never a required step. No streaks, goals, icons where words work, cards, or shadows. All colors from the named tokens. Brutalist instrument, not a lifestyle app.

## Assistant

_[tool: read_skill_prompt]_

_[tool: dc_write]_

_[tool: dc_html_str_replace]_

_[tool: ready_for_verification]_

Focus Log is up for review — CLOCK and STATS side by side, fully functional: sessions persist locally (surviving reload), totals and charts derive live from the log, and it's seeded with ~4 weeks of plausible history. Try starting a session, scrubbing/typing the minimum, long-pressing a history entry, and EXPORT for CSV. Accent color, counting failed sessions in totals, and a fresh-install view are in Tweaks.

One caveat: swipe navigation works within each phone, but with both screens visible the text links are the natural path.

## User

Found issues — fixing…

## Assistant

_[tool: dc_js_str_replace]_

_[tool: dc_js_str_replace]_

_[tool: dc_js_str_replace]_

_[tool: ready_for_verification]_

Fixed — the minimum now stays in MM:SS everywhere (e.g. `90:00`, `120:00`), never rolling into hours; elapsed digits still roll over as before.

## User

It has too much of a shap, statment look. don't do caps everywehre just use noraml capitalisation everywhere rahter than makign it a statment piece. the start button looks so weird. round it a bit more etc. look more friendly this is a study focus log

## Assistant

_[tool: dc_html_str_replace]_

_[tool: dc_html_str_replace]_

_[tool: dc_html_str_replace]_

_[tool: dc_html_str_replace]_

_[tool: dc_html_str_replace]_

_[tool: dc_html_str_replace]_

_[tool: dc_html_str_replace]_

_[tool: dc_html_str_replace]_

_[tool: dc_html_str_replace]_

_[tool: dc_html_str_replace]_

_[tool: dc_html_str_replace]_

_[tool: dc_html_str_replace]_

_[tool: dc_html_str_replace]_

_[tool: dc_html_str_replace]_

_[tool: dc_html_str_replace]_

_[tool: dc_html_str_replace]_

_[tool: dc_js_str_replace]_

_[tool: dc_js_str_replace]_

_[tool: dc_html_str_replace]_

_[tool: dc_html_str_replace]_

_[tool: dc_html_str_replace]_

_[tool: ready_for_verification]_

Softened throughout: normal sentence capitalization everywhere (no more all-caps statements), Start/Stop buttons and Terminate rounded (28px/20px radii) with lighter weight and normal letter-spacing, and chips are now pill-shaped. Structure and colors untouched.

## User

Make sure capitalisaiton is correct and also that spacing between hour and minute is correct. also the start button still is far too tall. also the font seems too robotic monospace kinda thing i don't liek that it's not friendly

## Assistant

_[tool: dc_html_str_replace]_

_[tool: dc_html_str_replace]_

_[tool: grep]_

_[tool: dc_html_str_replace]_

_[tool: dc_html_str_replace]_

_[tool: dc_html_str_replace]_

_[tool: dc_html_str_replace]_

_[tool: dc_html_str_replace]_

_[tool: dc_html_str_replace]_

_[tool: dc_html_str_replace]_

_[tool: dc_html_str_replace]_

_[tool: dc_html_str_replace]_

_[tool: dc_html_str_replace]_

_[tool: dc_js_str_replace]_

_[tool: dc_js_str_replace]_

_[tool: ready_for_verification]_

Changes made: UI text now uses Karla (a friendly sans) while digits, times, and durations stay JetBrains Mono as instrument readouts; durations now format with proper spacing ("11h 05m" instead of "11h05"); Start/Stop buttons trimmed to 96px tall; "Min 60:00" capitalized to match your edits.

## User

Good. But the way that some things are weird sizes still gives a weird look. like why are certian elemnts so much smalelr than others. it's liek you're makgin a statment art piece rather than a minimal tool.

## Assistant

_[tool: read_file]_

_[tool: run_script]_

_[tool: ready_for_verification]_

Evened out the type scale — everything now sits on a consistent ladder (12px labels/hints, 13–14px body, nothing below 11px), the 9px micro-labels are gone, and the two display sizes are less extreme (minimum 56px, running digits 80px). Body text got slightly bigger overall so it reads like a tool, not typography art.

## User

Much better. where it says minimum thoguth that font size is far too small. adn focus log. like think abotu heirarchy. also i don't know the rle but consdier are you meant to capitalise or not capitalise h and m for house and minute or put a space after the numebr preceding it. when you do by hour of day and by day of week is that all time? maybe on the right of where it says those two have a selection to press today/week/month/year/all so it's possibel to see how thos estats varied over time. also do the heatmap thing that somehwre liek github does. the graphs too also seem too unfriednly. everything else got friednlied up and rounded but the graphs are all straight edge

## Assistant

_[tool: run_script]_

_[tool: ready_for_verification]_

Done:

- **Hierarchy** — "Focus Log"/"Stats" titles now 18px bold; "Minimum" and "Tag · Optional" are proper 14px section headers; chart labels stepped up to match.
- **Range selector** — Today/Week/Month/Year/All sits right of "By hour of day" and filters both the hour and day-of-week charts (was all-time before).
- **Heatmap** — GitHub-style 16-week daily grid below Weekly total, intensity in the accent color.
- **Friendlier graphs** — all bars have rounded tops, tag bars and heatmap cells rounded too.
- **Units** — settled on the compact convention: lowercase, no space before the unit, space between parts ("2h 40m"). It's applied consistently everywhere; if you'd rather have "2 h 40 min" (SI style), say the word.

## User

And if you do it within the first 30 seconds you can press cancel without it showing on the log

## Assistant

_[tool: dc_js_str_replace]_

_[tool: dc_html_str_replace]_

_[tool: ready_for_verification]_

Added: for the first 30 seconds of a session the button is **Cancel** (with a small "Within 30s — won't be logged" note) and discards the session entirely; after that it switches to Terminate, which logs it as failed.

## User

Save as standalone HTML: `Focus Log.dc.html`

## Assistant

_[tool: copy_files]_

_[tool: str_replace_edit]_

_[tool: super_inline_html]_

_[tool: show_html]_

_[tool: show_html]_

_[tool: get_webview_logs]_

_[tool: present_fs_item_for_download]_

Standalone file ready above — fully self-contained (fonts inlined, works offline), loads cleanly. Your log data still lives in the browser's local storage, so it persists across reloads of the same file.

