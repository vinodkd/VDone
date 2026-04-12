# VDone — Design

## Tech Stack

### Android (v1)

| Concern | Choice | Reason |
|---|---|---|
| UI framework | Jetpack Compose | Modern native Android UI; first-class Google support |
| Notifications | Android Notification API (native) | Full-screen Intent + foreground service with no library dependencies |
| Background scheduling | WorkManager | Battery-aware; survives app restarts; correct tool for periodic condition evaluation |
| Local storage | Room (SQLite ORM) | Type-safe queries, migration support, integrates with Kotlin coroutines |
| Language | Kotlin | Standard for modern Android development |

### iOS (future)

| Concern | Choice |
|---|---|
| UI framework | SwiftUI |
| Notifications | UNUserNotificationCenter + Critical Alerts entitlement (requires Apple approval) |
| Storage | Core Data or SQLite (schema ported from Android) |
| Language | Swift |

**Platform:** Android-first. iOS added in a later milestone once Android is stable. Two native codebases are justified because unignorable background reminders are the core value proposition of the app.

---

## Data Model

### `tasks`
| Column | Type | Notes |
|---|---|---|
| id | TEXT (UUID) | Primary key |
| title | TEXT | Required |
| notes | TEXT | Optional |
| status | TEXT | `todo` \| `in_progress` \| `waiting` \| `done` \| `snoozed` |
| parent_id | TEXT | FK → tasks.id; null for root tasks |
| schedule_mode | TEXT | `none` \| `fixed` \| `frequency` \| `condition` |
| frequency | TEXT | `daily` \| `weekly` \| `monthly` \| `yearly`; null unless schedule_mode=frequency |
| fixed_start | INTEGER | Unix timestamp; null unless schedule_mode=fixed |
| fixed_end | INTEGER | Unix timestamp; null unless schedule_mode=fixed |
| is_open_loop | INTEGER | Boolean (0/1) |
| snoozed_until | INTEGER | Unix timestamp; null unless status=snoozed |
| created_at | INTEGER | Unix timestamp |
| updated_at | INTEGER | Unix timestamp |

### `conditions`
| Column | Type | Notes |
|---|---|---|
| id | TEXT (UUID) | Primary key |
| task_id | TEXT | FK → tasks.id (the task this condition gates) |
| type | TEXT | `after_task_done` \| `before_task_starts` \| `after_event` \| `after_offset` |
| ref_task_id | TEXT | FK → tasks.id; used for task-relative conditions |
| event_name | TEXT | Named event (e.g., "breakfast"); used for event-relative conditions |
| offset_seconds | INTEGER | Minimum offset from the reference point (positive = after, negative = before) |
| operator | TEXT | Reserved for future use: `and` \| `or`. Currently always `and`. |

### `reminders`
| Column | Type | Notes |
|---|---|---|
| id | TEXT (UUID) | Primary key |
| task_id | TEXT | FK → tasks.id |
| fire_at | INTEGER | Unix timestamp |
| sound | INTEGER | Boolean (0/1) |
| vibrate | INTEGER | Boolean (0/1) |
| dismissed | INTEGER | Boolean (0/1) |

---

## Architecture (Android)

```
app/src/main/java/com/vdone/
  data/
    db/               # Room database, DAOs, entities
    repository/       # Data access layer (TaskRepository, ReminderRepository)
  domain/
    model/            # Plain Kotlin data classes (Task, Condition, Reminder)
    scheduler/        # Condition evaluator — pure logic, no Android dependencies
  service/
    ReminderService   # Foreground service that owns the notification
    SchedulerWorker   # WorkManager worker — runs condition evaluator periodically
  ui/
    home/             # Next Tasks screen
    tasks/            # All tasks list
    openloops/        # Open loop tasks
    detail/           # Task detail / edit
    reminder/         # Full-screen reminder activity (launched via Full-screen Intent)
  MainActivity.kt
```

