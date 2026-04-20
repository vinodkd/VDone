package com.vdone.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vdone.data.db.ConditionEntity
import com.vdone.data.db.TaskEntity
import com.vdone.data.repository.ConditionRepository
import com.vdone.data.repository.TaskRepository
import com.vdone.scheduler.ConditionEvaluator
import com.vdone.scheduler.FrequencyChecker
import com.vdone.ui.util.scheduleLabel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

private data class TaskData(
    val freqTasks: List<TaskEntity>,
    val fixedTasks: List<TaskEntity>,
    val allTasks: List<TaskEntity>,
)

private data class ConditionData(
    val allConditions: List<ConditionEntity>,
    val tick: Int,
)

class HomeViewModel(
    private val repository: TaskRepository,
    private val conditionRepository: ConditionRepository,
) : ViewModel() {

    private val refreshTick = MutableStateFlow(0)

    init {
        // Recompute the due-task list every minute so overdue tasks appear
        // without the user having to navigate away and back.
        viewModelScope.launch {
            while (true) {
                delay(60_000)
                refreshTick.value++
            }
        }
    }

    private val taskData = combine(
        repository.getFrequencyTasks(),
        repository.getFixedTasks(),
        repository.getAllTasks(),
    ) { freq, fixed, all -> TaskData(freq, fixed, all) }

    private val conditionData = combine(
        conditionRepository.getAllConditions(),
        refreshTick,
    ) { conditions, tick -> ConditionData(conditions, tick) }

    val dueTasks = combine(taskData, conditionData) { td, cd ->
        val now = System.currentTimeMillis()
        val endOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        val cal = Calendar.getInstance().apply { timeInMillis = now }
        val minuteOfDay = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)

        val taskMap = td.allTasks.associateBy { it.id }
        val conditionsByTask = cd.allConditions.groupBy { it.taskId }

        val dueFreq = td.freqTasks.filter { task ->
            FrequencyChecker.isDueToday(task) &&
                (task.frequencyTime == null || minuteOfDay >= task.frequencyTime) &&
                ConditionEvaluator.areAllMet(conditionsByTask[task.id].orEmpty(), taskMap, now)
        }
        val dueFixed = td.fixedTasks.filter { task ->
            task.fixedStart != null && task.fixedStart <= endOfToday &&
                ConditionEvaluator.areAllMet(conditionsByTask[task.id].orEmpty(), taskMap, now)
        }
        val dueConditional = td.allTasks.filter { task ->
            task.scheduleMode == "condition" &&
                task.status != "done" &&
                task.parentId == null &&
                ConditionEvaluator.areAllMet(conditionsByTask[task.id].orEmpty(), taskMap, now)
        }

        (dueFreq + dueFixed + dueConditional)
            .distinctBy { it.id }
            .sortedWith(compareBy(nullsLast()) { it.fixedStart })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val scheduleLabels = combine(taskData, conditionData) { td, cd ->
        val taskMap = td.allTasks.associateBy { it.id }
        val conditionsByTask = cd.allConditions.groupBy { it.taskId }
        td.allTasks.associate { task ->
            task.id to scheduleLabel(task, conditionsByTask[task.id].orEmpty(), taskMap)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

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

    fun skip(task: TaskEntity) {
        viewModelScope.launch {
            repository.skipFrequencyTask(task)
        }
    }

    class Factory(
        private val repository: TaskRepository,
        private val conditionRepository: ConditionRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) =
            HomeViewModel(repository, conditionRepository) as T
    }
}
