# VDone — Requirements

VDone is a todo app for procrastinators. It helps people manage tasks without forcing rigid deadlines, supports repetitive/routine tasks, and delivers reminders that are hard to ignore.

---

## Task Management

- Users can create, edit, and delete tasks with a title and optional notes.
- Tasks do not require a schedule; scheduling is optional.
- Tasks can have one of four scheduling modes:
  - **None** — no time constraints; appears in Next Tasks if all conditions are met (or there are none)
  - **Fixed** — a specific date/time; an alarm fires at that moment
  - **Recurring** — must be done daily / weekly / monthly / yearly; optionally at a specific time of day (e.g. "daily at 06:00"); daily tasks can be restricted to specific days of the week
  - **Condition-based** — becomes active when one or more conditions are satisfied
- Condition types: after another task completes; before another task's scheduled time
- Multiple conditions on a task are AND'd
- Tasks can nest (have subtasks) to arbitrary depth
- Task status is `todo` or `done`; recurring tasks reset to `todo` after each completion

---

## Next Tasks View

- Shows tasks that are actionable now:
  - Unscheduled tasks with all conditions met (or no conditions)
  - Recurring tasks due in the current period that have not yet been completed; timed recurring tasks only appear after their scheduled time of day
  - Fixed tasks whose start time is today
- Overdue fixed tasks are highlighted with an "Overdue" label
- Updates automatically as time passes; + button to add a new task directly

---

## All Tasks View

- Shows the full task tree with expand/collapse for nested tasks
- Filter by status: All / Todo / Done
- Sort by: date created / title / due date
- Search by title or notes

---

## Open Loops

- A task can be marked "waiting on someone" with a free-text note
- An optional follow-up date/time triggers a reminder nudge
- The **Loops** tab lists all open loops sorted by follow-up date
- Loops can be cleared (resolved without completing the task) or marked done

---

## Reminders

- Fixed and timed recurring tasks fire an alarm when due
- Alarms are modal: a full-screen screen launches when the device is locked or sleeping; a heads-up notification appears when the screen is on
- The user can snooze a reminder for 5, 10, 15, or 30 minutes (chosen on the alarm screen)
- Sound and vibration are independently toggleable in Settings
- **Show mode** suppresses all alarms globally (e.g. while at an event); resume with one tap
- Alarms are rescheduled automatically after device reboots
- Follow-up reminders (open loops) are plain notifications, not full-screen alerts

---

## Home Screen Widget

- Shows up to 5 tasks due today without opening the app
- Tapping a task opens its edit screen; tapping + opens the new-task screen
- Updates immediately when tasks are created or changed

---

## Non-Functional

- Android-first; iOS is a future milestone
- All data stored locally; no server, no account, works fully offline
- Alarms must interrupt the user even when the app is closed (Full-screen Intent + exact AlarmManager alarms)
- Background alarms must survive device reboots

---

## Out of Scope (current version)

- iOS support
- Cloud sync / multi-device
- Calendar integrations
- Natural language input
- OR-logic for task conditions
- Per-task sound/vibrate overrides
