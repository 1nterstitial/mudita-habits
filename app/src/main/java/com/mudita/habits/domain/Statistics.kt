package com.mudita.habits.domain

import java.time.LocalDate

data class TaskStats(
    val taskId: Long,
    val name: String,
    val currentStreak: Int,
    val bestStreak: Int,
    val completions: Int,
    val scheduledOccurrences: Int,
    val completionRate: Double // 0.0..1.0
)

data class OverallStats(
    val totalScheduled: Int,
    val totalCompleted: Int,
    val completionRate: Double, // 0.0..1.0
    val dailyRate: Double,      // completion rate on DAILY tasks only
    val weeklyRate: Double,     // completion rate on WEEKLY tasks only
    val monthlyRate: Double     // completion rate on MONTHLY tasks only
)

/**
 * Pure statistics from historical completion records + task definitions.
 * Streaks are computed over SCHEDULED occurrences (a daily task's streak
 * doesn't break on a day the task wasn't supposed to fire).
 */
object Statistics {

    /**
     * @param completedDates the set of dates on which the task was marked complete.
     * @param today          the "current" date (for currentStreak endpoint).
     */
    fun forTask(
        task: TaskDefinition,
        completedDates: Set<LocalDate>,
        today: LocalDate
    ): TaskStats {
        val endDate = today
        val startDate = task.startDate
        var occurrences = 0
        var completions = 0
        var bestStreak = 0
        var running = 0

        // Walk forward over the task's active window and count occurrences,
        // completions, and best streak.
        var d = startDate
        while (!d.isAfter(endDate)) {
            if (Recurrence.occursOn(task, d)) {
                occurrences++
                if (completedDates.contains(d)) {
                    completions++
                    running++
                    if (running > bestStreak) bestStreak = running
                } else {
                    running = 0
                }
            }
            d = d.plusDays(1)
        }

        // Current streak: walk backwards from today over scheduled occurrences
        // until we hit a scheduled-but-uncompleted day. Today itself doesn't
        // break the streak if it's incomplete but the task hasn't fired yet
        // (i.e. we only look at days where the task was scheduled).
        //
        // Design choice: today counts as a streak day only when marked complete.
        // If today is scheduled and not complete, the streak is measured up to
        // the last scheduled day that WAS complete.
        var cur = 0
        var walker = endDate
        // Skip today if it's scheduled and incomplete — the streak continues from yesterday
        if (Recurrence.occursOn(task, walker) && !completedDates.contains(walker)) {
            walker = walker.minusDays(1)
        }
        while (!walker.isBefore(startDate)) {
            if (Recurrence.occursOn(task, walker)) {
                if (completedDates.contains(walker)) cur++ else break
            }
            walker = walker.minusDays(1)
        }

        val rate = if (occurrences == 0) 0.0 else completions.toDouble() / occurrences
        return TaskStats(
            taskId = task.id,
            name = task.name,
            currentStreak = cur,
            bestStreak = bestStreak,
            completions = completions,
            scheduledOccurrences = occurrences,
            completionRate = rate
        )
    }

    /**
     * Aggregates overall stats across [tasks], using [completionsByTask] as the
     * source of truth for what was completed on which date.
     */
    fun overall(
        tasks: List<TaskDefinition>,
        completionsByTask: Map<Long, Set<LocalDate>>,
        today: LocalDate
    ): OverallStats {
        var totalScheduled = 0
        var totalCompleted = 0
        var dailyScheduled = 0
        var dailyCompleted = 0
        var weeklyScheduled = 0
        var weeklyCompleted = 0
        var monthlyScheduled = 0
        var monthlyCompleted = 0

        for (task in tasks) {
            val completed = completionsByTask[task.id] ?: emptySet()
            var d = task.startDate
            while (!d.isAfter(today)) {
                if (Recurrence.occursOn(task, d)) {
                    val isComplete = completed.contains(d)
                    totalScheduled++
                    if (isComplete) totalCompleted++
                    when (task.frequency) {
                        Frequency.DAILY -> {
                            dailyScheduled++; if (isComplete) dailyCompleted++
                        }
                        Frequency.WEEKLY -> {
                            weeklyScheduled++; if (isComplete) weeklyCompleted++
                        }
                        Frequency.MONTHLY -> {
                            monthlyScheduled++; if (isComplete) monthlyCompleted++
                        }
                        Frequency.ONCE -> { /* not in periodic rate buckets */ }
                    }
                }
                d = d.plusDays(1)
            }
        }
        fun rate(c: Int, s: Int) = if (s == 0) 0.0 else c.toDouble() / s
        return OverallStats(
            totalScheduled = totalScheduled,
            totalCompleted = totalCompleted,
            completionRate = rate(totalCompleted, totalScheduled),
            dailyRate = rate(dailyCompleted, dailyScheduled),
            weeklyRate = rate(weeklyCompleted, weeklyScheduled),
            monthlyRate = rate(monthlyCompleted, monthlyScheduled)
        )
    }
}
