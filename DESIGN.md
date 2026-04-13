# VDone — Design

## Tech Stack

### Android (v1)

| Concern | Choice | Reason |
|---|---|---|
| UI framework | Jetpack Compose | Modern native Android UI; first-class Google support |
| Notifications | AlarmManager + Android Notification API | Exact alarms; Full-screen Intent; no library dependencies on the critical path |
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
| notes | TEXT | Optional long-form notes |
| status | TEXT | `todo` \| `done` |
| parentId | TEXT | FK → tasks.id; null for root tasks |
| scheduleMode | TEXT | `none` \| `fixed` \| `frequency` \| `condition` |
| frequency | TEXT | `daily` \| `weekly` \| `monthly` \| `yearly`; null unless scheduleMode=frequency |
| frequencyTime | INTEGER | Minutes from midnight (0–1439); null = anytime during the period |
| fixedStart | INTEGER | Unix ms; null unless scheduleMode=fixed |
| lastCompletedAt | INTEGER | Unix ms; updated each time a frequency task is completed |
| lastRemindedAt | INTEGER | Unix ms; updated each time a reminder fires |
| snoozedUntil | INTEGER | Unix ms; set when snoozed, cleared when alarm re-fires |
| waitingOn | TEXT | Free text; non-null marks this task as an open loop |
| followUpAt | INTEGER | Unix ms follow-up alarm for open loop tasks |
| createdAt | INTEGER | Unix ms |
| updatedAt | INTEGER | Unix ms |

### `conditions`

| Column | Type | Notes |
|---|---|---|
| id | TEXT (UUID) | Primary key |
| taskId | TEXT | FK → tasks.id (the task this condition gates) |
| type | TEXT | `after_task_done` \| `before_task_starts` \| `after_event` \| `after_offset` |
| refTaskId | TEXT | FK → tasks.id; used for task-relative conditions |
| eventName | TEXT | Named event (e.g., "breakfast"); used for event-relative conditions |
| offsetSeconds | INTEGER | Offset from the reference point (positive = after); default 0 |

> No `reminders` table — alarms are managed entirely through `AlarmManager` (not persisted in the DB). Notification channels, sound, and vibrate preferences live in `SharedPreferences` via `AppSettings`.

---

## Architecture (Android)

```
app/src/main/java/com/vdone/
  data/
    db/                 # Room: TaskEntity, ConditionEntity, DAOs, Migrations, AppDatabase
    repository/         # TaskRepository, ConditionRepository
  scheduler/
    ConditionEvaluator  # Pure Kotlin: evaluates all conditions for a task against current DB state
    SchedulerWorker     # WorkManager worker — runs ConditionEvaluator periodically
  reminder/
    AlarmScheduler      # Schedules/cancels exact alarms via AlarmManager
    ReminderReceiver    # BroadcastReceiver — fires when alarm triggers; posts notification
    ReminderActivity    # Full-screen Compose UI: task title, Snooze / Done buttons
    ReminderService     # Foreground service shell (declared for manifest; not in main flow)
    BootReceiver        # BOOT_COMPLETED receiver — reschedules all future alarms after reboot
  ui/
    home/               # HomeScreen + HomeViewModel: Next Tasks tab
    tasks/              # TaskListScreen + TaskListViewModel: All Tasks tab (filter/sort/tree)
    loops/              # OpenLoopsScreen + OpenLoopsViewModel: Loops tab (open loops)
    detail/             # TaskDetailScreen + TaskDetailViewModel: create/edit task
    settings/           # SettingsScreen: snooze duration, sound, vibrate, show mode
    theme/              # Material3 colour scheme and typography
  AppSettings.kt        # SharedPreferences wrapper (snooze, sound, vibrate, show mode)
  VDoneApp.kt           # Application: DB init, notification channels, alarm rescheduling on start
  Navigation.kt         # Bottom nav (Next / All Tasks / Loops) + Settings route
  MainActivity.kt       # Entry point; requests POST_NOTIFICATIONS, SCHEDULE_EXACT_ALARM,
                        #   USE_FULL_SCREEN_INTENT permissions
```

### Reminder Delivery

```
AlarmManager (exact, wake-device)
    │  fires at scheduled time
    ▼
ReminderReceiver.onReceive()
    │  reads EXTRA_TASK_ID, EXTRA_IS_FOLLOWUP
    │  checks show mode → suppress if active
    │  picks notification channel from AppSettings (sound × vibrate → vdone_sv/s/v/silent)
    │  posts NotificationCompat with PRIORITY_HIGH + setFullScreenIntent (non-follow-up only)
    │  records lastRemindedAt / clears snoozedUntil async via goAsync()
    ▼
Device locked/screen off → ReminderActivity launches full-screen
Device screen on         → heads-up notification banner
```

