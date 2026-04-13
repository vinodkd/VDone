package com.vdone.ui.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.vdone.data.db.ConditionEntity
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
    val savedConditions by viewModel.savedConditions.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()

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

            Spacer(Modifier.height(12.dp))
            WaitingOnSection(
                waitingOn = uiState.waitingOn,
                followUpAt = uiState.followUpAt,
                onSetWaitingOn = { viewModel.setWaitingOn(it) },
                onSetFollowUpAt = { viewModel.setFollowUpAt(it) },
            )

            // Only show schedule for root tasks (not subtasks)
            if (uiState.parentId == null) {
                Spacer(Modifier.height(16.dp))
                ScheduleSection(
                    scheduleMode = uiState.scheduleMode,
                    frequency = uiState.frequency,
                    frequencyTime = uiState.frequencyTime,
                    fixedStart = uiState.fixedStart,
                    savedConditions = savedConditions,
                    pendingConditions = uiState.pendingConditions,
                    allTasks = allTasks.filter { it.id != taskId && it.parentId == null },
                    onSetScheduleMode = { viewModel.setScheduleMode(it) },
                    onSetFrequency = { viewModel.setFrequency(it) },
                    onSetFrequencyTime = { viewModel.setFrequencyTime(it) },
                    onSetFixedStart = { viewModel.setFixedStart(it) },
                    onAddCondition = { type, refTaskId -> viewModel.addCondition(type, refTaskId) },
                    onDeleteSavedCondition = { viewModel.deleteSavedCondition(it) },
                    onDeletePendingCondition = { viewModel.deletePendingCondition(it) },
                    onEditSavedCondition = { id, type, refTaskId -> viewModel.editSavedCondition(id, type, refTaskId) },
                    onEditPendingCondition = { id, type, refTaskId -> viewModel.editPendingCondition(id, type, refTaskId) },
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
    scheduleMode: String,
    frequency: String?,
    frequencyTime: Int?,
    fixedStart: Long?,
    savedConditions: List<ConditionEntity>,
    pendingConditions: List<PendingCondition>,
    allTasks: List<TaskEntity>,
    onSetScheduleMode: (String) -> Unit,
    onSetFrequency: (String?) -> Unit,
    onSetFrequencyTime: (Int?) -> Unit,
    onSetFixedStart: (Long?) -> Unit,
    onAddCondition: (type: String, refTaskId: String?) -> Unit,
    onDeleteSavedCondition: (String) -> Unit,
    onDeletePendingCondition: (String) -> Unit,
    onEditSavedCondition: (id: String, type: String, refTaskId: String?) -> Unit,
    onEditPendingCondition: (id: String, type: String, refTaskId: String?) -> Unit,
) {
    val modeIndex = when (scheduleMode) {
        "frequency" -> 1
        "fixed" -> 2
        "condition" -> 3
        else -> 0
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showFrequencyTimePicker by remember { mutableStateOf(false) }
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
    val freqTimePickerState = rememberTimePickerState(
        initialHour = (frequencyTime ?: 480) / 60,   // default 8 AM
        initialMinute = (frequencyTime ?: 480) % 60,
    )

    Column {
        Text(
            text = "Schedule",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            listOf("None", "Recurring", "On date", "Conditional").forEachIndexed { index, label ->
                SegmentedButton(
                    selected = modeIndex == index,
                    onClick = {
                        when (index) {
                            0 -> onSetScheduleMode("none")
                            1 -> onSetScheduleMode("frequency")
                            2 -> { onSetScheduleMode("fixed"); showDatePicker = true }
                            3 -> onSetScheduleMode("condition")
                        }
                    },
                    shape = SegmentedButtonDefaults.itemShape(index, 4),
                    label = { Text(label) },
                )
            }
        }

        if (modeIndex == 1) {
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "At:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(end = 8.dp),
                )
                OutlinedButton(onClick = { showFrequencyTimePicker = true }) {
                    Text(
                        if (frequencyTime != null)
                            "%02d:%02d".format(frequencyTime / 60, frequencyTime % 60)
                        else "Any time"
                    )
                }
                if (frequencyTime != null) {
                    TextButton(onClick = { onSetFrequencyTime(null) }) { Text("Clear") }
                }
            }
        }

        if (modeIndex == 2 && fixedStart != null) {
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            ) {
                Text(DATE_FMT.format(fixedStart))
            }
        }

        if (modeIndex == 3) {
            ConditionSection(
                savedConditions = savedConditions,
                pendingConditions = pendingConditions,
                allTasks = allTasks,
                onAdd = onAddCondition,
                onDeleteSaved = onDeleteSavedCondition,
                onDeletePending = onDeletePendingCondition,
                onEditSaved = onEditSavedCondition,
                onEditPending = onEditPendingCondition,
            )
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    pendingDateMs = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                    showTimePicker = true
                }) { Text("Next") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

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
                        TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
                        TextButton(onClick = {
                            showTimePicker = false
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

    if (showFrequencyTimePicker) {
        Dialog(onDismissRequest = { showFrequencyTimePicker = false }) {
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
                    TimePicker(state = freqTimePickerState)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = { showFrequencyTimePicker = false }) { Text("Cancel") }
                        TextButton(onClick = {
                            showFrequencyTimePicker = false
                            onSetFrequencyTime(freqTimePickerState.hour * 60 + freqTimePickerState.minute)
                        }) { Text("OK") }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConditionSection(
    savedConditions: List<ConditionEntity>,
    pendingConditions: List<PendingCondition>,
    allTasks: List<TaskEntity>,
    onAdd: (type: String, refTaskId: String?) -> Unit,
    onDeleteSaved: (String) -> Unit,
    onDeletePending: (String) -> Unit,
    onEditSaved: (id: String, type: String, refTaskId: String?) -> Unit,
    onEditPending: (id: String, type: String, refTaskId: String?) -> Unit,
) {
    var showAddDialog by remember { mutableStateOf(false) }
    // editTarget: Pair(id, isForSavedCondition) when editing an existing condition
    var editTarget by remember { mutableStateOf<Triple<String, String, String?>?>(null) }
    val hasAny = savedConditions.isNotEmpty() || pendingConditions.isNotEmpty()

    Column(modifier = Modifier.padding(top = 8.dp)) {
        if (!hasAny) {
            Text(
                "No conditions yet. Task will appear in Next Tasks immediately.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp),
            )
        } else {
            savedConditions.forEach { condition ->
                ListItem(
                    headlineContent = { Text(condition.label(allTasks)) },
                    trailingContent = {
                        Row {
                            IconButton(onClick = {
                                editTarget = Triple(condition.id, condition.type, condition.refTaskId)
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit condition",
                                    modifier = Modifier.size(18.dp))
                            }
                            IconButton(onClick = { onDeleteSaved(condition.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove condition",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                )
                HorizontalDivider()
            }
            pendingConditions.forEach { pending ->
                ListItem(
                    headlineContent = { Text(pending.label(allTasks)) },
                    trailingContent = {
                        Row {
                            IconButton(onClick = {
                                editTarget = Triple(pending.id, pending.type, pending.refTaskId)
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit condition",
                                    modifier = Modifier.size(18.dp))
                            }
                            IconButton(onClick = { onDeletePending(pending.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove condition",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                )
                HorizontalDivider()
            }
        }
        TextButton(onClick = { showAddDialog = true }) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Text("Add condition")
        }
    }

    if (showAddDialog) {
        AddConditionDialog(
            allTasks = allTasks,
            onDismiss = { showAddDialog = false },
            onConfirm = { type, refTaskId ->
                onAdd(type, refTaskId)
                showAddDialog = false
            },
        )
    }

    editTarget?.let { (id, currentType, currentRefTaskId) ->
        val isSaved = savedConditions.any { it.id == id }
        AddConditionDialog(
            allTasks = allTasks,
            initialType = currentType,
            initialRefTaskId = currentRefTaskId,
            onDismiss = { editTarget = null },
            onConfirm = { type, refTaskId ->
                if (isSaved) onEditSaved(id, type, refTaskId)
                else onEditPending(id, type, refTaskId)
                editTarget = null
            },
        )
    }
}

private fun ConditionEntity.label(tasks: List<TaskEntity>): String {
    val refTitle = tasks.find { it.id == refTaskId }?.title ?: "?"
    return when (type) {
        "after_task_done" -> "After done: $refTitle"
        "before_task_time" -> "Before: $refTitle"
        else -> type
    }
}

private fun PendingCondition.label(tasks: List<TaskEntity>): String {
    val refTitle = tasks.find { it.id == refTaskId }?.title ?: "?"
    return when (type) {
        "after_task_done" -> "After done: $refTitle"
        "before_task_time" -> "Before: $refTitle"
        else -> type
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddConditionDialog(
    allTasks: List<TaskEntity>,
    initialType: String? = null,
    initialRefTaskId: String? = null,
    onDismiss: () -> Unit,
    onConfirm: (type: String, refTaskId: String?) -> Unit,
) {
    val conditionTypes = listOf("After task done" to "after_task_done", "Before task time" to "before_task_time")
    var selectedType by remember {
        mutableStateOf(conditionTypes.firstOrNull { it.second == initialType } ?: conditionTypes[0])
    }
    var selectedTask by remember {
        mutableStateOf(allTasks.firstOrNull { it.id == initialRefTaskId } ?: allTasks.firstOrNull())
    }
    var typeExpanded by remember { mutableStateOf(false) }
    var taskExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Condition") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = it },
                ) {
                    OutlinedTextField(
                        value = selectedType.first,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(typeExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    )
                    ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                        conditionTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.first) },
                                onClick = { selectedType = type; typeExpanded = false },
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = taskExpanded,
                    onExpandedChange = { taskExpanded = it },
                ) {
                    OutlinedTextField(
                        value = selectedTask?.title ?: "No tasks available",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Task") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(taskExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    )
                    ExposedDropdownMenu(expanded = taskExpanded, onDismissRequest = { taskExpanded = false }) {
                        if (allTasks.isEmpty()) {
                            DropdownMenuItem(text = { Text("No other tasks") }, onClick = {})
                        } else {
                            allTasks.forEach { task ->
                                DropdownMenuItem(
                                    text = { Text(task.title) },
                                    onClick = { selectedTask = task; taskExpanded = false },
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { selectedTask?.let { onConfirm(selectedType.second, it.id) } },
                enabled = selectedTask != null,
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WaitingOnSection(
    waitingOn: String,
    followUpAt: Long?,
    onSetWaitingOn: (String) -> Unit,
    onSetFollowUpAt: (Long?) -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var pendingDateMs by remember { mutableStateOf(0L) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = localToUtcMidnight(followUpAt ?: System.currentTimeMillis())
    )
    val cal = remember { Calendar.getInstance() }.apply {
        timeInMillis = followUpAt ?: System.currentTimeMillis()
    }
    val timePickerState = rememberTimePickerState(
        initialHour = cal.get(Calendar.HOUR_OF_DAY),
        initialMinute = cal.get(Calendar.MINUTE),
    )

    Column {
        OutlinedTextField(
            value = waitingOn,
            onValueChange = {
                onSetWaitingOn(it)
                if (it.isBlank()) onSetFollowUpAt(null)
            },
            label = { Text("Waiting on (optional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        if (waitingOn.isNotBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Follow up:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(end = 8.dp),
                )
                OutlinedButton(onClick = { showDatePicker = true }) {
                    Text(if (followUpAt != null) DATE_FMT.format(followUpAt) else "Set date")
                }
                if (followUpAt != null) {
                    TextButton(onClick = { onSetFollowUpAt(null) }) { Text("Clear") }
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    pendingDateMs = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                    showTimePicker = true
                }) { Text("Next") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            },
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimePicker) {
        Dialog(onDismissRequest = { showTimePicker = false }) {
            androidx.compose.material3.Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("Pick time", style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp))
                    TimePicker(state = timePickerState)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
                        TextButton(onClick = {
                            showTimePicker = false
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
                            onSetFollowUpAt(c.timeInMillis)
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
