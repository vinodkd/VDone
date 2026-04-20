# VDone Backlog

Future milestones and ideas, roughly in priority order.

---

## Bugs

- ~~**Snooze/edit conflict**~~: fixed — `updateTask()` now clears `snoozedUntil` on save.
- ~~**Fixed task time picker resets on edit**~~: fixed in v1.0.19 with `key(fixedStart)` in `TaskDetailScreen.kt`.
- ~~**Skip only on alarm screen**~~: fixed in v1.0.27 — Skip button now appears on recurring task cards in Next Tasks.
- ~~**Rebrand to periwinkle blue**~~: shipped in v1.0.25 — app theme and website updated.
- **App icon rebrand**: launcher icon and notification icon still use the old green/teal. Needs new icon assets in periwinkle blue.
- ~~**Conditional alarms never fire proactively**~~: fixed in v1.0.29 — alarm is scheduled immediately when the blocking task is marked done.
- **Show mode silently drops alarms**: when Show Mode is active, `ReminderReceiver` discards the alarm with no rescheduling and no indication to the user. Suppressed alarms should either be listed somewhere (e.g. an "overdue while in Show Mode" badge) or rescheduled to fire immediately when Show Mode is turned off.

---

## ~~M10 — Search~~ (done)

- ~~Add a search bar to the All Tasks tab~~
- ~~Filter task list by title and notes as the user types~~
- ~~Clear button to dismiss search~~

---

## ~~M11 — Days-of-week recurring tasks~~ (done)

- ~~Add days-of-week picker for daily recurring tasks (Su M T W Th F S chips + Weekdays shortcut)~~
- ~~New `frequencyDays` bitmask DB column (migration 10→11)~~
- ~~FrequencyChecker respects selected days~~
- ~~AlarmScheduler advances to next matching day~~

---

## ~~M12 — Home screen widget~~ (done)

- ~~Jetpack Glance widget showing up to 5 tasks due today~~
- ~~Tap task → opens edit screen; tap + → new task~~
- ~~Refreshes immediately on task create/update/toggle~~
- ~~Mark Done button added to task detail screen~~

---

## M14 — Today's Tasks view redesign

- Rename "Next Tasks" tab → "Today's Tasks"; rename "All Tasks" tab → "Planned Tasks"
- Today's Tasks filter: show only tasks due today **or overdue** (not future-dated); label overdue items distinctly
- Remove mark-done checkbox from Planned Tasks rows (recurring tasks are templates, not instances); done/skip/snooze live in Today's Tasks only
- Add `isActive: Boolean` flag to `TaskEntity` (DB migration); deactivated tasks never appear in Today's Tasks or fire alarms
- **Deactivate** (ban icon) on every task row and in the edit screen, uniform across all task types; tapping toggles active/inactive
- **Delete** moves to long-press; long-press enters multi-select mode — checkboxes replace the done indicator, toolbar shows a delete action for all selected tasks; exit selection mode via back or deselecting all
- Fix the root issue from issue #5: a recurring task marked done in Planned Tasks currently leaves its next alarm showing in Today's Tasks with no visual distinction — the filter + status model change above resolves this

---

## ~~M15 — Recurring task default alarm times~~ (done)

- ~~Daily recurring tasks with no time set: default alarm to **08:00 the next day**~~
- ~~Weekly/monthly/yearly: default to **08:00**; monthly fires on the 1st~~
- ~~Shipped in v1.0.28~~

---

## M16 — Full schedule specification for weekly/monthly/yearly tasks

Currently the lower-level time units are either implicit (weekly fires on the same weekday as creation; yearly fires on the same month+day as creation) or hardcoded (monthly always fires on the 1st). Users should be able to specify them explicitly in the task edit screen:

- **Weekly**: add a single day-of-week picker (Mon–Sun) — "fire every week on [day]". Stored in a new `frequencyDayOfWeek` field (or reuse `frequencyDays` bitmask limited to one selection).
- **Monthly**: add a day-of-month picker (1–28, plus "last day of month") — currently hardcoded to 1st in `nextMonthlyTrigger`.
- **Yearly**: add a month + day-of-month picker — currently fires on the anniversary of task creation.
- All three should default to today's equivalent (current weekday / current day-of-month / current month+day) so existing behavior is preserved if the user doesn't change them.
- Requires DB migration and updates to `AlarmScheduler`, `FrequencyChecker`, and `TaskDetailScreen`.

