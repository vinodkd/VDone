package com.vdone.ui.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.vdone.data.db.TaskEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    viewModel: TaskDetailViewModel,
    onBack: () -> Unit,
    onAddSubtask: (parentId: String) -> Unit,
    onEditSubtask: (id: String) -> Unit,
    taskId: String?,
) {
    val uiState by viewModel.uiState.collectAsState()
    val subtasks by viewModel.subtasks.collectAsState()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when {
                            uiState.isNew && uiState.parentId != null -> "New Subtask"
                            uiState.isNew -> "New Task"
                            else -> "Edit Task"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.save() }) {
                Icon(Icons.Default.Check, contentDescription = "Save")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .imePadding()
                .verticalScroll(rememberScrollState()),
        ) {
            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.setTitle(it) },
                label = { Text("Title") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
            )
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = { viewModel.setNotes(it) },
                label = { Text("Notes (optional)") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )

            // Only show schedule for root tasks (not subtasks)
            if (uiState.parentId == null) {
                Spacer(Modifier.height(16.dp))
                ScheduleSection(
                    frequency = uiState.frequency,
                    fixedStart = uiState.fixedStart,
                    onSetFrequency = { viewModel.setFrequency(it) },
                    onSetFixedStart = { viewModel.setFixedStart(it) },
                )
            }

            // Show subtasks section whenever editing an existing task, at any depth
            if (!uiState.isNew && taskId != null) {
                Spacer(Modifier.height(24.dp))
                SubtasksSection(
                    subtasks = subtasks,
                    onAdd = { onAddSubtask(taskId) },
                    onEdit = onEditSubtask,
                    onToggle = { viewModel.toggleSubtaskStatus(it) },
                    onDelete = { viewModel.deleteSubtask(it) },
                )
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

private val FREQUENCIES = listOf("daily", "weekly", "monthly", "yearly")
private val DATE_FMT = SimpleDateFormat("EEE, MMM d yyyy  HH:mm", Locale.getDefault())

// DatePicker works in UTC dates. Convert a local-timezone timestamp to
// UTC midnight of that same local date so the picker shows the correct day.
private fun localToUtcMidnight(localMs: Long): Long {
    val local = Calendar.getInstance().apply { timeInMillis = localMs }
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        set(Calendar.YEAR, local.get(Calendar.YEAR))
        set(Calendar.MONTH, local.get(Calendar.MONTH))
        set(Calendar.DAY_OF_MONTH, local.get(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ScheduleSection(
    frequency: String?,
    fixedStart: Long?,
    onSetFrequency: (String?) -> Unit,
    onSetFixedStart: (Long?) -> Unit,
) {
    // 0 = none, 1 = recurring, 2 = fixed date
    val mode = when {
        frequency != null -> 1
        fixedStart != null -> 2
        else -> 0
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var pendingDateMs by remember { mutableStateOf(0L) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = localToUtcMidnight(fixedStart ?: System.currentTimeMillis())
    )
    val cal = remember { Calendar.getInstance() }.apply {
        timeInMillis = fixedStart ?: System.currentTimeMillis()
    }
    val timePickerState = rememberTimePickerState(
        initialHour = cal.get(Calendar.HOUR_OF_DAY),
        initialMinute = cal.get(Calendar.MINUTE),
    )

    Column {
        Text(
            text = "Schedule",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)) {
            listOf("None", "Recurring", "On date").forEachIndexed { index, label ->
                SegmentedButton(
                    selected = mode == index,
                    onClick = {
                        when (index) {
                            0 -> { onSetFrequency(null); onSetFixedStart(null) }
                            1 -> onSetFrequency(frequency ?: "daily")
                            2 -> { onSetFrequency(null); showDatePicker = true }
                        }
                    },
                    shape = SegmentedButtonDefaults.itemShape(index, 3),
                    label = { Text(label) },
                )
            }
        }

        if (mode == 1) {
            FlowRow(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FREQUENCIES.forEach { freq ->
                    FilterChip(
                        selected = frequency == freq,
                        onClick = { onSetFrequency(if (frequency == freq) "daily" else freq) },
                        label = { Text(freq.replaceFirstChar { it.uppercase() }) },
                    )
                }
            }
        }

        if (mode == 2 && fixedStart != null) {
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            ) {
                Text(DATE_FMT.format(fixedStart))
            }
        }
    }

    // Date picker dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    showDatePicker = false
                    pendingDateMs = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                    showTimePicker = true
                }) { Text("Next") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Time picker dialog
    if (showTimePicker) {
        Dialog(onDismissRequest = { showTimePicker = false }) {
            androidx.compose.material3.Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        "Pick time",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp),
                    )
                    TimePicker(state = timePickerState)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        androidx.compose.material3.TextButton(onClick = { showTimePicker = false }) {
                            Text("Cancel")
                        }
                        androidx.compose.material3.TextButton(onClick = {
                            showTimePicker = false
                            // DatePicker returns UTC midnight — extract Y/M/D in UTC
                            // to avoid off-by-one in timezones behind UTC
                            val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                                timeInMillis = pendingDateMs
                            }
                            val c = Calendar.getInstance().apply {
                                set(Calendar.YEAR, utcCal.get(Calendar.YEAR))
                                set(Calendar.MONTH, utcCal.get(Calendar.MONTH))
                                set(Calendar.DAY_OF_MONTH, utcCal.get(Calendar.DAY_OF_MONTH))
                                set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                                set(Calendar.MINUTE, timePickerState.minute)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            onSetFixedStart(c.timeInMillis)
                        }) { Text("OK") }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubtasksSection(
    subtasks: List<TaskEntity>,
    onAdd: () -> Unit,
    onEdit: (String) -> Unit,
    onToggle: (TaskEntity) -> Unit,
    onDelete: (TaskEntity) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Subtasks",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onAdd) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier
                    .size(16.dp)
                    .padding(end = 2.dp),
            )
            Text("Add")
        }
    }

    HorizontalDivider()

    if (subtasks.isEmpty()) {
        Text(
            text = "No subtasks yet.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 12.dp),
        )
    } else {
        subtasks.forEach { subtask ->
            val done = subtask.status == "done"
            ListItem(
                modifier = Modifier.clickable { onEdit(subtask.id) },
                headlineContent = {
                    Text(
                        text = subtask.title,
                        textDecoration = if (done) TextDecoration.LineThrough else null,
                        color = if (done) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface,
                    )
                },
                supportingContent = if (!subtask.notes.isNullOrBlank()) {
                    { Text(subtask.notes, maxLines = 1, style = MaterialTheme.typography.bodySmall) }
                } else null,
                leadingContent = {
                    IconButton(onClick = { onToggle(subtask) }) {
                        Icon(
                            imageVector = if (done) Icons.Filled.CheckCircle
                            else Icons.Filled.RadioButtonUnchecked,
                            contentDescription = if (done) "Mark todo" else "Mark done",
                            tint = if (done) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                trailingContent = {
                    IconButton(onClick = { onDelete(subtask) }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                },
                colors = ListItemDefaults.colors(
                    containerColor = if (done) MaterialTheme.colorScheme.surfaceVariant
                    else MaterialTheme.colorScheme.surface,
                ),
            )
            HorizontalDivider()
        }
    }
}
