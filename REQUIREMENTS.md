# VDone — Requirements

VDone is a todo application for procrastinators. It helps people manage tasks without forcing rigid deadlines, and supports repetitive/routine tasks (e.g., daily chores).

---

## Functional Requirements

### Task Management

- **FR-1** Users can create, edit, and delete tasks.
- **FR-2** Tasks do not require a start date/time; scheduling is optional.
- **FR-3** Tasks can have one of the following scheduling modes:
  - **Unscheduled** — no time constraints; appears in Next Tasks if conditions are met
  - **Fixed** — a specific date/time; alarm fires at that moment
  - **Frequency-based** — must be done daily / weekly / monthly / yearly; optionally at a specific time of day (e.g., "daily at 06:00")
  - **Condition-based** — triggered when one or more conditions are satisfied
- **FR-4** Tasks can have start/trigger conditions:
  - After another task completes
  - Before another task starts
  - After a named event (e.g., "after breakfast") with an optional minimum offset in seconds
  - Multiple conditions are AND'd; OR logic is reserved for a future version
- **FR-5** Tasks can nest (have subtasks) to an arbitrary depth. Parent tasks show subtask completion progress.
- **FR-6** Task status is `todo` or `done`. Frequency tasks reset to `todo` after completion; completion date is tracked separately via `lastCompletedAt`.

### Open Loops

- **FR-7** A task can be marked as an "open loop" by filling in a "Waiting on" field. This indicates the user has done their part and is waiting on an external party.
- **FR-8** Open loop tasks appear in a dedicated **Loops** tab, sorted by follow-up date.
- **FR-9** Users can add a follow-up date/time to an open loop task. A reminder fires at that time.
- **FR-10** Users can clear the waiting state from the Loops tab (marks the loop as resolved without completing the task), or mark the task done directly.

### Reminders

- **FR-11** When a fixed-schedule or timed frequency task becomes due, an alarm fires.
- **FR-12** Reminders are modal: a full-screen activity (`ReminderActivity`) launches when the device is locked/sleeping. When the screen is on, a high-priority heads-up notification is shown.
- **FR-13** Reminders can make a sound, vibrate, both, or neither — configurable globally in Settings.
- **FR-14** The user can snooze a reminder. Snooze duration is configurable (5 / 10 / 15 / 30 minutes). Default is 10 minutes.
- **FR-15** After snoozing, the alarm re-fires at the snoozed time. The task card shows "Snoozed until HH:mm" until it fires.
- **FR-16** Follow-up reminders (open loops) are plain notifications, not full-screen alerts.
- **FR-17** A global **"At a show"** mode suppresses all alarms until the mode is turned off.

### Next Tasks View

- **FR-18** The **Next Tasks** tab shows tasks that are actionable now:
  - Unscheduled tasks with all conditions met (or no conditions)
  - Frequency tasks due today that have not been completed today; timed frequency tasks only appear after their scheduled time of day
  - Fixed tasks whose start time is in the past or within the current session
  - Open loop tasks (waiting-on) are excluded
- **FR-19** Overdue fixed tasks are highlighted in the error container colour with an "Overdue" label.

### All Tasks View

- **FR-20** The **All Tasks** tab shows the full task tree with expand/collapse for nested tasks.
- **FR-21** Users can filter tasks by status: All / Todo / Done.
- **FR-22** Users can sort tasks by: date created (default) / title / due date.
- **FR-23** Frequency tasks completed today are shown with strikethrough and a "Done today" label.

---

## Non-Functional Requirements

- **NFR-1** The app targets Android first. iOS support is a later milestone.
- **NFR-2** All data is stored locally on the device (no server required).
- **NFR-3** The app must work offline at all times.
- **NFR-4** Cloud sync is explicitly out of scope for the initial version.
- **NFR-5** The UI must be usable one-handed on a phone screen.
- **NFR-6** Background reminders must interrupt the user even when the app is closed (Android Full-screen Intent + AlarmManager exact alarms).
- **NFR-7** Alarms must survive device reboots (rescheduled via `BOOT_COMPLETED` receiver).

---

## Out of Scope (v1)

- iOS support (planned for v2)
- Cloud sync / multi-device
- Collaboration / sharing tasks
- Calendar integrations
- Natural language input
- Web or desktop clients
- OR-logic for task conditions
- Per-task sound/vibrate overrides (sound and vibrate are global settings)
