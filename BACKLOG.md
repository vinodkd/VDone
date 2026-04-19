# VDone Backlog

Future milestones and ideas, roughly in priority order.

---

## Bugs

- ~~**Snooze/edit conflict**~~: fixed — `updateTask()` now clears `snoozedUntil` on save.
- ~~**Fixed task time picker resets on edit**~~: fixed in v1.0.19 with `key(fixedStart)` in `TaskDetailScreen.kt`.
- **Skip only on alarm screen**: "Skip this time" for recurring tasks is only reachable via the full-screen alarm. Should also appear in the Next Tasks row (swipe action or button) and in the task edit screen.
- ~~**Rebrand to periwinkle blue**~~: shipped in v1.0.25 — app theme and website updated.
- **App icon rebrand**: launcher icon and notification icon still use the old green/teal. Needs new icon assets in periwinkle blue.

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
