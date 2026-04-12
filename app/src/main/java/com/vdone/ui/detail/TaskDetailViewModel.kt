package com.vdone.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vdone.data.db.TaskEntity
import com.vdone.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TaskDetailUiState(
    val title: String = "",
    val notes: String = "",
    val isNew: Boolean = true,
    val parentId: String? = null,
    val frequency: String? = null,   // null = no recurrence
    val isSaved: Boolean = false,
)

class TaskDetailViewModel(
    private val repository: TaskRepository,
    private val taskId: String?,
    private val initialParentId: String? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskDetailUiState(parentId = initialParentId))
    val uiState = _uiState.asStateFlow()

    // Live subtask list — emits automatically when children are added/removed/changed
    val subtasks = if (taskId != null) {
        repository.getChildrenFlow(taskId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    } else {
        MutableStateFlow(emptyList<TaskEntity>())
    }

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
                        frequency = task.frequency,
                    )
                }
            }
        }
    }

    fun setTitle(value: String) { _uiState.value = _uiState.value.copy(title = value) }
    fun setNotes(value: String) { _uiState.value = _uiState.value.copy(notes = value) }
    fun setFrequency(value: String?) { _uiState.value = _uiState.value.copy(frequency = value) }

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
                repository.createTask(
                    title = state.title.trim(),
                    notes = state.notes.trim().ifBlank { null },
                    parentId = state.parentId,
                    scheduleMode = if (state.frequency != null) "frequency" else "none",
                    frequency = state.frequency,
                )
            } else {
                val existing = repository.getTaskById(taskId!!) ?: return@launch
                repository.updateTask(
                    existing.copy(
                        title = state.title.trim(),
                        notes = state.notes.trim().ifBlank { null },
                        scheduleMode = if (state.frequency != null) "frequency" else "none",
                        frequency = state.frequency,
                    )
                )
            }
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }

    class Factory(
        private val repository: TaskRepository,
        private val taskId: String?,
        private val parentId: String? = null,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) =
            TaskDetailViewModel(repository, taskId, parentId) as T
    }
}
