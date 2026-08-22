package com.mudita.habits

import com.mudita.habits.domain.Frequency
import com.mudita.habits.domain.Period
import com.mudita.habits.domain.Statistics
import com.mudita.habits.domain.TaskDefinition
import com.mudita.habits.domain.Weekdays
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class StatisticsTest {

    private fun daily(
        id: Long = 1L,
        startDate: LocalDate = LocalDate.of(2026, 8, 1)
    ) = TaskDefinition(
        id = id, name = "Exercise", frequency = Frequency.DAILY, period = Period.ANY,
        weekdaysMask = 0, monthDay = null, onceDate = null,
        startDate = startDate, active = true
    )

    @Test fun `zero completions gives zero stats`() {
        val today = LocalDate.of(2026, 8, 20)
        val stats = Statistics.forTask(daily(), emptySet(), today)
        assertEquals(0, stats.currentStreak)
        assertEquals(0, stats.bestStreak)
        assertEquals(0, stats.completions)
        assertEquals(20, stats.scheduledOccurrences) // Aug 1..20
        assertEquals(0.0, stats.completionRate, 0.0001)
    }

    @Test fun `all-days completed gives full streak`() {
        val today = LocalDate.of(2026, 8, 10)
        val start = LocalDate.of(2026, 8, 1)
        val allDates = generateSequence(start) { it.plusDays(1) }
            .takeWhile { !it.isAfter(today) }.toSet()
        val stats = Statistics.forTask(daily(startDate = start), allDates, today)
        assertEquals(10, stats.currentStreak)
        assertEquals(10, stats.bestStreak)
        assertEquals(10, stats.completions)
        assertEquals(10, stats.scheduledOccurrences)
        assertEquals(1.0, stats.completionRate, 0.0001)
    }

    @Test fun `incomplete today does not break streak from yesterday`() {
        val today = LocalDate.of(2026, 8, 10)
        val start = LocalDate.of(2026, 8, 1)
        // Completed Aug 1..9 (9 days), Aug 10 not yet done.
        val completed = generateSequence(start) { it.plusDays(1) }
            .takeWhile { it.isBefore(today) }.toSet()
        val stats = Statistics.forTask(daily(startDate = start), completed, today)
        assertEquals(9, stats.currentStreak) // spans back through yesterday
        assertEquals(9, stats.bestStreak)
        assertEquals(9, stats.completions)
    }

    @Test fun `gap breaks current streak but preserves best streak`() {
        val today = LocalDate.of(2026, 8, 10)
        val start = LocalDate.of(2026, 8, 1)
        // Completed Aug 1,2,3,4,5 (best=5), missed Aug 6, then Aug 7,8,9,10 (current=4)
        val completed = setOf(
            LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 2),
            LocalDate.of(2026, 8, 3), LocalDate.of(2026, 8, 4),
            LocalDate.of(2026, 8, 5),
            LocalDate.of(2026, 8, 7), LocalDate.of(2026, 8, 8),
            LocalDate.of(2026, 8, 9), LocalDate.of(2026, 8, 10)
        )
        val stats = Statistics.forTask(daily(startDate = start), completed, today)
        assertEquals(4, stats.currentStreak)
        assertEquals(5, stats.bestStreak)
        assertEquals(9, stats.completions)
        assertEquals(10, stats.scheduledOccurrences)
    }

    @Test fun `weekly task streak counts scheduled days only`() {
        val start = LocalDate.of(2026, 8, 3) // Monday
        val today = LocalDate.of(2026, 8, 21) // Friday
        val mask = Weekdays.bitFor(DayOfWeek.MONDAY.value) or Weekdays.bitFor(DayOfWeek.WEDNESDAY.value) or Weekdays.bitFor(DayOfWeek.FRIDAY.value)
        val task = TaskDefinition(
            id = 2L, name = "Clean bathroom", frequency = Frequency.WEEKLY, period = Period.ANY,
            weekdaysMask = mask, monthDay = null, onceDate = null,
            startDate = start, active = true
        )
        // Scheduled days: Aug 3,5,7,10,12,14,17,19,21 (9 days)
        // Completed: all of them
        val completed = listOf(3, 5, 7, 10, 12, 14, 17, 19, 21).map { LocalDate.of(2026, 8, it) }.toSet()
        val stats = Statistics.forTask(task, completed, today)
        assertEquals(9, stats.scheduledOccurrences)
        assertEquals(9, stats.completions)
        assertEquals(9, stats.currentStreak) // Tues/Thur/etc. don't break the streak
        assertEquals(1.0, stats.completionRate, 0.0001)
    }

    @Test fun `overall aggregates across daily and weekly`() {
        val today = LocalDate.of(2026, 8, 7)
        val start = LocalDate.of(2026, 8, 1) // Sat
        val d = daily(id = 1L, startDate = start)
        val w = TaskDefinition(
            id = 2L, name = "W", frequency = Frequency.WEEKLY, period = Period.ANY,
            weekdaysMask = Weekdays.bitFor(DayOfWeek.MONDAY.value),
            monthDay = null, onceDate = null,
            startDate = start, active = true
        )
        // Daily: 7 scheduled (Aug 1..7), 5 completed
        // Weekly (Mon): scheduled Aug 3 only, completed
        val completions = mapOf(
            1L to setOf(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 2),
                LocalDate.of(2026, 8, 3), LocalDate.of(2026, 8, 4), LocalDate.of(2026, 8, 5)),
            2L to setOf(LocalDate.of(2026, 8, 3))
        )
        val overall = Statistics.overall(listOf(d, w), completions, today)
        assertEquals(8, overall.totalScheduled)  // 7 daily + 1 weekly
        assertEquals(6, overall.totalCompleted)  // 5 + 1
        assertEquals(6.0 / 8.0, overall.completionRate, 0.0001)
        assertEquals(5.0 / 7.0, overall.dailyRate, 0.0001)
        assertEquals(1.0, overall.weeklyRate, 0.0001)
    }
}
