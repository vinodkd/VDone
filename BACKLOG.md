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

## M11 — Schedule mode UI + days-of-week recurring

- Replace 4-option segmented button for schedule mode with a dropdown (ExposedDropdownMenuBox) — "Conditional" label is too wide for a segmented slot
- Add days-of-week picker for recurring tasks (Mon–Sun checkboxes; "weekdays" shortcut)
  - New `frequencyDays` DB column (bitmask or comma-separated) — needs migration
  - Update FrequencyChecker to match selected days
  - Update AlarmScheduler to fire on the next matching day

---

## M12 — Home screen widget

- AppWidget using Jetpack Glance showing next due tasks
- Tap a task to open it; tap + to go straight to task creation
- Needs AppWidgetProvider, Glance layout, manifest registration, RemoteViews data loading

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
