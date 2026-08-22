package com.mudita.habits.ui.day

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mudita.habits.data.HabitsRepository
import com.mudita.habits.domain.Period
import com.mudita.habits.domain.Recurrence
import com.mudita.habits.domain.TaskDefinition
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Owns the currently-viewed date. The day snapshot rebuilds when either
 * the date changes or the underlying data changes.
 */
class DayViewModel(private val repo: HabitsRepository) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    @OptIn(ExperimentalCoroutinesApi::class)
    val dayUi: StateFlow<DayUiState> = _selectedDate
        .flatMapLatest { date ->
            repo.observeDay(date).map { snap ->
                val occurring = snap.tasks.filter { Recurrence.occursOn(it, date) }
                val grouped = Period.entries.map { p ->
                    p to occurring.filter { it.period == p }
                }
                val completedCount = occurring.count { snap.completedTaskIds.contains(it.id) }
                DayUiState(
                    date = date,
                    groups = grouped,
                    completedIds = snap.completedTaskIds,
                    completedCount = completedCount,
                    totalCount = occurring.size
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, DayUiState.EMPTY)

    fun setDate(date: LocalDate) { _selectedDate.value = date }
    fun nextDay() { _selectedDate.value = _selectedDate.value.plusDays(1) }
    fun previousDay() { _selectedDate.value = _selectedDate.value.minusDays(1) }
    fun today() { _selectedDate.value = LocalDate.now() }

    fun toggle(task: TaskDefinition) {
        val date = _selectedDate.value
        viewModelScope.launch { repo.toggleCompletion(task.id, date) }
    }

    class Factory(private val repo: HabitsRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DayViewModel(repo) as T
        }
    }
}

data class DayUiState(
    val date: LocalDate,
    val groups: List<Pair<Period, List<TaskDefinition>>>,
    val completedIds: Set<Long>,
    val completedCount: Int,
    val totalCount: Int
) {
    companion object {
        val EMPTY = DayUiState(LocalDate.now(), emptyList(), emptySet(), 0, 0)
    }
}