### Condition Evaluation

`SchedulerWorker` (WorkManager) runs:
- Periodically in the background (15-min minimum interval; WorkManager handles battery)
- On app foreground (via lifecycle observer)
- When a task status changes (may unblock dependents)

It evaluates all `conditions` rows against current state. When all conditions for a task are met (AND semantics; OR and more complex logic designed for later), it schedules a notification via `ReminderService`.

### Reminder Delivery

- `ReminderService` is a foreground service that posts the notification
- Notification uses `fullScreenIntent` pointing to a dedicated `ReminderActivity`
- `ReminderActivity` is a full-screen Compose UI — dismiss or snooze
- On Android 14+, `USE_FULL_SCREEN_INTENT` permission is declared and requested at runtime
- When app is already foregrounded, the activity launches directly over current screen

---

## Milestones

### M0 — Scaffold (runs on device/emulator, nothing else)
- Android project initialized with Kotlin + Jetpack Compose
- Room database wired up, schema created on first launch
- App opens, shows a placeholder home screen
- Builds and runs on Android emulator and physical device

### M1 — Basic Task CRUD (first steel thread)
- Create, read, update, delete tasks (title + notes only)
- Task list with status toggle (todo ↔ done)
- Task detail/edit screen
- No scheduling, no reminders, no nesting yet

### M2 — Full-screen Reminder (core feature, early)
- `ReminderService` foreground service wired up
- `ReminderActivity` full-screen Compose UI
- `USE_FULL_SCREEN_INTENT` permission requested
- Manually trigger a test reminder from task detail to verify end-to-end
- Snooze: pick duration, reschedule notification

### M3 — Nested Tasks
- Tasks can have subtasks (add child from task detail)
- Task list renders tree with expand/collapse
- Parent shows completion progress based on children

### M4 — Frequency-Based Tasks
- Tasks can be marked as daily / weekly / monthly / yearly
- "Next Tasks" home view shows tasks due today based on frequency + last completion
- Completing a frequency task resets it for the next period
- `SchedulerWorker` (WorkManager) wired up and running

### M5 — Fixed Schedule + Automatic Reminders
- Tasks can have a fixed start date/time
- Scheduler fires reminder automatically at scheduled time via `ReminderService`
- Full end-to-end: set a time, background the app, reminder interrupts you

### M6 — Condition-Based Scheduling
- Named events: user-defined with defaults (breakfast, lunch, dinner, wake up, bed)
- `ConditionEditor` UI: after task / before task / after event + offset
- Condition evaluator runs on foreground + task status change
- All conditions AND'd; schema supports future OR/complex logic via `operator` column

### M7 — Open Loops
- Tasks can be marked as "waiting on someone"
- Open Loops view lists all waiting tasks
- Follow-up/poke reminder: set a date to be reminded to follow up

### M8 — Reminder Polish
- Sound on/off per reminder
- Vibration on/off per reminder
- Reminder history in task detail

### M9 — Next Tasks View Polish
- Filter/sort options
- Unscheduled tasks surfaced with a "do it sometime today?" nudge
- Overdue tasks highlighted
- Snoozed tasks visible but visually de-emphasised

### M10 — iOS (future)
- Swift + SwiftUI port
- Same data model, same feature set
- Apply for Critical Alerts entitlement

---

## Decisions

1. **Named events** — user-defined with a default set (breakfast, lunch, dinner, wake up, bed).
2. **Condition logic** — AND semantics for v1. Schema includes an `operator` column reserved for future OR/complex expressions.
3. **Background scheduling on iOS** — condition evaluator runs on foreground only; acceptable limitation for iOS v2.
4. **Snoozed task visibility** — snoozed tasks remain visible in Next Tasks but are visually de-emphasised.
5. **Reminder delivery** — native Android Full-screen Intent via a foreground service + `ReminderActivity`. No cross-platform library dependencies on the critical notification path.
