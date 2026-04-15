package com.vdone.ui.tasks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

private fun isToday(timestampMs: Long?): Boolean {
    if (timestampMs == null) return false
    val ref = Calendar.getInstance().apply { timeInMillis = timestampMs }
    val today = Calendar.getInstance()
    return ref.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
        ref.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
}

private fun formatSnoozeTime(ms: Long): String {
    val now = System.currentTimeMillis()
    return if (isToday(ms)) {
        "Snoozed until " + SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(ms))
    } else {
        val diff = ms - now
        val mins = (diff / 60_000).toInt()
        "Snoozed ${mins}m"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: TaskListViewModel,
    onAddTask: () -> Unit,
    onEditTask: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    val nodes by viewModel.taskNodesWithRefresh.collectAsState()
    val filterMode by viewModel.filterMode.collectAsState()
    val sortMode by viewModel.sortMode.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var showSortMenu by remember { mutableStateOf(false) }
    var searchActive by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tasks") },
                actions = {
                    IconButton(onClick = {
                        searchActive = !searchActive
                        if (!searchActive) viewModel.setSearchQuery("")
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Default.Sort, contentDescription = "Sort")
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("By date created") },
                                onClick = { viewModel.sortMode.value = SortMode.CREATED; showSortMenu = false },
                                trailingIcon = if (sortMode == SortMode.CREATED) ({
                                    Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp))
                                }) else null,
                            )
                            DropdownMenuItem(
                                text = { Text("By title") },
                                onClick = { viewModel.sortMode.value = SortMode.TITLE; showSortMenu = false },
                                trailingIcon = if (sortMode == SortMode.TITLE) ({
                                    Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp))
                                }) else null,
                            )
                            DropdownMenuItem(
                                text = { Text("By due date") },
                                onClick = { viewModel.sortMode.value = SortMode.DUE; showSortMenu = false },
                                trailingIcon = if (sortMode == SortMode.DUE) ({
                                    Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp))
                                }) else null,
                            )
                        }
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTask) {
                Icon(Icons.Default.Add, contentDescription = "Add task")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // Search bar
            if (searchActive) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    placeholder = { Text("Search tasks…") },
                    singleLine = true,
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                )
            }

            // Filter chips
            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    FilterChip(
                        selected = filterMode == FilterMode.ALL,
                        onClick = { viewModel.filterMode.value = FilterMode.ALL },
                        label = { Text("All") },
                    )
                }
                item {
                    FilterChip(
                        selected = filterMode == FilterMode.TODO,
                        onClick = { viewModel.filterMode.value = FilterMode.TODO },
                        label = { Text("Todo") },
                    )
                }
                item {
                    FilterChip(
                        selected = filterMode == FilterMode.DONE,
                        onClick = { viewModel.filterMode.value = FilterMode.DONE },
                        label = { Text("Done") },
                    )
                }
            }

            if (nodes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (filterMode == FilterMode.ALL) "No tasks yet." else "Nothing here.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (filterMode == FilterMode.ALL) {
                            Text(
                                "Tap + to add one.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    item { }
                    items(nodes, key = { it.task.id }) { node ->
                        TaskCard(
                            node = node,
                            onToggle = { viewModel.toggleStatus(node.task) },
                            onEdit = { onEditTask(node.task.id) },
                            onDelete = { viewModel.deleteTask(node.task) },
                            onToggleExpand = { viewModel.toggleExpanded(node.task.id) },
                        )
                    }
                    item { }
                }
            }
        }
    }
}

@Composable
private fun TaskCard(
    node: TaskNode,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleExpand: () -> Unit,
) {
    val task = node.task
    val done = task.status == "done"
    val doneToday = task.scheduleMode == "frequency" && isToday(task.lastCompletedAt)
    val now = System.currentTimeMillis()
    val isSnoozed = task.snoozedUntil != null && task.snoozedUntil > now
    val indentDp = (node.depth * 20).dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = indentDp)
            .clickable(onClick = onEdit),
        colors = CardDefaults.cardColors(
            containerColor = when {
                done || doneToday -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (done || doneToday) 0.dp else 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (node.childCount > 0) {
                IconButton(onClick = onToggleExpand, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = if (node.isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (node.isExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
            } else {
                Box(modifier = Modifier.width(40.dp))
            }

            IconButton(onClick = onToggle, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = if (done || doneToday) Icons.Filled.CheckCircle
                    else Icons.Filled.RadioButtonUnchecked,
                    contentDescription = if (done || doneToday) "Mark todo" else "Mark done",
                    tint = if (done || doneToday) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp),
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (done || doneToday) TextDecoration.LineThrough else null,
                    color = if (done || doneToday) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface,
                )
                if (doneToday) {
                    Text(
                        "Done today",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                if (isSnoozed) {
                    Text(
                        formatSnoozeTime(task.snoozedUntil!!),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
                if (!task.notes.isNullOrBlank()) {
                    Text(
                        text = task.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                    )
                }
                if (node.childCount > 0) {
                    Text(
                        text = "${node.doneChildCount}/${node.childCount} subtasks done",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
