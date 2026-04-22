# VDone — Requirements

VDone is a todo app for procrastinators. It helps people manage tasks without forcing rigid deadlines, supports repetitive/routine tasks, and delivers reminders that are hard to ignore.

---

## Task Management

- Users can create, edit, and delete tasks with a title and optional notes.
- Tasks do not require a schedule; scheduling is optional.
- Tasks can have one of four scheduling modes:
  - **None** — no time constraints; appears in Start tab if all conditions are met (or there are none)
  - **Fixed** — a specific date/time; an alarm fires at that moment
  - **Recurring** — must be done daily / weekly / monthly / yearly; optionally at a specific time of day (e.g. "daily at 06:00"); daily tasks can be restricted to specific days of the week
  - **Condition-based** — becomes active when one or more conditions are satisfied
- Condition types: after another task completes; before another task's scheduled time
- Multiple conditions on a task are AND'd
- Tasks can nest (have subtasks) to arbitrary depth
- Task status: `todo` | `doing` | `done`; recurring tasks reset to `todo` after each completion
- Tasks can be **deactivated** (`isActive = false`): they never appear in the Start tab or fire alarms; reactivatable at any time

---

## Tab Structure

Five tabs, each an action verb:

### Plan
- Shows the full task tree with expand/collapse for nested tasks
- Task definitions only — no status toggle; edit, deactivate, or delete here
- Filter by status: All / Todo / Done; sort by: date created / title / due date; search by title or notes
- Long-press a task to delete (with confirmation); toggle to deactivate

### Start
- Shows tasks that are actionable now (today + overdue):
  - Recurring tasks due in the current period not yet completed; timed recurring tasks only appear after their scheduled time
  - Fixed tasks whose start time is today or overdue
  - Condition-based tasks whose conditions are all met
  - Snoozed tasks are hidden until the snooze expires
- ▶ to start a task (moves it to Doing); ↻ to skip a recurring occurrence for the current period
- Overdue fixed tasks are highlighted with an "Overdue" label

### Doing
- Tasks currently in progress (`status = "doing"`)
- Shows when the task was started
- Done button marks the task complete
- Tasks left in Doing overnight are auto-completed when the app is next opened

### Waiting
- Tasks marked "waiting on someone" with a free-text note
- Optional follow-up date/time triggers a reminder nudge
- Sorted by follow-up date; can be cleared (resolved) or marked done

### Done
- Today's completions only: tasks marked done today and recurring tasks completed today
- Auto-completed tasks (overnight Doing tasks) labelled "Auto-completed"
- Read-only; tap to open detail

---

## Reminders

- Fixed and timed recurring tasks fire an alarm when due
- Alarms are modal: a full-screen screen launches when the device is locked or sleeping; a heads-up notification appears when the screen is on
- The user can snooze a reminder for 5, 10, or 15 minutes (chosen on the alarm screen)
- Sound and vibration are independently toggleable in Settings
- **Show mode** suppresses all alarms globally (e.g. while at an event); queued alarms are rescheduled immediately when Show Mode is turned off
- Alarm screen auto-dismisses after a configurable timeout (default 2 min)
- Alarms are rescheduled automatically after device reboots
- Follow-up reminders (open loops) are plain notifications, not full-screen alerts
- Condition-based task alarms fire immediately when the blocking task is marked done

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
