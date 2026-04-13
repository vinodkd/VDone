#!/usr/bin/env bash
# Seed the VDone emulator/device with a representative demo data set.
# Usage: ./scripts/seed_demo_data.sh [serial]
#   serial — optional ADB device serial (defaults to first connected device)

set -euo pipefail

ADB="${ANDROID_SDK_ROOT:-/home/vinodkd/installed/AndroidSDK}/platform-tools/adb"
SERIAL_FLAG=""
[ -n "${1:-}" ] && SERIAL_FLAG="-s $1"

NOW=$(date +%s%3N)
HOUR=3600000
DAY=86400000

OVERDUE=$(( NOW - HOUR * 2 ))
IN_2H=$(( NOW + HOUR * 2 ))
IN_2D=$(( NOW + DAY * 2 ))
YESTERDAY=$(( NOW - DAY ))
TODAY_DONE=$(( NOW - HOUR * 3 ))
AGO_1D=$(( NOW - DAY ))
AGO_2D=$(( NOW - DAY * 2 ))
AGO_3D=$(( NOW - DAY * 3 ))
AGO_5D=$(( NOW - DAY * 5 ))
AGO_6D=$(( NOW - DAY * 6 ))
AGO_7D=$(( NOW - DAY * 7 ))
AGO_10D=$(( NOW - DAY * 10 ))
AGO_14D=$(( NOW - DAY * 14 ))
AGO_20D=$(( NOW - DAY * 20 ))
AGO_30D=$(( NOW - DAY * 30 ))

# Actual DB column order (snoozedUntil added by migration — sits last):
# id, title, notes, status, parentId, scheduleMode, frequency, frequencyTime,
# fixedStart, lastCompletedAt, lastRemindedAt, waitingOn, followUpAt,
# createdAt, updatedAt, snoozedUntil

TMPFILE=$(mktemp /tmp/vdone_seed_XXXX.sql)
trap 'rm -f "$TMPFILE"' EXIT

cat > "$TMPFILE" <<SQL
DELETE FROM conditions;
DELETE FROM tasks;

-- Root tasks
INSERT INTO tasks(id,title,notes,status,parentId,scheduleMode,frequency,frequencyTime,fixedStart,lastCompletedAt,lastRemindedAt,waitingOn,followUpAt,createdAt,updatedAt)
VALUES('task-morning-routine','Morning routine','Get the day started right.','todo',NULL,'frequency','daily',360,NULL,NULL,NULL,NULL,NULL,$AGO_7D,$AGO_7D);

INSERT INTO tasks(id,title,notes,status,parentId,scheduleMode,frequency,frequencyTime,fixedStart,lastCompletedAt,lastRemindedAt,waitingOn,followUpAt,createdAt,updatedAt)
VALUES('task-exercise','Morning exercise','At least 20 minutes.','todo',NULL,'frequency','daily',420,NULL,$TODAY_DONE,NULL,NULL,NULL,$AGO_14D,$AGO_14D);

INSERT INTO tasks(id,title,notes,status,parentId,scheduleMode,frequency,frequencyTime,fixedStart,lastCompletedAt,lastRemindedAt,waitingOn,followUpAt,createdAt,updatedAt)
VALUES('task-standup','Team standup',NULL,'todo',NULL,'frequency','daily',570,NULL,NULL,NULL,NULL,NULL,$AGO_30D,$AGO_30D);

INSERT INTO tasks(id,title,notes,status,parentId,scheduleMode,frequency,frequencyTime,fixedStart,lastCompletedAt,lastRemindedAt,waitingOn,followUpAt,createdAt,updatedAt)
VALUES('task-dentist','Call dentist','Get that check-up booked already.','todo',NULL,'fixed',NULL,NULL,$OVERDUE,NULL,NULL,NULL,NULL,$AGO_5D,$AGO_5D);

INSERT INTO tasks(id,title,notes,status,parentId,scheduleMode,frequency,frequencyTime,fixedStart,lastCompletedAt,lastRemindedAt,waitingOn,followUpAt,createdAt,updatedAt)
VALUES('task-expenses','Submit expense report','Q1 expenses. Receipts in Downloads folder.','todo',NULL,'fixed',NULL,NULL,$IN_2H,NULL,NULL,NULL,NULL,$AGO_3D,$AGO_3D);

INSERT INTO tasks(id,title,notes,status,parentId,scheduleMode,frequency,frequencyTime,fixedStart,lastCompletedAt,lastRemindedAt,waitingOn,followUpAt,createdAt,updatedAt)
VALUES('task-insurance','Renew car insurance','Policy expires in 2 days. Check GoAuto.','todo',NULL,'fixed',NULL,NULL,$IN_2D,NULL,NULL,NULL,NULL,$AGO_10D,$AGO_10D);

