# VDone Backlog

Future milestones and ideas, roughly in priority order.

---

## Bugs

- ~~**Snooze/edit conflict**~~: fixed — `updateTask()` now clears `snoozedUntil` on save.

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

- **HomeViewModel tick optimization**: replace `while(true)/delay(60s)` with a `tickerFlow` combined via `SharingStarted.WhileSubscribed` so it only ticks while the home screen is visible.
- iOS app
- Cloud sync / backup
- Natural-language task entry ("remind me to call dentist tomorrow at 9am")
