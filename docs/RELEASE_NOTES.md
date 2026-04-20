# VDone — Release Notes

---

## v1.0.26 — Day label fix

- Weekday chips now show **Su M T W Th F Sa** — Saturday is unambiguous on narrow screens

---

## v1.0.25 — Periwinkle rebrand

- App theme and website updated from teal/green to periwinkle blue

---

## v1.0.24 — Per-task alarm sound

- **Custom alarm ringtone per task** — fixed and recurring tasks now have a sound picker in the edit screen; choose any system ringtone; defaults to the system default alarm sound

---

## v1.0.19 — v1.0.23

**Scheduling improvements and alarm screen upgrades**

- **Skip this time** — recurring tasks now have a "Skip this time" button on the alarm screen; advances the schedule without marking the task done
- **Fixed alarm scheduling** — monthly, weekly, and yearly alarms now schedule correctly to their next occurrence
- **Conditional offset** — after-task-done conditions can now specify a delay (e.g. wait 1h 30m after the preceding task completes)
- **Schedule info on task rows** — Next Tasks and All Tasks rows now show a compact schedule label (e.g. "Today at 2:30 PM", "Weekdays at 08:00", "After: meeting")
- **Time picker fix** — editing a fixed-time task now correctly pre-fills the time picker with the saved time

---

## v1.0.18

**Alarm improvements and defer-to-task**

- **Defer to task** — on the alarm screen, instead of snoozing by time, pick a task you're currently doing; the alarm re-surfaces automatically when that task is marked done. "Other task at HH:MM" creates a placeholder you can rename later.
- **Reliable full-screen alarm** — alarm now uses a persistent foreground service so audio loops and the notification stays visible until explicitly dismissed, even on OEM devices (OnePlus tested)
- **Lock screen notification** — notification now visible on lock screen on Android 14
- **Snooze simplified** — 5 / 10 / 15 min options; 30 min removed
- **Version shown in Settings**

---

## v1.0.12 — v1.0.17

**Search, days-of-week scheduling, and home screen widget**

- **Search** — filter any task by title or notes from the All Tasks tab
- **Days-of-week** — daily recurring tasks can now be restricted to specific days (e.g. weekdays only); day picker on the task edit screen
- **Home screen widget** — shows up to 5 tasks due today; tap a task to open it, tap + to add a new one; updates immediately when tasks change
- **Mark done from detail** — checkmark button in the task edit screen top bar
- **+ button on Next Tasks** — add a task without switching tabs
- Fixed: editing a snoozed task no longer leaves a stale snooze in effect
- Fixed: notification sound now routes through alarm volume (bypasses Do Not Disturb)

---

## v1.0.1 — v1.0.11

**Initial release**

- Create, edit, delete tasks with optional notes
- Nested tasks (subtasks) to arbitrary depth
- Scheduling: fixed date/time, recurring (daily/weekly/monthly/yearly, optionally at a time of day), condition-based (after task done, before task starts)
- Full-screen alarm when device is locked; heads-up notification when screen is on
- Snooze and configurable sound/vibration
- Show mode: suppress all alarms globally
- Open loops: "waiting on" field with follow-up reminder
- Loops tab, All Tasks tab (filter/sort), Next Tasks tab
- Alarms survive device reboots

> Code written by [Claude Code](https://claude.ai/code); product decisions by a human. No code written by hand. Everything is local — no account, no tracking.
