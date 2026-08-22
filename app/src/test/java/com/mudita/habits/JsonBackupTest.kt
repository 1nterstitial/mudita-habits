package com.mudita.habits

import com.mudita.habits.data.CompletionEntity
import com.mudita.habits.data.JsonBackup
import com.mudita.habits.data.TaskEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class JsonBackupTest {

    @Test fun `round-trip preserves tasks and completions`() {
        val tasks = listOf(
            TaskEntity(
                id = 1, name = "Vitamins", frequency = "DAILY", period = "MORNING",
                startDate = "2026-08-01"
            ),
            TaskEntity(
                id = 2, name = "Clean", frequency = "WEEKLY", period = "ANY",
                weekdaysMask = 0b0100000, // Sat
                startDate = "2026-08-01", active = false
            )
        )
        val comps = listOf(
            CompletionEntity(taskId = 1, date = "2026-08-01"),
            CompletionEntity(taskId = 1, date = "2026-08-02"),
            CompletionEntity(taskId = 2, date = "2026-08-08")
        )
        val json = JsonBackup.encode(tasks, comps)
        val decoded = JsonBackup.decode(json)
        assertEquals(tasks.map { it.copy(createdAt = decoded.tasks[tasks.indexOf(it)].createdAt) },
            decoded.tasks.map { it.copy(createdAt = it.createdAt) })
        assertEquals(comps.map { it.date to it.taskId }.toSet(),
            decoded.completions.map { it.date to it.taskId }.toSet())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects wrong version`() {
        JsonBackup.decode("""{"version": 99, "tasks": [], "completions": []}""")
    }
}
