package com.mudita.habits.domain

/**
 * Task recurrence type. Stored as a string in the DB so the schema is stable
 * even if we add or reorder enum members later.
 */
enum class Frequency { ONCE, DAILY, WEEKLY, MONTHLY }

/** Time-of-day bucket a task belongs to on the day view. */
enum class Period { MORNING, AFTERNOON, NIGHT, ANY }

/** Weekday bitmask helpers. Bit 0 = Monday, Bit 6 = Sunday. Matches java.time.DayOfWeek.value - 1. */
object Weekdays {
    const val NONE = 0
    fun bitFor(dayOfWeekValue: Int): Int = 1 shl (dayOfWeekValue - 1)
    fun contains(mask: Int, dayOfWeekValue: Int): Boolean = (mask and bitFor(dayOfWeekValue)) != 0
    fun toggle(mask: Int, dayOfWeekValue: Int): Int = mask xor bitFor(dayOfWeekValue)
}
