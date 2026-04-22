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
- ~~**Show mode silently drops alarms**~~: fixed in v1.0.30 — suppressed alarms are queued and rescheduled immediately when Show Mode is turned off.

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

## ~~M14 — Pre-redesign improvements~~ (done, v1.0.32–33)

- ~~**Filter Next Tasks to today + overdue**~~: snoozed tasks hidden; `fixedStart <= endOfToday` already filtered future fixed tasks.
- ~~**Deactivate flag**~~: toggle switch on task rows; DB migration 12→13; inactive tasks excluded from Start and alarms.
- ~~**Long-press to delete**~~: replaces dedicated delete icon; confirmation dialog on long-press.
- **Multi-select delete**: deferred — long-press now triggers delete confirm. If multi-select lands, it will move delete to the toolbar.

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

## ~~M17 — 5-tab redesign + Started state~~ (done, v1.0.34)

- ~~5 tabs: Plan · Start · Doing · Waiting · Done~~
- ~~`status = "doing"`, `startedAt`, `autoDone` fields (DB migration 13→14)~~
- ~~Start tab: ▶ start + ↻ skip icons; fixed-width action column for alignment~~
- ~~Doing tab: in-progress tasks; Done button~~
- ~~Done tab: today's completions via `updatedAt` / `lastCompletedAt`~~
- ~~Auto-done on `onResume`: overnight "doing" tasks completed automatically~~
- ~~Plan tab: no status toggle; task definitions only~~

---

## Ideas / Unscoped

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
## M18 — First-run tour / onboarding

The 5-tab structure and icon-based actions (▶ start, ↻ skip) are not self-evident to new users. A lightweight tour should explain the core model before they hit any real tasks.

- **What to cover**: the 5 tabs and their purpose (Plan = define, Next = act, Doing = in progress, Waiting = blocked, Done = today's wins); the Start/Skip icons on Next cards; long-press to delete in Plan.
- **Trigger**: shown once on first launch (flag in SharedPrefs); re-accessible from Settings.
- **Format**: overlay coach marks (highlight + tooltip per element) or a simple swipeable intro card sequence — whichever is less intrusive.
- **Scope**: Android only for now; iOS gets its own tour when the iOS app is built.

---

- iOS app
- Cloud sync / backup
- Natural-language task entry ("remind me to call dentist tomorrow at 9am")
