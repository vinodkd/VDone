package com.vdone.ui.settings

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.vdone.AppSettings

private val SNOOZE_OPTIONS = listOf(5, 10, 15, 30)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var snoozeMinutes by remember { mutableStateOf(AppSettings.getSnoozeMinutes(context)) }
    var reminderSound by remember { mutableStateOf(AppSettings.isReminderSound(context)) }
    var reminderVibrate by remember { mutableStateOf(AppSettings.isReminderVibrate(context)) }
    var showMode by remember { mutableStateOf(AppSettings.isShowMode(context)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // Snooze duration
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Snooze duration", style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SNOOZE_OPTIONS.forEachIndexed { index, minutes ->
                        SegmentedButton(
                            selected = snoozeMinutes == minutes,
                            onClick = {
                                snoozeMinutes = minutes
                                AppSettings.setSnoozeMinutes(context, minutes)
                            },
                            shape = SegmentedButtonDefaults.itemShape(index, SNOOZE_OPTIONS.size),
                            label = { Text("${minutes}m") },
                        )
                    }
                }
            }

            HorizontalDivider()

            // Sound / vibration
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Reminders", style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Sound", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = reminderSound,
                        onCheckedChange = {
                            reminderSound = it
                            AppSettings.setReminderSound(context, it)
                        },
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Vibration", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = reminderVibrate,
                        onCheckedChange = {
                            reminderVibrate = it
                            AppSettings.setReminderVibrate(context, it)
                        },
                    )
                }
            }

            HorizontalDivider()

            // Show mode
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("At a show", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Suppress all alarms until turned off",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = showMode,
                        onCheckedChange = { enabled ->
                            showMode = enabled
                            AppSettings.setShowMode(context, enabled)
                        },
                    )
                }
                if (showMode) {
                    Text(
                        "Alarms are suppressed. Switch off to resume.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            HorizontalDivider()

            // Permissions
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Permissions", style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "VDone needs these to deliver reminders reliably. Tap any row to open the relevant Android setting.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                val nm = context.getSystemService(NotificationManager::class.java)
                val am = context.getSystemService(AlarmManager::class.java)
                val canExactAlarm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    am.canScheduleExactAlarms() else true
                val canOverlay = Settings.canDrawOverlays(context)
                val canFullScreen = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
                    @Suppress("NewApi") nm.canUseFullScreenIntent() else true

                PermissionRow(
                    label = "Notification sound & vibration",
                    description = "Alarm sound plays at alarm volume",
                    granted = true, // channel settings are user-controlled; always show link
                    showOpenButton = true,
                ) {
                    context.startActivity(
                        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }
                    )
                }

                PermissionRow(
                    label = "Exact alarms",
                    description = "Required for reminders to fire at the right time",
                    granted = canExactAlarm,
                ) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        if (context.packageManager.resolveActivity(intent, 0) != null)
                            context.startActivity(intent)
                    }
                }

                PermissionRow(
                    label = "Display over other apps",
                    description = "Allows the alarm screen to appear over the lock screen",
                    granted = canOverlay,
                ) {
                    context.startActivity(
                        Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}"))
                    )
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    PermissionRow(
                        label = "Full-screen alerts",
                        description = "Required on Android 14+ for lock-screen alarm",
                        granted = canFullScreen,
                    ) {
                        // Use try/catch rather than resolveActivity — on Android 12+,
                        // package visibility rules can make resolveActivity() return null
                        // even when the intent is actually handleable.
                        try {
                            context.startActivity(
                                Intent("android.settings.MANAGE_APP_USE_FULL_SCREEN_INTENTS").apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                            )
                        } catch (_: ActivityNotFoundException) {
                            context.startActivity(
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.parse("package:${context.packageName}"))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionRow(
    label: String,
    description: String,
    granted: Boolean,
    showOpenButton: Boolean = false,
    onOpen: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = if (granted) Icons.Filled.CheckCircle else Icons.Filled.Warning,
            contentDescription = null,
            tint = if (granted) MaterialTheme.colorScheme.primary
                   else MaterialTheme.colorScheme.error,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(description, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (!granted || showOpenButton) {
            OutlinedButton(onClick = onOpen) {
                Text(if (granted) "Open" else "Fix")
            }
        }
    }
}
