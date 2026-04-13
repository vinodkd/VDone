package com.vdone.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vdone.data.db.TaskEntity
import com.vdone.data.repository.TaskRepository
import com.vdone.scheduler.FrequencyChecker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: TaskRepository) : ViewModel() {

    private val refreshTick = MutableStateFlow(0)

    val dueTasks = combine(
        repository.getFrequencyTasks(),
        repository.getFixedTasks(),
        refreshTick,
    ) { freqTasks, fixedTasks, _ ->
        val endOfToday = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 23)
            set(java.util.Calendar.MINUTE, 59)
            set(java.util.Calendar.SECOND, 59)
            set(java.util.Calendar.MILLISECOND, 999)
        }.timeInMillis
        val dueFreq = freqTasks.filter { FrequencyChecker.isDueToday(it) }
        val dueFixed = fixedTasks.filter { it.fixedStart != null && it.fixedStart <= endOfToday }
        (dueFreq + dueFixed).sortedWith(
            compareBy(nullsLast()) { it.fixedStart }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun refresh() { refreshTick.value++ }

    fun complete(task: TaskEntity) {
        viewModelScope.launch {
            if (task.scheduleMode == "frequency") {
                repository.completeFrequencyTask(task)
            } else {
                repository.toggleStatus(task)
            }
        }
    }

    class Factory(private val repository: TaskRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) =
            HomeViewModel(repository) as T
    }
}
