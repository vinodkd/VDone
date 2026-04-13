# VDone v1.0 — Release Notes

## What is VDone?

A todo app for procrastinators. No rigid deadlines required. Intrusive reminders that actually interrupt you. Flexible scheduling — fixed times, recurring tasks, condition-based triggers, and open-loop tracking for things you're waiting on.

## How to install

VDone is not on the Play Store yet. See the [installation instructions](https://vinodkd.github.io/VDone/#installing-the-apk) on the website, or the short version:

1. Download `vdone.apk` from this release
2. On your Android device: **Settings → Apps → Special app access → Install unknown apps** → allow your browser or file manager
3. Open the APK and tap Install
4. On first launch, grant the three permissions it asks for (notifications, exact alarms, full-screen intents) — all are needed for reminders to work

> **Tested on Android 14.** Should work on Android 8+ (API 26+).

---

## What's in v1.0

Nine milestones from empty project to this release:

**Core task management**
- Create, edit, delete tasks with optional notes
- Nested tasks (subtasks) to arbitrary depth with progress tracking
- Task status: todo / done

**Scheduling**
- Fixed date/time tasks with exact alarms
- Recurring tasks (daily / weekly / monthly / yearly), optionally at a specific time of day
- Condition-based tasks: start after another task completes, before one starts, or after a named event (e.g. "after breakfast")

**Reminders**
- Full-screen alert with Snooze / Done when device is locked
- High-priority heads-up notification when screen is on
- Configurable snooze duration: 5 / 10 / 15 / 30 minutes
- Sound and vibration toggleable independently in Settings
- **Show mode**: suppress all alarms globally while at a gig or event; resume with one tap
- Alarms survive device reboots

**Open loops**
- Mark a task as "waiting on someone" with a free-text note
- Set a follow-up date for a nudge reminder
- Dedicated Loops tab lists all open loops sorted by follow-up date
- Clear or complete directly from the Loops tab

**Task list polish**
- Filter by All / Todo / Done
- Sort by Date created / Title / Due date
- Snoozed tasks show "Snoozed until HH:mm" on their card

---

## A note on how this was built

The code was written by [Claude Code](https://claude.ai/code). The intent, design, and product decisions were made by a human. Nine milestones, conversational sessions, no code written by hand.

This is both a working app and an experiment in how far AI-assisted development can go on a non-trivial Android project — real background services, exact alarms, Room migrations, Jetpack Compose UI.

Everything is local only. No account, no tracking, no data leaves your device.

---

## Known limitations

- Debug-signed APK (not Play Store signed) — safe to install but you may see a warning
- Full-screen alert only launches when the screen is off/locked; heads-up notification shown otherwise (this is Android OS behaviour, not a bug)
- No cloud sync, no iOS app — both planned for future versions
- Condition-based tasks require the app to run in the background periodically for evaluation (WorkManager, ~15 min interval)
