package com.vdone.ui.loops

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vdone.data.db.TaskEntity
import com.vdone.data.repository.TaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OpenLoopsViewModel(private val repository: TaskRepository) : ViewModel() {

    val waitingTasks = repository.getWaitingTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun markDone(task: TaskEntity) {
        viewModelScope.launch { repository.toggleStatus(task) }
    }

    fun clearWaiting(task: TaskEntity) {
        viewModelScope.launch { repository.clearWaiting(task) }
    }

    class Factory(private val repository: TaskRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) =
            OpenLoopsViewModel(repository) as T
    }
}
