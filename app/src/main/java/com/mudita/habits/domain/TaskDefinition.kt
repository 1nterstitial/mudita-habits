package com.mudita.habits.domain

import java.time.LocalDate

/**
 * Immutable "schedule shape" of a task. Pure Kotlin — no Android or Room imports.
 * The repository maps [com.mudita.habits.data.TaskEntity] into this so the
 * recurrence and statistics code stays unit-testable on a plain JVM.
 */
data class TaskDefinition(
    val id: Long,
    val name: String,
    val frequency: Frequency,
    val period: Period,
    /** For WEEKLY: bitmask of selected weekdays (bit 0 = Monday). Zero for other frequencies. */
    val weekdaysMask: Int,
    /** For MONTHLY: 1..31. Null for other frequencies. */
    val monthDay: Int?,
    /** For ONCE: the specific date. Null for other frequencies. */
    val onceDate: LocalDate?,
    /**
     * The date the task first became active. Recurring tasks only occur on/after
     * this date, so newly-created tasks don't retroactively appear on old days
     * (which would corrupt streaks and stats).
     */
    val startDate: LocalDate,
    /** Archived tasks stay in DB (for history) but no longer appear on new days. */
    val active: Boolean
)
