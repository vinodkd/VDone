package com.vdone.ui.loops

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vdone.data.db.TaskEntity
import java.text.SimpleDateFormat
import java.util.Locale

private val DATE_FMT = SimpleDateFormat("EEE, MMM d  HH:mm", Locale.getDefault())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenLoopsScreen(
    viewModel: OpenLoopsViewModel,
    onEditTask: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    val tasks by viewModel.waitingTasks.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Open Loops") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
            )
        },
    ) { padding ->
        if (tasks.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("No open loops.", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "Mark a task as waiting on someone to track it here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item { }
                items(tasks, key = { it.id }) { task ->
                    LoopCard(
                        task = task,
                        onDone = { viewModel.markDone(task) },
                        onClear = { viewModel.clearWaiting(task) },
                        onEdit = { onEditTask(task.id) },
                    )
                }
                item { }
            }
        }
    }
}

@Composable
private fun LoopCard(
    task: TaskEntity,
    onDone: () -> Unit,
    onClear: () -> Unit,
    onEdit: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(task.title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Waiting on: ${task.waitingOn}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 2.dp),
                )
                if (task.followUpAt != null) {
                    Text(
                        "Follow up: ${DATE_FMT.format(task.followUpAt)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (task.followUpAt < System.currentTimeMillis())
                            MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                IconButton(onClick = onDone) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Mark done",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                TextButton(onClick = onClear) {
                    Text("Clear", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
