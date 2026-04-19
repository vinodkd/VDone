package com.vdone.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vdone.data.db.ConditionEntity
import com.vdone.data.db.TaskEntity
import com.vdone.data.repository.ConditionRepository
import com.vdone.data.repository.TaskRepository
import com.vdone.ui.util.scheduleLabel
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
    val scheduleLabel: String?,
)

class TaskListViewModel(
    private val repository: TaskRepository,
    private val conditionRepository: ConditionRepository,
) : ViewModel() {

    private val expandedIds = mutableSetOf<String>()
    private val _refreshTick = MutableStateFlow(0)

    val filterMode  = MutableStateFlow(FilterMode.ALL)
    val sortMode    = MutableStateFlow(SortMode.CREATED)
    val searchQuery = MutableStateFlow("")

    private val tasksAndConditions = combine(
        repository.getAllTasks(),
        conditionRepository.getAllConditions(),
    ) { tasks, conditions -> Pair(tasks, conditions) }

    val taskNodesWithRefresh = combine(
        tasksAndConditions,
        _refreshTick,
        filterMode,
        sortMode,
        searchQuery,
    ) { (all, conditions), _, filter, sort, query -> buildTree(all, conditions, filter, sort, query) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private fun buildTree(
        all: List<TaskEntity>,
        conditions: List<ConditionEntity>,
        filter: FilterMode,
        sort: SortMode,
        query: String,
    ): List<TaskNode> {
        val byParent = all.groupBy { it.parentId }
        val taskMap = all.associateBy { it.id }
        val conditionsByTask = conditions.groupBy { it.taskId }
        val q = query.trim().lowercase()

        // Apply filter and search to root tasks; children always follow their parent.
        // When a search is active, also include roots that have a matching descendant.
        val matchingIds: Set<String> = if (q.isEmpty()) emptySet() else
            all.filter { it.title.lowercase().contains(q) || it.notes?.lowercase()?.contains(q) == true }
               .map { it.id }.toSet()

        fun hasMatchingDescendant(taskId: String): Boolean =
            (byParent[taskId] ?: emptyList()).any { it.id in matchingIds || hasMatchingDescendant(it.id) }

        val roots = (byParent[null] ?: emptyList()).filter { task ->
            val passesFilter = when (filter) {
                FilterMode.ALL  -> true
                FilterMode.TODO -> task.status != "done"
                FilterMode.DONE -> task.status == "done"
            }
            val passesSearch = q.isEmpty() || task.id in matchingIds || hasMatchingDescendant(task.id)
            passesFilter && passesSearch
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
            val label = scheduleLabel(task, conditionsByTask[task.id].orEmpty(), taskMap)
            result.add(TaskNode(task, depth, children.size, doneChildren, expanded, label))
            if (expanded) children.sortedBy { it.createdAt }.forEach { visit(it, depth + 1) }
        }

        roots.forEach { visit(it, 0) }
        return result
    }

    fun setSearchQuery(q: String) { searchQuery.value = q }

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

    class Factory(
        private val repository: TaskRepository,
        private val conditionRepository: ConditionRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) =
            TaskListViewModel(repository, conditionRepository) as T
    }
}