**Notification channels** (all IMPORTANCE_HIGH):

| Channel ID | Sound | Vibrate |
|---|---|---|
| `vdone_sv` | ✓ | ✓ |
| `vdone_s` | ✓ | ✗ |
| `vdone_v` | ✗ | ✓ |
| `vdone_silent` | ✗ | ✗ |

### Condition Evaluation

`SchedulerWorker` (WorkManager, 15-min minimum interval) and `ConditionEvaluator` evaluate all `conditions` rows against current task state. Supported types:

- `after_task_done` — satisfied when `refTask.status == "done"` or `refTask.lastCompletedAt` is today (handles frequency tasks that auto-reset)
- `before_task_starts` — satisfied when `refTask.fixedStart` is in the future
- `after_event` — satisfied when `event.lastOccurredAt + offsetSeconds ≤ now`
- `after_offset` — satisfied when `now ≥ baseline + offsetSeconds`

Multiple conditions per task are AND'd.

---

## Milestones

### M0 — Scaffold
Android project with Kotlin + Jetpack Compose, Room DB wired up, app runs on device.

### M1 — Basic Task CRUD
Create/read/update/delete tasks (title + notes). Task list with `todo`/`done` toggle.

### M2 — Full-screen Reminder
`AlarmManager` exact alarm → `ReminderReceiver` → `ReminderActivity`. `USE_FULL_SCREEN_INTENT` permission. Manual trigger from task detail.

### M3 — Nested Tasks
Subtasks, tree expand/collapse in All Tasks view, parent shows `n/m subtasks done`.

### M4 — Frequency Tasks + Next Tasks View
Daily/weekly/monthly/yearly tasks. `SchedulerWorker` + `ConditionEvaluator`. Next Tasks tab shows actionable tasks based on frequency and completion.

### M5 — Fixed Schedule + Automatic Alarms
Fixed date/time tasks. Alarm fires automatically at `fixedStart`. End-to-end background reminder.

### M6 — Condition-Based Scheduling
Named events (user-defined, default set seeded). Condition editor UI. Condition evaluator runs on foreground + task status change. Pending conditions queued before task save via `PendingCondition`.

### M7 — Open Loops
`waitingOn` field marks a task as an open loop. Follow-up alarm via `followUpAt`. Dedicated Loops tab. "Clear waiting" action.

### M8 — Reminder Polish
Four notification channels (sound × vibrate). Configurable snooze duration (5/10/15/30 min). "At a show" mode suppresses all alarms. `lastRemindedAt` recorded in task. `canUseFullScreenIntent()` guard on Android 14+. Per-task reminder history shown in detail screen.

### M9 — Task List Polish (v1 release)
Filter chips (All / Todo / Done) and sort (Created / Title / Due) on All Tasks tab. Snoozed tasks show "Snoozed until HH:mm" label. `snoozedUntil` persisted in DB, cleared when alarm re-fires.

### M10 — iOS (future)
Swift + SwiftUI port. Same data model. Apply for Critical Alerts entitlement.

---

## Key Decisions

1. **AlarmManager over WorkManager for reminders** — WorkManager has a 15-min minimum interval and is not suitable for exact, user-facing alarms. `AlarmManager.setExactAndAllowWhileIdle()` is required for reliable alarm delivery.
2. **Full-screen intent behaviour** — On Android 10+, `setFullScreenIntent` only launches the activity when the screen is off/locked. When the screen is on, Android intentionally shows a heads-up banner instead. This is OS-enforced behaviour, not a bug.
3. **No `reminders` table** — Alarm state is managed entirely by AlarmManager; there is no persisted reminder log. `lastRemindedAt` in the task row is sufficient for history display.
4. **AND-only condition logic** — OR semantics deferred to a future version; the `conditions` schema supports it without migration.
5. **Frequency task "done today" detection** — Frequency tasks always have `status = "todo"` (they auto-reset). "Done today" is derived from `lastCompletedAt` being today, not from `status`. `ConditionEvaluator` applies the same logic for `after_task_done` conditions.
6. **Named events (v1 scope)** — User-defined named events with a seeded default set (Wake up, Breakfast, Lunch, Dinner, Bed time). Event times are recorded manually by the user marking them; no calendar integration.
7. **Sound/vibrate are global settings** — Per-task overrides are out of scope for v1. Four notification channels cover all sound × vibrate combinations; the active channel is selected at alarm fire time.
