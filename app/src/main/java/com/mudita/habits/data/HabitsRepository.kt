package com.mudita.habits.data

import com.mudita.habits.domain.TaskDefinition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/**
 * All app data flows through here. UI never touches DAOs directly.
 */
class HabitsRepository(
    private val taskDao: TaskDao,
    private val completionDao: CompletionDao
) {

    // ---- Observation ---------------------------------------------------------

    /** All tasks that should be considered when generating a day view (active only). */
    fun observeActiveTasks(): Flow<List<TaskDefinition>> =
        taskDao.observeActive().map { list -> list.map { it.toDomain() } }

    /** All tasks including archived — used by statistics and settings. */
    fun observeAllTasks(): Flow<List<TaskDefinition>> =
        taskDao.observeAll().map { list -> list.map { it.toDomain() } }

    fun observeAllCompletions(): Flow<List<CompletionEntity>> = completionDao.observeAll()

    /** Set of task ids completed on [date]. */
    fun observeCompletionsForDate(date: LocalDate): Flow<Set<Long>> =
        completionDao.observeForDate(date.toString())
            .map { list -> list.map { it.taskId }.toSet() }

    /** Combined: tasks + completion set for a specific date. */
    fun observeDay(date: LocalDate): Flow<DaySnapshot> =
        observeActiveTasks().combine(observeCompletionsForDate(date)) { tasks, completed ->
            DaySnapshot(date = date, tasks = tasks, completedTaskIds = completed)
        }

    // ---- Mutations -----------------------------------------------------------

    suspend fun getTask(id: Long): TaskDefinition? = taskDao.getById(id)?.toDomain()

    suspend fun createOrUpdate(task: TaskEntity): Long =
        if (task.id == 0L) taskDao.insert(task) else { taskDao.update(task); task.id }

    /** Archive: keeps DB row + all completion history, but hides from future days. */
    suspend fun archive(id: Long) {
        val t = taskDao.getById(id) ?: return
        taskDao.update(t.copy(active = false))
    }

    /** Restore an archived task. */
    suspend fun restore(id: Long) {
        val t = taskDao.getById(id) ?: return
        taskDao.update(t.copy(active = true))
    }

    /** Hard-delete: removes task AND cascades all completions. Use only via explicit user confirmation. */
    suspend fun hardDelete(id: Long) {
        val t = taskDao.getById(id) ?: return
        taskDao.delete(t)
    }

    /** Toggle completion for (task, date). */
    suspend fun toggleCompletion(taskId: Long, date: LocalDate) {
        val existing = completionDao.get(taskId, date.toString())
        if (existing == null) {
            completionDao.insert(CompletionEntity(taskId = taskId, date = date.toString()))
        } else {
            completionDao.delete(taskId, date.toString())
        }
    }

    // ---- Export/Import -------------------------------------------------------

    suspend fun snapshot(): Pair<List<TaskEntity>, List<CompletionEntity>> =
        taskDao.getAll() to completionDao.getAll()

    /** Wipe and repopulate the DB from an imported snapshot. */
    suspend fun restoreFromSnapshot(tasks: List<TaskEntity>, completions: List<CompletionEntity>) {
        completionDao.deleteAll()
        taskDao.deleteAll()
        // Re-insert with new ids so ordering is preserved and FK constraints hold.
        val idMap = mutableMapOf<Long, Long>()
        for (t in tasks) {
            val newId = taskDao.insert(t.copy(id = 0L))
            idMap[t.id] = newId
        }
        for (c in completions) {
            val mapped = idMap[c.taskId] ?: continue
            completionDao.insert(c.copy(taskId = mapped))
        }
    }
}

data class DaySnapshot(
    val date: LocalDate,
    val tasks: List<TaskDefinition>,
    val completedTaskIds: Set<Long>
)
