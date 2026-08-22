package com.mudita.habits.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mudita.habits.data.HabitsRepository
import com.mudita.habits.domain.OverallStats
import com.mudita.habits.domain.Statistics
import com.mudita.habits.domain.TaskStats
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

class StatsViewModel(private val repo: HabitsRepository) : ViewModel() {

    val state: StateFlow<StatsUiState> =
        combine(repo.observeAllTasks(), repo.observeAllCompletions()) { tasks, completions ->
            val today = LocalDate.now()
            val byTask: Map<Long, Set<LocalDate>> = completions
                .groupBy { it.taskId }
                .mapValues { (_, list) -> list.map { LocalDate.parse(it.date) }.toSet() }

            // Task-level cards: skip archived tasks with zero history so the list stays useful.
            val perTask = tasks
                .filter { it.active || (byTask[it.id]?.isNotEmpty() == true) }
                .map { def -> Statistics.forTask(def, byTask[def.id] ?: emptySet(), today) }
                .filter { it.scheduledOccurrences > 0 }
                .sortedByDescending { it.completionRate }

            val overall = Statistics.overall(tasks.filter { it.active }, byTask, today)
            StatsUiState(overall = overall, perTask = perTask)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, StatsUiState.EMPTY)

    class Factory(private val repo: HabitsRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = StatsViewModel(repo) as T
    }
}

data class StatsUiState(
    val overall: OverallStats,
    val perTask: List<TaskStats>
) {
    companion object {
        val EMPTY = StatsUiState(OverallStats(0, 0, 0.0, 0.0, 0.0, 0.0), emptyList())
    }
}
