package com.vdone.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vdone.data.db.ConditionEntity
import com.vdone.data.db.TaskEntity
import com.vdone.data.repository.ConditionRepository
import com.vdone.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

// Condition held in memory for new tasks (not yet persisted)
data class PendingCondition(
    val id: String = UUID.randomUUID().toString(),
    val type: String,
    val refTaskId: String?,
)

data class TaskDetailUiState(
    val title: String = "",
    val notes: String = "",
    val isNew: Boolean = true,
    val parentId: String? = null,
    val scheduleMode: String = "none",   // "none" | "frequency" | "fixed" | "condition"
    val frequency: String? = null,
    val fixedStart: Long? = null,
    val pendingConditions: List<PendingCondition> = emptyList(),
    val isSaved: Boolean = false,
)

class TaskDetailViewModel(
    private val repository: TaskRepository,
    private val conditionRepository: ConditionRepository,
    private val taskId: String?,
    private val initialParentId: String? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskDetailUiState(parentId = initialParentId))
    val uiState = _uiState.asStateFlow()

    val subtasks = if (taskId != null) {
        repository.getChildrenFlow(taskId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    } else {
        MutableStateFlow(emptyList<TaskEntity>())
    }

    // Saved conditions for existing tasks (live from DB)
    val savedConditions = if (taskId != null) {
        conditionRepository.getConditionsForTask(taskId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    } else {
        MutableStateFlow(emptyList<ConditionEntity>())
    }

    val allTasks = repository.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        if (taskId != null) {
            viewModelScope.launch {
                val task = repository.getTaskById(taskId)
                if (task != null) {
                    _uiState.value = TaskDetailUiState(
                        title = task.title,
                        notes = task.notes ?: "",
                        isNew = false,
                        parentId = task.parentId,
                        scheduleMode = task.scheduleMode,
                        frequency = task.frequency,
                        fixedStart = task.fixedStart,
                    )
                }
            }
        }
    }

    fun setTitle(value: String) { _uiState.value = _uiState.value.copy(title = value) }
    fun setNotes(value: String) { _uiState.value = _uiState.value.copy(notes = value) }

    fun setFrequency(value: String?) {
        _uiState.value = _uiState.value.copy(
            scheduleMode = if (value != null) "frequency" else "none",
            frequency = value,
            fixedStart = null,
        )
    }

    fun setFixedStart(value: Long?) {
        _uiState.value = _uiState.value.copy(
            scheduleMode = if (value != null) "fixed" else "none",
            fixedStart = value,
            frequency = null,
        )
    }

    fun setScheduleMode(mode: String) {
        _uiState.value = _uiState.value.copy(
            scheduleMode = mode,
            frequency = if (mode == "frequency") (_uiState.value.frequency ?: "daily") else null,
            fixedStart = if (mode == "fixed") _uiState.value.fixedStart else null,
        )
    }

    fun addCondition(type: String, refTaskId: String?) {
        if (taskId != null) {
            // Existing task: persist immediately
            viewModelScope.launch {
                conditionRepository.addCondition(taskId, type, refTaskId)
            }
        } else {
            // New task: queue in memory until save()
            val pending = _uiState.value.pendingConditions + PendingCondition(type = type, refTaskId = refTaskId)
            _uiState.value = _uiState.value.copy(pendingConditions = pending)
        }
    }

    fun deletePendingCondition(id: String) {
        val updated = _uiState.value.pendingConditions.filter { it.id != id }
        _uiState.value = _uiState.value.copy(pendingConditions = updated)
    }

    fun deleteSavedCondition(conditionId: String) {
        viewModelScope.launch { conditionRepository.deleteCondition(conditionId) }
    }

    fun toggleSubtaskStatus(subtask: TaskEntity) {
        viewModelScope.launch { repository.toggleStatus(subtask) }
    }

    fun deleteSubtask(subtask: TaskEntity) {
        viewModelScope.launch { repository.deleteTask(subtask) }
    }

    fun save() {
        val state = _uiState.value
        if (state.title.isBlank()) return
        viewModelScope.launch {
            if (state.isNew) {
                val newId = UUID.randomUUID().toString()
                repository.createTaskWithId(
                    id = newId,
                    title = state.title.trim(),
                    notes = state.notes.trim().ifBlank { null },
                    parentId = state.parentId,
                    scheduleMode = state.scheduleMode,
                    frequency = state.frequency,
                    fixedStart = state.fixedStart,
                )
                state.pendingConditions.forEach { pending ->
                    conditionRepository.addCondition(newId, pending.type, pending.refTaskId)
                }
            } else {
                val existing = repository.getTaskById(taskId!!) ?: return@launch
                repository.updateTask(
                    existing.copy(
                        title = state.title.trim(),
                        notes = state.notes.trim().ifBlank { null },
                        scheduleMode = state.scheduleMode,
                        frequency = state.frequency,
                        fixedStart = state.fixedStart,
                    )
                )
            }
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }

    class Factory(
        private val repository: TaskRepository,
        private val conditionRepository: ConditionRepository,
        private val taskId: String?,
        private val parentId: String? = null,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) =
            TaskDetailViewModel(repository, conditionRepository, taskId, parentId) as T
    }
}
