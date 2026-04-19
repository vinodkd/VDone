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
    val offsetSeconds: Long = 0,
)

data class TaskDetailUiState(
    val title: String = "",
    val notes: String = "",
    val isNew: Boolean = true,
    val parentId: String? = null,
    val scheduleMode: String = "none",   // "none" | "frequency" | "fixed" | "condition"
    val frequency: String? = null,
    val frequencyDays: Int? = null,      // bitmask; null/0 = all days (only used for daily)
    val frequencyTime: Int? = null,      // minutes from midnight; null = anytime
    val fixedStart: Long? = null,
    val soundUri: String? = null,
    val pendingConditions: List<PendingCondition> = emptyList(),
    val waitingOn: String = "",
    val followUpAt: Long? = null,
    val lastRemindedAt: Long? = null,
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
                        frequencyDays = task.frequencyDays,
                        frequencyTime = task.frequencyTime,
                        fixedStart = task.fixedStart,
                        soundUri = task.soundUri,
                        waitingOn = task.waitingOn ?: "",
                        followUpAt = task.followUpAt,
                        lastRemindedAt = task.lastRemindedAt,
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
            // days filter only applies to daily; clear when switching to another frequency
            frequencyDays = if (value == "daily") _uiState.value.frequencyDays else null,
            fixedStart = null,
        )
    }

    fun setFrequencyDays(value: Int?) {
        _uiState.value = _uiState.value.copy(frequencyDays = value)
    }

    fun setFrequencyTime(value: Int?) {
        _uiState.value = _uiState.value.copy(frequencyTime = value)
    }

    fun setWaitingOn(value: String) { _uiState.value = _uiState.value.copy(waitingOn = value) }
    fun setFollowUpAt(value: Long?) { _uiState.value = _uiState.value.copy(followUpAt = value) }

    fun setSoundUri(value: String?) {
        _uiState.value = _uiState.value.copy(soundUri = value)
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

    fun addCondition(type: String, refTaskId: String?, offsetSeconds: Long = 0) {
        if (taskId != null) {
            viewModelScope.launch {
                conditionRepository.addCondition(taskId, type, refTaskId, offsetSeconds)
            }
        } else {
            val pending = _uiState.value.pendingConditions +
                PendingCondition(type = type, refTaskId = refTaskId, offsetSeconds = offsetSeconds)
            _uiState.value = _uiState.value.copy(pendingConditions = pending)
        }
    }

    fun deletePendingCondition(id: String) {
        val updated = _uiState.value.pendingConditions.filter { it.id != id }
        _uiState.value = _uiState.value.copy(pendingConditions = updated)
    }

    fun editPendingCondition(id: String, newType: String, newRefTaskId: String?, offsetSeconds: Long = 0) {
        val updated = _uiState.value.pendingConditions.map { pending ->
            if (pending.id == id) pending.copy(type = newType, refTaskId = newRefTaskId, offsetSeconds = offsetSeconds)
            else pending
        }
        _uiState.value = _uiState.value.copy(pendingConditions = updated)
    }

    fun deleteSavedCondition(conditionId: String) {
        viewModelScope.launch { conditionRepository.deleteCondition(conditionId) }
    }

    fun editSavedCondition(conditionId: String, newType: String, newRefTaskId: String?, offsetSeconds: Long = 0) {
        val targetTaskId = taskId ?: return
        viewModelScope.launch {
            conditionRepository.deleteCondition(conditionId)
            conditionRepository.addCondition(targetTaskId, newType, newRefTaskId, offsetSeconds)
        }
    }

    fun markDone() {
        val id = taskId ?: return
        viewModelScope.launch {
            val task = repository.getTaskById(id) ?: return@launch
            repository.toggleStatus(task)
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
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
                    frequencyDays = state.frequencyDays,
                    frequencyTime = state.frequencyTime,
                    fixedStart = state.fixedStart,
                    waitingOn = state.waitingOn.trim().ifBlank { null },
                    followUpAt = state.followUpAt,
                    soundUri = state.soundUri,
                )
                state.pendingConditions.forEach { pending ->
                    conditionRepository.addCondition(newId, pending.type, pending.refTaskId, pending.offsetSeconds)
                }
            } else {
                val existing = repository.getTaskById(taskId!!) ?: return@launch
                repository.updateTask(
                    existing.copy(
                        title = state.title.trim(),
                        notes = state.notes.trim().ifBlank { null },
                        scheduleMode = state.scheduleMode,
                        frequency = state.frequency,
                        frequencyDays = state.frequencyDays,
                        frequencyTime = state.frequencyTime,
                        fixedStart = state.fixedStart,
                        waitingOn = state.waitingOn.trim().ifBlank { null },
                        followUpAt = state.followUpAt,
                        soundUri = state.soundUri,
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
