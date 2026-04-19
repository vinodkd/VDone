# VDone Backlog

Future milestones and ideas, roughly in priority order.

---

## Bugs

- ~~**Snooze/edit conflict**~~: fixed — `updateTask()` now clears `snoozedUntil` on save.
- **Fixed task time picker resets on edit**: `rememberTimePickerState` initializes before `fixedStart` loads from the ViewModel; fix with `key(fixedStart)` in `TaskDetailScreen.kt`.

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

## M13 — Google Play Store

- Create a Play developer account
- Generate a release keystore (replace debug-signed APK)
- Meet Play Store requirements: privacy policy, target API level, 64-bit, etc.
- Submit for review and publish
- Self-update handled by Play once live (no in-app update mechanism needed)

---

## Ideas / Unscoped

- **Show fixed task time in lists**: Next Tasks and All Tasks rows should display the scheduled time for fixed tasks (currently only shows title).
- **Conditional offset**: `offsetSeconds` already exists in `ConditionEntity` and DB but UI never sets it and evaluator ignores it. Add duration input (hours + minutes) to Add Condition dialog; update `ConditionEvaluator` to check `now >= refCompletedAt + offsetSeconds`.
- **Retroactive completion time**: when marking done, optionally back-date `lastCompletedAt`. Useful for conditional chains. Held — may be overkill.
- **Recur + conditional tasks**: e.g. hiking on Sat/Sun, 1hr after wake-up. Held — needs design.
- **Skip recurring task instance**: for procrastinators who need multiple reminders — "skip" means "not doing it this time, suppress for current period" without marking it done. Completing normally already handles "done for this period". Implementation: a Skip button that advances `lastCompletedAt` to now (same as done) but sets a `skipped` flag so it doesn't count as a completion in any streak/history.
- **Countdown reminders before a fixed event**: "remind me every day starting X days before event" so prep happens gradually. Not just 2 reminders — daily from day-N until the event. Design: a `countdownDays` field on fixed tasks; scheduler generates a daily alarm series starting N days out.
- **Per-task alarm sound**: same sound gets ignored over time. Add a sound picker per task (shown in task detail when schedule mode is fixed or frequency). Store sound URI in `TaskEntity`. `ReminderService` uses the task's sound instead of system default.
- **Monthly tasks alarm on 1st**: currently monthly tasks show all month with no alarm anchor. Change to: alarm fires on the 1st of each month (if `frequencyTime` set, at that time; otherwise just appears in Next Tasks on the 1st).
- **Mute alarm with lock button**: pressing the power/lock button while the alarm is sounding should silence the audio (but keep the notification/screen visible until explicitly dismissed or snoozed).
- ~~**Defer to task**~~: shipped in v1.0.18 — alarm screen shows current tasks to defer behind, plus "Other task at HH:MM" placeholder.
- **Snooze UX revisit**: replace fixed-duration snooze buttons with a "remind me in X" input (user-specified duration or time). Currently 5/10/15 min hardcoded on the alarm screen.
- **HomeViewModel tick optimization**: replace `while(true)/delay(60s)` with a `tickerFlow` combined via `SharingStarted.WhileSubscribed` so it only ticks while the home screen is visible.
- iOS app
- Cloud sync / backup
- Natural-language task entry ("remind me to call dentist tomorrow at 9am")
