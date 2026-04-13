package com.vdone.reminder

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.vdone.AppSettings
import com.vdone.VDoneApp
import com.vdone.ui.theme.VDoneTheme
import kotlinx.coroutines.launch

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

        val repository = (application as VDoneApp).taskRepository

        setContent {
            VDoneTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.errorContainer,
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(24.dp),
                            modifier = Modifier.padding(32.dp),
                        ) {
                            Icon(
                                Icons.Default.Alarm,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                            )
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
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                OutlinedButton(onClick = {
                                    val snoozeMinutes = AppSettings.getSnoozeMinutes(this@ReminderActivity)
                                    val snoozeAt = System.currentTimeMillis() + snoozeMinutes * 60_000L
                                    AlarmScheduler.scheduleAt(this@ReminderActivity, taskId, taskTitle, snoozeAt)
                                    finish()
                                }) {
                                    val snoozeMinutes = AppSettings.getSnoozeMinutes(this@ReminderActivity)
                                    Text("Snooze ${snoozeMinutes}m")
                                }
                                Button(onClick = {
                                    lifecycleScope.launch {
                                        val task = repository.getTaskById(taskId)
                                        if (task != null) repository.toggleStatus(task)
                                        finish()
                                    }
                                }) {
                                    Text("Done")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
