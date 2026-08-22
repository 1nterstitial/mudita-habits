package com.mudita.habits.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mudita.habits.domain.Frequency
import com.mudita.habits.domain.Period
import com.mudita.habits.domain.TaskDefinition
import java.time.LocalDate

/**
 * Row-level task definition. Enums stored as strings for schema stability.
 * `active=false` means archived — kept for historical completion records but
 * doesn't appear on future days.
 */
@Entity(
    tableName = "tasks",
    indices = [Index("active")]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val frequency: String,          // Frequency name
    val period: String,             // Period name
    val weekdaysMask: Int = 0,      // WEEKLY: bit 0 = Monday
    val monthDay: Int? = null,      // MONTHLY: 1..31
    val onceDate: String? = null,   // ONCE: ISO yyyy-MM-dd
    val startDate: String,          // ISO yyyy-MM-dd
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): TaskDefinition = TaskDefinition(
        id = id,
        name = name,
        frequency = Frequency.valueOf(frequency),
        period = Period.valueOf(period),
        weekdaysMask = weekdaysMask,
        monthDay = monthDay,
        onceDate = onceDate?.let(LocalDate::parse),
        startDate = LocalDate.parse(startDate),
        active = active
    )
}
