# VDone — Release Notes

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
