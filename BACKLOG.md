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

## M11 — Google Play Store

- Create a Play developer account
- Generate a release keystore (replace debug-signed APK)
- Meet Play Store requirements: privacy policy, target API level, 64-bit, etc.
- Submit for review and publish
- Self-update handled by Play once live (no in-app update mechanism needed)

---

## Ideas / Unscoped

- **HomeViewModel tick optimization**: current `while(true)/delay(60s)` runs for the ViewModel lifetime even when the home screen isn't visible. Low-priority alternative: replace with a `tickerFlow` combined into `dueTasks` via `SharingStarted.WhileSubscribed` so it only ticks while the screen is actually showing.



- iOS app
- Cloud sync / backup
- Widget (home screen task glance)
- Natural-language task entry ("remind me to call dentist tomorrow at 9am")