INSERT INTO tasks(id,title,notes,status,parentId,scheduleMode,frequency,frequencyTime,fixedStart,lastCompletedAt,lastRemindedAt,waitingOn,followUpAt,createdAt,updatedAt)
VALUES('task-groceries','Buy groceries','Milk, eggs, bread, coffee.','todo',NULL,'none',NULL,NULL,NULL,NULL,NULL,NULL,NULL,$AGO_1D,$AGO_1D);

INSERT INTO tasks(id,title,notes,status,parentId,scheduleMode,frequency,frequencyTime,fixedStart,lastCompletedAt,lastRemindedAt,waitingOn,followUpAt,createdAt,updatedAt)
VALUES('task-kickoff','Project kickoff prep','Slides, agenda, stakeholder list.','todo',NULL,'condition',NULL,NULL,NULL,NULL,NULL,NULL,NULL,$AGO_2D,$AGO_2D);

INSERT INTO tasks(id,title,notes,status,parentId,scheduleMode,frequency,frequencyTime,fixedStart,lastCompletedAt,lastRemindedAt,waitingOn,followUpAt,createdAt,updatedAt)
VALUES('task-alice','Follow up with Alice','Sent draft last Thursday. Waiting for redlines.','todo',NULL,'none',NULL,NULL,NULL,NULL,NULL,'Alice''s response',$IN_2D,$AGO_6D,$AGO_6D);

INSERT INTO tasks(id,title,notes,status,parentId,scheduleMode,frequency,frequencyTime,fixedStart,lastCompletedAt,lastRemindedAt,waitingOn,followUpAt,createdAt,updatedAt)
VALUES('task-vendor','Chase vendor invoice','Invoice 4421 should have been paid 2 weeks ago.','todo',NULL,'none',NULL,NULL,NULL,NULL,NULL,'Accounts payable team',$OVERDUE,$AGO_14D,$AGO_14D);

INSERT INTO tasks(id,title,notes,status,parentId,scheduleMode,frequency,frequencyTime,fixedStart,lastCompletedAt,lastRemindedAt,waitingOn,followUpAt,createdAt,updatedAt)
VALUES('task-garage','Declutter garage',NULL,'done',NULL,'none',NULL,NULL,NULL,NULL,NULL,NULL,NULL,$AGO_20D,$YESTERDAY);

-- Subtasks of Morning routine
INSERT INTO tasks(id,title,notes,status,parentId,scheduleMode,frequency,frequencyTime,fixedStart,lastCompletedAt,lastRemindedAt,waitingOn,followUpAt,createdAt,updatedAt)
VALUES('task-teeth','Brush teeth',NULL,'todo','task-morning-routine','none',NULL,NULL,NULL,NULL,NULL,NULL,NULL,$AGO_7D,$AGO_7D);

INSERT INTO tasks(id,title,notes,status,parentId,scheduleMode,frequency,frequencyTime,fixedStart,lastCompletedAt,lastRemindedAt,waitingOn,followUpAt,createdAt,updatedAt)
VALUES('task-coffee','Make coffee',NULL,'todo','task-morning-routine','none',NULL,NULL,NULL,NULL,NULL,NULL,NULL,$AGO_7D,$AGO_7D);

INSERT INTO tasks(id,title,notes,status,parentId,scheduleMode,frequency,frequencyTime,fixedStart,lastCompletedAt,lastRemindedAt,waitingOn,followUpAt,createdAt,updatedAt)
VALUES('task-cal-check','Check calendar',NULL,'done','task-morning-routine','none',NULL,NULL,NULL,NULL,NULL,NULL,NULL,$AGO_7D,$YESTERDAY);

-- Condition: kickoff unlocks after expense report is submitted
INSERT INTO conditions(id,taskId,type,refTaskId,eventName,offsetSeconds)
VALUES('cond-kickoff-1','task-kickoff','after_task_done','task-expenses',NULL,0);
SQL

echo "Generated SQL ($(wc -l < "$TMPFILE") lines), NOW=$NOW"

DEVICE_TMP="/data/local/tmp/vdone_seed.sql"
$ADB $SERIAL_FLAG push "$TMPFILE" "$DEVICE_TMP" > /dev/null
$ADB $SERIAL_FLAG shell "run-as com.vdone sqlite3 databases/vdone.db < $DEVICE_TMP && echo 'Seed OK'"
$ADB $SERIAL_FLAG shell "rm -f $DEVICE_TMP"
echo "Done. Force-stop and relaunch the app to see the data."
