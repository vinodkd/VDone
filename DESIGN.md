# VDone — Design

## Tech Stack

| Concern | Choice | Reason |
|---|---|---|
| Language | Kotlin | Standard for modern Android development |
| UI framework | Jetpack Compose | Modern native Android UI |
| Widget | Jetpack Glance | Compose-based AppWidget API |
| Notifications | AlarmManager + Notification API | Exact alarms; Full-screen Intent; no library dependencies on the critical path |
| Background scheduling | WorkManager | Battery-aware; survives app restarts; correct tool for periodic work |
| Local storage | Room (SQLite ORM) | Type-safe queries, migration support, Kotlin coroutines integration |

iOS is a future milestone. Two native codebases are justified because unignorable background reminders are the core value proposition — the delivery mechanisms (AlarmManager vs UNUserNotificationCenter) are too different to share.

---

## Data Model

### `tasks`

| Column | Type | Notes |
|---|---|---|
| id | TEXT (UUID) | Primary key |
| title | TEXT | Required |
| notes | TEXT | Optional |
| status | TEXT | `todo` \| `done` |
| parentId | TEXT | FK → tasks.id; null for root tasks |
| scheduleMode | TEXT | `none` \| `fixed` \| `frequency` \| `condition` |
| frequency | TEXT | `daily` \| `weekly` \| `monthly` \| `yearly`; null unless scheduleMode=frequency |
| frequencyDays | INTEGER | Bitmask of selected days for daily tasks; bit = `1 shl (Calendar.DAY_OF_WEEK-1)`; null/0 = every day |
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
| type | TEXT | `after_task_done` \| `before_task_time` |
| refTaskId | TEXT | FK → tasks.id |
| eventName | TEXT | Reserved for future event-based conditions |
| offsetSeconds | INTEGER | Offset from the reference point; default 0 |

> Alarms are managed entirely through AlarmManager — no reminders table. Notification preferences live in SharedPreferences via `AppSettings`.

---

## Architecture

```
app/src/main/java/com/vdone/
  data/
    db/           # Room: TaskEntity, ConditionEntity, DAOs, Migrations, AppDatabase
    repository/   # TaskRepository, ConditionRepository
  scheduler/
    FrequencyChecker    # Pure logic: is a frequency task due today?
    ConditionEvaluator  # Pure logic: evaluates all conditions for a task
    SchedulerWorker     # WorkManager worker — periodic condition evaluation
  reminder/
    AlarmScheduler      # Schedules/cancels exact alarms via AlarmManager
    ReminderReceiver    # BroadcastReceiver — fires when alarm triggers; posts notification
    ReminderActivity    # Full-screen Compose UI: task title, Snooze / Done
    ReminderService     # Foreground service declaration (manifest requirement)
    BootReceiver        # BOOT_COMPLETED — reschedules all future alarms after reboot
  widget/
    DueTasksWidget          # GlanceAppWidget — shows tasks due today
    DueTasksWidgetReceiver  # GlanceAppWidgetReceiver — manifest entry point
    DueTasksWidgetWorker    # CoroutineWorker — refreshes widget every 15 min
  ui/
    home/      # Next Tasks tab
    tasks/     # All Tasks tab (filter / sort / search / tree)
    loops/     # Loops tab (open loops)
    detail/    # Create / edit task
    settings/  # Sound, vibrate, show mode, permissions
    theme/     # Material3 colour scheme and typography
  AppSettings.kt   # SharedPreferences wrapper
  VDoneApp.kt      # Application: DB init, notification channels, alarm rescheduling
  Navigation.kt    # Bottom nav + deep links (vdone://open/detail/{id})
  MainActivity.kt  # Entry point; permission requests; widget deep-link handling
```

### Reminder Delivery

```
AlarmManager.setAlarmClock() — exact, wakes device, shown in status bar
    │
    ▼
ReminderReceiver.onReceive()
    │  reads task ID; checks show mode → suppresses if active
    │  picks notification channel based on sound/vibrate settings
    │  posts NotificationCompat PRIORITY_HIGH + setFullScreenIntent
    │
    ├─ device locked / screen off → ReminderActivity launches full-screen
    └─ screen on                  → heads-up notification banner
```

### Notification Channels

All channels are `IMPORTANCE_HIGH` using alarm audio stream (`USAGE_ALARM`).

| Channel ID | Sound | Vibrate |
|---|---|---|
| `vdone_sv2` | ✓ | ✓ |
| `vdone_s2` | ✓ | ✗ |
| `vdone_v2` | ✗ | ✓ |
| `vdone_silent2` | ✗ | ✗ |

### Widget Data Flow

```
Task created/updated → TaskRepository → DueTasksWidget.refresh()
                                             │
WorkManager (15-min tick) ──────────────────►│
                                             ▼
                                   Load due tasks from DB
                                   Write to DataStore (Glance state)
                                   GlanceAppWidget.update()
                                             │
                                             ▼
                                        Widget redraws
```

---

## Key Decisions

1. **`setAlarmClock()` over `setExactAndAllowWhileIdle()`** — AlarmClock API gets OS priority, bypasses OEM battery batching, and shows an alarm icon in the status bar. Required for reliable delivery on OEM devices (OnePlus, Samsung).

2. **Full-screen intent always set** — On Android 14, the alarm-clock exception allows full-screen even when `canUseFullScreenIntent()` returns false, but only if `setFullScreenIntent` is actually called. Guard removed.

3. **No reminders table** — Alarm state is managed entirely by AlarmManager. `lastRemindedAt` in the task row is sufficient for history.

4. **AND-only condition logic** — OR semantics deferred to future. The schema supports it without migration.

5. **Frequency tasks never "done"** — Frequency tasks always have `status = "todo"`. "Done today" is derived from `lastCompletedAt` being today, not from `status`. `ConditionEvaluator` applies the same logic for `after_task_done`.

6. **Sound/vibrate are global** — Four notification channels cover all combinations; the active channel is selected at alarm fire time.

7. **Widget state via Glance DataStore** — Widget composable reads from `PreferencesGlanceStateDefinition`; data is pushed in on every task mutation and every 15-min worker tick. No live Flow inside Glance composables.
