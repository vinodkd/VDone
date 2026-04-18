package com.vdone.reminder

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.vdone.VDoneApp
import com.vdone.data.db.TaskEntity
import com.vdone.ui.theme.VDoneTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReminderActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show over lock screen and wake the screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        val taskId = intent.getStringExtra(AlarmScheduler.EXTRA_TASK_ID) ?: run { finish(); return }
        val taskTitle = intent.getStringExtra(AlarmScheduler.EXTRA_TASK_TITLE) ?: "Task due"

        // Cancel the accompanying notification so it doesn't linger in the shade
        val notifId = taskId.hashCode().and(0x7FFFFFFF)
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(notifId)

        val app = application as VDoneApp
        val repository = app.taskRepository
        val conditionRepository = app.conditionRepository

        setContent {
            VDoneTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.errorContainer,
                ) {
                    var nextTasks by remember { mutableStateOf<List<TaskEntity>>(emptyList()) }

                    LaunchedEffect(Unit) {
                        nextTasks = repository.getAllTasks().first()
                            .filter { it.status == "todo" && it.id != taskId }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 32.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            Icons.Default.Alarm,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Task Due",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                        Text(
                            taskTitle,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(20.dp))
                        Text(
                            "Snooze",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(5, 10, 15).forEach { minutes ->
                                OutlinedButton(onClick = {
                                    val snoozeAt = System.currentTimeMillis() + minutes * 60_000L
                                    AlarmScheduler.scheduleAt(this@ReminderActivity, taskId, taskTitle, snoozeAt)
                                    lifecycleScope.launch { repository.setSnooze(taskId, snoozeAt) }
                                    ReminderService.snooze(this@ReminderActivity)
                                    finish()
                                }) {
                                    Text("${minutes}m")
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                lifecycleScope.launch {
                                    val task = repository.getTaskById(taskId)
                                    if (task != null) repository.toggleStatus(task)
                                    ReminderService.dismiss(this@ReminderActivity)
                                    finish()
                                }
                            },
                        ) {
                            Text("Done")
                        }

                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.3f)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Doing instead — remind me after",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                        Spacer(Modifier.height(8.dp))
                        val timeLabel = SimpleDateFormat("h:mma", Locale.getDefault())
                            .format(Date()).lowercase()
                        OutlinedButton(
                            onClick = {
                                lifecycleScope.launch {
                                    val newTaskId = java.util.UUID.randomUUID().toString()
                                    repository.createTaskWithId(
                                        id = newTaskId,
                                        title = "Other task at $timeLabel",
                                        notes = null,
                                    )
                                    val task = repository.getTaskById(taskId)
                                    if (task != null) {
                                        repository.updateTask(
                                            task.copy(scheduleMode = "condition")
                                        )
                                        conditionRepository.addCondition(
                                            taskId = taskId,
                                            type = "after_task_done",
                                            refTaskId = newTaskId,
                                        )
                                    }
                                    ReminderService.dismiss(this@ReminderActivity)
                                    finish()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Other task at $timeLabel", color = MaterialTheme.colorScheme.onErrorContainer)
                        }

                        if (nextTasks.isNotEmpty()) {
                            Spacer(Modifier.height(4.dp))
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                            ) {
                                items(nextTasks) { blockingTask ->
                                    Text(
                                        text = blockingTask.title,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                lifecycleScope.launch {
                                                    val task = repository.getTaskById(taskId)
                                                    if (task != null) {
                                                        repository.updateTask(
                                                            task.copy(scheduleMode = "condition")
                                                        )
                                                        conditionRepository.addCondition(
                                                            taskId = taskId,
                                                            type = "after_task_done",
                                                            refTaskId = blockingTask.id,
                                                        )
                                                    }
                                                    ReminderService.dismiss(this@ReminderActivity)
                                                    finish()
                                                }
                                            }
                                            .padding(vertical = 12.dp, horizontal = 4.dp),
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.2f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
