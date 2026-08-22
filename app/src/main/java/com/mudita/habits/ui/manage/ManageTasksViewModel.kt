package com.mudita.habits.ui.manage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mudita.habits.data.HabitsRepository
import com.mudita.habits.domain.TaskDefinition
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.viewModelScope

class ManageTasksViewModel(repo: HabitsRepository) : ViewModel() {

    /** Every task: active first (A→Z), then archived (A→Z). Deleted tasks are gone from this list. */
    val tasks: StateFlow<List<TaskDefinition>> = repo.observeAllTasks()
        .map { list -> list.sortedWith(compareByDescending<TaskDefinition> { it.active }.thenBy { it.name.lowercase() }) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    class Factory(private val repo: HabitsRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ManageTasksViewModel(repo) as T
    }
}
