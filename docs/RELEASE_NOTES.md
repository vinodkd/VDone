# VDone — Release Notes

---

## v1.1

**Search, days-of-week scheduling, and home screen widget**

- **Search** — filter any task by title or notes from the All Tasks tab
- **Days-of-week** — daily recurring tasks can now be restricted to specific days (e.g. weekdays only); day picker on the task edit screen
- **Home screen widget** — shows up to 5 tasks due today; tap a task to open it, tap + to add a new one; updates immediately when tasks change
- **Mark done from detail** — checkmark button in the task edit screen top bar
- **Inline snooze options** — 5 / 10 / 15 / 30 min buttons directly on the alarm screen (snooze duration preference removed from Settings)
- **+ button on Next Tasks** — add a task without switching tabs
- Fixed: editing a snoozed task no longer leaves a stale snooze in effect
- Fixed: notification sound now routes through alarm volume (bypasses Do Not Disturb)
- Fixed: full-screen alarm now works correctly on Android 14 OEM devices (OnePlus, Samsung)

---

## v1.0

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

> How it was built: code written by [Claude Code](https://claude.ai/code); product decisions by a human. No code written by hand. Everything is local — no account, no tracking.
