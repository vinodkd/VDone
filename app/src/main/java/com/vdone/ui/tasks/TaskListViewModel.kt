package com.vdone.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vdone.data.db.TaskEntity
import com.vdone.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class FilterMode { ALL, TODO, DONE }
enum class SortMode  { CREATED, TITLE, DUE }

data class TaskNode(
    val task: TaskEntity,
    val depth: Int,
    val childCount: Int,
    val doneChildCount: Int,
    val isExpanded: Boolean,
)

class TaskListViewModel(private val repository: TaskRepository) : ViewModel() {

    private val expandedIds = mutableSetOf<String>()
    private val _refreshTick = MutableStateFlow(0)

    val filterMode = MutableStateFlow(FilterMode.ALL)
    val sortMode   = MutableStateFlow(SortMode.CREATED)

    val taskNodesWithRefresh = combine(
        repository.getAllTasks(),
        _refreshTick,
        filterMode,
        sortMode,
    ) { all, _, filter, sort -> buildTree(all, filter, sort) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private fun buildTree(
        all: List<TaskEntity>,
        filter: FilterMode,
        sort: SortMode,
    ): List<TaskNode> {
        val byParent = all.groupBy { it.parentId }

        // Apply filter to root tasks; children always follow their parent
        val roots = (byParent[null] ?: emptyList()).filter { task ->
            when (filter) {
                FilterMode.ALL  -> true
                FilterMode.TODO -> task.status != "done"
                FilterMode.DONE -> task.status == "done"
            }
        }.sortedWith(compareBy { task ->
            when (sort) {
                SortMode.CREATED -> task.createdAt
                SortMode.TITLE   -> 0L  // handled below via thenBy
                SortMode.DUE     -> task.fixedStart ?: Long.MAX_VALUE
            }
        }).let { list ->
            if (sort == SortMode.TITLE) list.sortedBy { it.title.lowercase() } else list
        }

        val result = mutableListOf<TaskNode>()

        fun visit(task: TaskEntity, depth: Int) {
            val children = byParent[task.id] ?: emptyList()
            val doneChildren = children.count { it.status == "done" }
            val expanded = task.id in expandedIds
            result.add(TaskNode(task, depth, children.size, doneChildren, expanded))
            if (expanded) children.sortedBy { it.createdAt }.forEach { visit(it, depth + 1) }
        }

        roots.forEach { visit(it, 0) }
        return result
    }

    fun toggleExpanded(taskId: String) {
        if (taskId in expandedIds) expandedIds.remove(taskId) else expandedIds.add(taskId)
        _refreshTick.value = _refreshTick.value + 1
    }

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
