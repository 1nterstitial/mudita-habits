package com.mudita.habits.domain

import java.time.LocalDate

/**
 * Pure decision function: "should this task appear on this date?"
 *
 * Documented monthly behavior: if the configured day-of-month exceeds the length
 * of the given month (e.g. day 31 in February), the task occurs on the LAST day
 * of that month rather than being silently skipped. This gives users a
 * predictable single-firing-per-month guarantee.
 *
 * An ARCHIVED task never occurs on any date. Its historical completions remain
 * in the DB, but it no longer generates future occurrences.
 */
object Recurrence {

    fun occursOn(task: TaskDefinition, date: LocalDate): Boolean {
        if (!task.active) return false
        if (date.isBefore(task.startDate)) return false

        return when (task.frequency) {
            Frequency.ONCE -> task.onceDate == date
            Frequency.DAILY -> true
            Frequency.WEEKLY -> {
                if (task.weekdaysMask == Weekdays.NONE) false
                else Weekdays.contains(task.weekdaysMask, date.dayOfWeek.value)
            }
            Frequency.MONTHLY -> {
                val day = task.monthDay ?: return false
                val lastDayOfMonth = date.lengthOfMonth()
                val effectiveDay = minOf(day, lastDayOfMonth)
                date.dayOfMonth == effectiveDay
            }
        }
    }

    /**
     * Returns the number of days between [from] and [to] (inclusive) on which
     * [task] is scheduled to occur. Used by statistics.
     */
    fun countOccurrences(task: TaskDefinition, from: LocalDate, to: LocalDate): Int {
        if (to.isBefore(from)) return 0
        var count = 0
        var d = if (from.isBefore(task.startDate)) task.startDate else from
        while (!d.isAfter(to)) {
            if (occursOn(task, d)) count++
            d = d.plusDays(1)
        }
        return count
    }
}
