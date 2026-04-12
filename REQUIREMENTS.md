# VDone — Requirements

VDone is a todo application for procrastinators. It helps people manage tasks without forcing rigid deadlines, and supports repetitive/routine tasks (e.g., daily chores).

---

## Functional Requirements

### Task Management

- **FR-1** Users can create, edit, and delete tasks.
- **FR-2** Tasks do not require a start date/time; scheduling is optional.
- **FR-3** Tasks can have one of the following scheduling modes:
  - Unscheduled (no time constraints)
  - Fixed schedule (start/end at specific date-times)
  - Frequency-based (must be done sometime during the day / week / month / year)
  - Condition-based (see FR-4)
- **FR-4** Tasks can have start/trigger conditions:
  - After another task completes
  - Before another task starts
  - After a named event (e.g., "after breakfast") with an optional minimum offset (e.g., "at least 30 mins before lunch")
  - After a relative time offset from another condition
- **FR-5** Tasks can nest (have subtasks) to an arbitrary depth.
- **FR-6** Tasks have a status: `todo`, `in-progress`, `waiting` (open loop), `done`, `snoozed`.

### Open Loops

- **FR-7** A task can be marked as an "open loop" — the user has done their part and is now waiting on an external party.
- **FR-8** Open loop tasks appear in a dedicated view.
- **FR-9** Users can add a follow-up note or poke reminder to an open loop task.

### Reminders

- **FR-10** When a task becomes due (by schedule or condition), the user is reminded.
- **FR-11** Reminders must be modal and visible — they must demand attention, not just appear as a passive notification.
- **FR-12** Reminders are configurable to make a sound, vibrate, or both.
- **FR-13** The user can snooze a reminder for a specified duration: seconds / minutes / hours / days.
- **FR-14** After snoozing, the reminder fires again at the specified time.

### Next Tasks View

- **FR-15** A "Next Tasks" view shows tasks that are actionable now — either unscheduled tasks, or tasks whose conditions are currently met.

---

## Non-Functional Requirements

- **NFR-1** The app targets Android first. iOS support is a later milestone.
- **NFR-2** All data is stored locally on the device (no server required).
- **NFR-3** The app must work offline at all times.
- **NFR-4** Cloud sync is explicitly out of scope for the initial version.
- **NFR-5** The UI must be usable one-handed on a phone screen.
- **NFR-6** Background reminders must interrupt the user even when the app is closed (Android Full-screen Intent).

---

## Out of Scope (v1)

- iOS support (planned for v2)
- Cloud sync / multi-device
- Collaboration / sharing tasks
- Calendar integrations
- Natural language input
- Web or desktop clients
