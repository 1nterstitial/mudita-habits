package com.mudita.habits.util

import android.content.Context
import com.mudita.habits.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

object DateFormat {

    private val LONG_DATE_FMT = DateTimeFormatter.ofPattern("EEEE, MMMM d")
    private val MONTH_DAY_FMT = DateTimeFormatter.ofPattern("MMMM d, yyyy")
    private val ISO_FMT = DateTimeFormatter.ISO_LOCAL_DATE

    fun longDate(date: LocalDate, locale: Locale = Locale.getDefault()): String =
        LONG_DATE_FMT.withLocale(locale).format(date)

    fun monthDay(date: LocalDate, locale: Locale = Locale.getDefault()): String =
        MONTH_DAY_FMT.withLocale(locale).format(date)

    fun iso(date: LocalDate): String = ISO_FMT.format(date)

    fun relativeLabel(context: Context, date: LocalDate, today: LocalDate): String = when {
        date == today -> context.getString(R.string.today)
        date == today.minusDays(1) -> context.getString(R.string.yesterday)
        date == today.plusDays(1) -> context.getString(R.string.tomorrow)
        else -> date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()).uppercase(Locale.getDefault())
    }
}