---

## M13 — Google Play Store

- Create a Play developer account
- Generate a release keystore (replace debug-signed APK)
- Meet Play Store requirements: privacy policy, target API level, 64-bit, etc.
- Submit for review and publish
- Self-update handled by Play once live (no in-app update mechanism needed)

---

## Ideas / Unscoped

- **"Started" / in-progress task state** — fully designed, ready to build when M14 is done:
  - **New status**: `"doing"` alongside `"todo"` and `"done"`. New DB field `startedAt: Long?` (records when started; duration = doneAt − startedAt; also counts procrastination attempts). `autoDone: Boolean` flag distinguishes auto-completed from explicitly marked done.
  - **Entry points**: "Start" button on alarm screen (alongside Snooze/Done); button on Next Tasks / Today's Tasks card (alongside Done/Skip).
  - **Alarm behavior**: cancel alarm on start. While "doing", suppress any re-alarm silently. On done/auto-done, advance recurring schedule and trigger unblocked conditional tasks as normal.
  - **Conditions / defer**: tasks deferred behind a "doing" task wait for **done** (including auto-done), not for "started". Same as current behavior.
  - **Auto-complete at EOD**: tasks still "doing" at end of day are auto-completed (`autoDone = true`, status = "done"). Recurring tasks advance their schedule. This fires **lazily on app foreground** (`onResume`): check for `status == "doing"` where `startedAt < today's midnight`; complete them before any UI renders. While the app is actively in the foreground (e.g. late-night cramming), no auto-complete fires — task stays in Doing until the user marks it done or leaves and returns. The 60s tick handles all time-sensitive UI updates while in-app.
  - **Tab structure change** (part of M14 / M17 combined): replace current 3-tab layout (Next Tasks / All Tasks / Loops) with 5 tabs: **Plan** (all tasks including future; replaces All Tasks) · **Next** (today + overdue) · **Doing** (in-progress tasks) · **Waiting** (replaces Loops) · **Done** (today's completions only).
  - **DB change**: add `startedAt: Long?` and `autoDone: Boolean` to `TaskEntity`; migration needed.



- ~~**Show fixed task time in lists**~~: shipped in v1.0.20 — schedule labels on all task rows.
- ~~**Conditional offset**~~: shipped in v1.0.21 — hours+minutes input in Add Condition dialog; evaluator enforces the offset.
- ~~**Monthly tasks alarm on 1st**~~: shipped in v1.0.22 — alarm fires on 1st of month; FrequencyChecker only surfaces monthly tasks on the 1st.
- ~~**Skip recurring task instance**~~: shipped in v1.0.23 — "Skip this time" button on alarm screen.
- **Retroactive completion time**: when marking done, optionally back-date `lastCompletedAt`. Useful for conditional chains. Held — may be overkill.
- **Recur + conditional tasks**: e.g. hiking on Sat/Sun, 1hr after wake-up. Held — needs design.
- **Countdown reminders before a fixed event**: "remind me every day starting X days before event" so prep happens gradually. Not just 2 reminders — daily from day-N until the event. Design: a `countdownDays` field on fixed tasks; scheduler generates a daily alarm series starting N days out.
- ~~**Per-task alarm sound**~~: shipped in v1.0.24 — sound picker in task detail for fixed/recurring tasks.
- **Mute alarm with lock button**: pressing the power/lock button while the alarm is sounding should silence the audio (but keep the notification/screen visible until explicitly dismissed or snoozed).
- ~~**Defer to task**~~: shipped in v1.0.18 — alarm screen shows current tasks to defer behind, plus "Other task at HH:MM" placeholder.
- **Snooze UX revisit**: replace fixed-duration snooze buttons with a "remind me in X" input (user-specified duration or time). Currently 5/10/15 min hardcoded on the alarm screen.
- **HomeViewModel tick optimization**: replace `while(true)/delay(60s)` with a `tickerFlow` combined via `SharingStarted.WhileSubscribed` so it only ticks while the home screen is visible.
- iOS app
- Cloud sync / backup
- Natural-language task entry ("remind me to call dentist tomorrow at 9am")
