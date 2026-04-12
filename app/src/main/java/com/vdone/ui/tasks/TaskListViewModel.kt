package com.vdone.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vdone.data.db.TaskEntity
import com.vdone.data.repository.TaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TaskNode(
    val task: TaskEntity,
    val depth: Int,
    val childCount: Int,
    val doneChildCount: Int,
    val isExpanded: Boolean,
)

class TaskListViewModel(private val repository: TaskRepository) : ViewModel() {

    private val expandedIds = mutableSetOf<String>()

    val taskNodes = repository.getAllTasks()
        .map { buildTree(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private fun buildTree(all: List<TaskEntity>): List<TaskNode> {
        val byParent = all.groupBy { it.parentId }
        val result = mutableListOf<TaskNode>()

        fun visit(task: TaskEntity, depth: Int) {
            val children = byParent[task.id] ?: emptyList()
            val doneChildren = children.count { it.status == "done" }
            val expanded = task.id in expandedIds
            result.add(TaskNode(task, depth, children.size, doneChildren, expanded))
            if (expanded) children.forEach { visit(it, depth + 1) }
        }

        byParent[null]?.forEach { visit(it, 0) }
        return result
    }

    fun toggleExpanded(taskId: String) {
        if (taskId in expandedIds) expandedIds.remove(taskId) else expandedIds.add(taskId)
        // trigger recompute by forcing a re-emit — easiest via a dedicated state flag
        _refreshTick.value = _refreshTick.value + 1
    }

    private val _refreshTick = kotlinx.coroutines.flow.MutableStateFlow(0)

    val taskNodesWithRefresh = kotlinx.coroutines.flow.combine(
        repository.getAllTasks(),
        _refreshTick,
    ) { all, _ -> buildTree(all) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun toggleStatus(task: TaskEntity) {
        viewModelScope.launch { repository.toggleStatus(task) }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch { repository.deleteTask(task) }
    }

    class Factory(private val repository: TaskRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) =
            TaskListViewModel(repository) as T
    }
}
