package com.vdone.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
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
        }
    }
}
