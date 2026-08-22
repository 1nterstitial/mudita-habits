package com.mudita.habits.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mudita.habits.data.HabitsRepository
import com.mudita.habits.data.TaskEntity
import com.mudita.habits.domain.Frequency
import com.mudita.habits.domain.Period
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class EditTaskViewModel(private val repo: HabitsRepository) : ViewModel() {

    /** null = new task. non-null after load(). */
    private val _loaded = MutableStateFlow<TaskEntity?>(null)
    val loaded: StateFlow<TaskEntity?> = _loaded

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun load(id: Long) {
        if (id == 0L) return
        _isLoading.value = true
        viewModelScope.launch {
            val t = repo.getTask(id)
            _loaded.value = t?.let {
                TaskEntity(
                    id = it.id,
                    name = it.name,
                    frequency = it.frequency.name,
                    period = it.period.name,
                    weekdaysMask = it.weekdaysMask,
                    monthDay = it.monthDay,
                    onceDate = it.onceDate?.toString(),
                    startDate = it.startDate.toString(),
                    active = it.active
                )
            }
            _isLoading.value = false
        }
    }

    fun save(
        existingId: Long,
        name: String,
        frequency: Frequency,
        period: Period,
        weekdaysMask: Int,
        monthDay: Int?,
        onceDate: LocalDate?
    ) {
        viewModelScope.launch {
            val existing = if (existingId != 0L) repo.getTask(existingId) else null
            val startDate = existing?.startDate ?: LocalDate.now()
            val entity = TaskEntity(
                id = existingId,
                name = name.trim(),
                frequency = frequency.name,
                period = period.name,
                weekdaysMask = if (frequency == Frequency.WEEKLY) weekdaysMask else 0,
                monthDay = if (frequency == Frequency.MONTHLY) monthDay else null,
                onceDate = if (frequency == Frequency.ONCE) onceDate?.toString() else null,
                startDate = startDate.toString(),
                active = existing?.active ?: true
            )
            repo.createOrUpdate(entity)
        }
    }

    fun archive(id: Long) { viewModelScope.launch { repo.archive(id) } }
    fun restore(id: Long) { viewModelScope.launch { repo.restore(id) } }
    fun hardDelete(id: Long) { viewModelScope.launch { repo.hardDelete(id) } }

    class Factory(private val repo: HabitsRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = EditTaskViewModel(repo) as T
    }
}
