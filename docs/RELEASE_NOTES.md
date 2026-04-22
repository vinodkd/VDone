# VDone — Release Notes

---

## v1.0.37 — Mute alarm with lock button

- Pressing the power/lock button while an alarm is sounding now silences the audio immediately
- The notification and alarm screen remain visible — tap the notification to return and dismiss or snooze as normal

---

## v1.0.36 — Periwinkle icon rebrand

- Launcher icon updated to periwinkle blue — square icon on the home screen, proper circle on launchers that use round icons
- Notification icon replaced with a clean white checkmark (was the system alarm clock)

---

## v1.0.35 — Plan tab cleanup

- Plan tab no longer shows done styling (strikethrough, greyed cards) — it shows task definitions, not status
- Filter chips (All / Todo / Done) removed from Plan tab
- Mark-done button removed from the task edit screen

---

## v1.0.34 — 5-tab redesign + Started state

- **New tab structure**: **Plan · Start · Doing · Waiting · Done** — every tab is an action verb
- **Plan tab** — task definitions only; no status toggle; long-press to delete; toggle to deactivate
- **Start tab** (was Next Tasks) — shows tasks due today + overdue; ▶ to start a task, ↻ to skip a recurring occurrence
- **Doing tab** — tasks currently in progress; tap Done when finished
- **Done tab** — today's completions (recurring and one-off); auto-completed overnight tasks shown with "Auto-completed" label
- **Deactivate tasks** — toggle a task inactive from Plan; it disappears from Start and stops alarming; reactivate with the same toggle
- **Auto-done** — tasks left in Doing overnight are automatically completed when you next open the app
- Snoozed tasks are now correctly hidden from Start until the snooze expires

---

## v1.0.32 — Deactivate and long-press delete

- **Deactivate** — toggle switch on every task row in the task list; inactive tasks show faded text and an "Inactive" label; deactivated tasks never appear in Next Tasks or fire alarms
- **Long-press to delete** — removes the dedicated delete icon; long-pressing a task card shows a confirmation dialog instead

---

## v1.0.31 — Alarm timeout and multi-alarm fix

- **Alarm auto-dismiss**: full-screen alarm now closes automatically after a configurable timeout (default 2 min), making way for subsequent alarms in a series. Set in Settings → "Alarm auto-dismiss". Warning shown if timeout is ≤ snooze duration.
- **Multiple alarms no longer stack**: only one alarm screen can exist at a time; a new alarm replaces the current one and resets the timeout countdown
- **Audio no longer orphaned**: switching alarms stops the previous audio before starting the new one

---

## v1.0.30 — Show Mode no longer silently drops alarms

- Alarms that fire while Show Mode is active are now queued; turning Show Mode off reschedules them immediately so nothing is missed

---

## v1.0.29 — Conditional alarms fire proactively

- Condition-based tasks now schedule an alarm immediately when their blocking task is marked done — no more waiting for the 60-second poll to surface them

---

## v1.0.28 — Default alarm time for recurring tasks

- Recurring tasks saved without an explicit time now default to **08:00** — they will always fire an alarm rather than silently never appearing

---

## v1.0.27 — Skip on Next Tasks

- **Skip this time** button now appears directly on recurring task cards in the Next Tasks tab — no need to wait for the full-screen alarm

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
