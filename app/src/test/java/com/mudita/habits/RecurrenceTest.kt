package com.mudita.habits

import com.mudita.habits.domain.Frequency
import com.mudita.habits.domain.Period
import com.mudita.habits.domain.Recurrence
import com.mudita.habits.domain.TaskDefinition
import com.mudita.habits.domain.Weekdays
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class RecurrenceTest {

    private fun task(
        freq: Frequency,
        weekdays: Int = 0,
        monthDay: Int? = null,
        onceDate: LocalDate? = null,
        startDate: LocalDate = LocalDate.of(2026, 1, 1),
        active: Boolean = true
    ) = TaskDefinition(
        id = 1, name = "T", frequency = freq, period = Period.ANY,
        weekdaysMask = weekdays, monthDay = monthDay, onceDate = onceDate,
        startDate = startDate, active = active
    )

    @Test fun `daily occurs every day at or after start`() {
        val t = task(Frequency.DAILY, startDate = LocalDate.of(2026, 3, 10))
        assertFalse(Recurrence.occursOn(t, LocalDate.of(2026, 3, 9)))
        assertTrue(Recurrence.occursOn(t, LocalDate.of(2026, 3, 10)))
        assertTrue(Recurrence.occursOn(t, LocalDate.of(2026, 4, 20)))
    }

    @Test fun `weekly picks only chosen weekdays`() {
        // Mon and Fri
        val mask = Weekdays.bitFor(DayOfWeek.MONDAY.value) or Weekdays.bitFor(DayOfWeek.FRIDAY.value)
        val t = task(Frequency.WEEKLY, weekdays = mask)
        // 2026-08-24 is Monday, 2026-08-25 is Tuesday, 2026-08-28 is Friday, 2026-08-30 is Sunday
        assertTrue(Recurrence.occursOn(t, LocalDate.of(2026, 8, 24)))
        assertFalse(Recurrence.occursOn(t, LocalDate.of(2026, 8, 25)))
        assertTrue(Recurrence.occursOn(t, LocalDate.of(2026, 8, 28)))
        assertFalse(Recurrence.occursOn(t, LocalDate.of(2026, 8, 30)))
    }

    @Test fun `weekly with no days set never occurs`() {
        val t = task(Frequency.WEEKLY, weekdays = 0)
        assertFalse(Recurrence.occursOn(t, LocalDate.of(2026, 8, 24)))
    }

    @Test fun `monthly occurs on configured day`() {
        val t = task(Frequency.MONTHLY, monthDay = 15)
        assertTrue(Recurrence.occursOn(t, LocalDate.of(2026, 3, 15)))
        assertFalse(Recurrence.occursOn(t, LocalDate.of(2026, 3, 14)))
        assertFalse(Recurrence.occursOn(t, LocalDate.of(2026, 3, 16)))
    }

    @Test fun `monthly day 31 clamps to last day of february`() {
        val t = task(Frequency.MONTHLY, monthDay = 31, startDate = LocalDate.of(2024, 1, 1))
        // 2026 is not a leap year -> Feb has 28 days
        assertTrue(Recurrence.occursOn(t, LocalDate.of(2026, 2, 28)))
        assertFalse(Recurrence.occursOn(t, LocalDate.of(2026, 2, 27)))
        // Also fires on Feb 29 in a leap year (2024)
        assertTrue(Recurrence.occursOn(t, LocalDate.of(2024, 2, 29)))
        assertFalse(Recurrence.occursOn(t, LocalDate.of(2024, 2, 28)))
        // And on the 31st in months that have one
        assertTrue(Recurrence.occursOn(t, LocalDate.of(2026, 3, 31)))
    }

    @Test fun `monthly day 30 clamps to feb 28 non-leap`() {
        val t = task(Frequency.MONTHLY, monthDay = 30)
        assertTrue(Recurrence.occursOn(t, LocalDate.of(2026, 2, 28)))
        assertFalse(Recurrence.occursOn(t, LocalDate.of(2026, 2, 27)))
        assertTrue(Recurrence.occursOn(t, LocalDate.of(2026, 4, 30)))
    }

    @Test fun `once occurs only on exact date`() {
        val date = LocalDate.of(2026, 6, 1)
        val t = task(Frequency.ONCE, onceDate = date)
        assertTrue(Recurrence.occursOn(t, date))
        assertFalse(Recurrence.occursOn(t, date.minusDays(1)))
        assertFalse(Recurrence.occursOn(t, date.plusDays(1)))
    }

    @Test fun `archived task never occurs`() {
        val t = task(Frequency.DAILY, active = false)
        assertFalse(Recurrence.occursOn(t, LocalDate.of(2026, 5, 5)))
    }

    @Test fun `countOccurrences counts weekly correctly across a month`() {
        // Every Tue and Thu, in August 2026.
        val mask = Weekdays.bitFor(DayOfWeek.TUESDAY.value) or Weekdays.bitFor(DayOfWeek.THURSDAY.value)
        val t = task(Frequency.WEEKLY, weekdays = mask, startDate = LocalDate.of(2026, 8, 1))
        val count = Recurrence.countOccurrences(t, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31))
        // Aug 2026 Tues: 4, 11, 18, 25 (4). Thurs: 6, 13, 20, 27 (4). Total 8.
        assertEquals(8, count)
    }

    @Test fun `countOccurrences respects start date`() {
        val t = task(Frequency.DAILY, startDate = LocalDate.of(2026, 8, 15))
        val count = Recurrence.countOccurrences(t, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 20))
        assertEquals(6, count) // Aug 15..20 inclusive
    }
}
