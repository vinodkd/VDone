# VDone

VDone is a todo application for procrastinators. It also helps people do repetitive tasks (eg, daily chores).

## Features

- **Flexible scheduling** — tasks can be unscheduled, fixed date/time, recurring (daily/weekly/monthly/yearly, optionally on specific days of the week), or condition-based (after another task is done, before one starts, after a named event)
- **Intrusive reminders** — full-screen alarm when the device is locked; heads-up notification when the screen is on; survives reboots
- **Snooze** — 5 / 10 / 15 / 30 minute options directly on the alarm screen
- **Open loops** — mark a task "waiting on someone" with a follow-up reminder date
- **Nested tasks** — subtasks to arbitrary depth
- **Home screen widget** — see next due tasks without opening the app; tap to open, + to add
- **Search** — filter any task by title or notes
- Sound and vibration toggleable independently

## Install

VDone is not on the Play Store yet. To sideload:

1. Download `vdone.apk` from [Releases](../../releases)
2. On your device: **Settings → Apps → Special app access → Install unknown apps** → allow your browser or file manager
3. Open the APK and tap Install
4. On first launch, grant the permissions it requests (notifications, exact alarms, full-screen intents) — all required for reminders to work

Tested on Android 14. Requires Android 8+ (API 26+).

## How it was built

Code written by [Claude Code](https://claude.ai/code). Product decisions by a human. No code written by hand.

Everything is stored locally. No account, no tracking, no data leaves your device.
