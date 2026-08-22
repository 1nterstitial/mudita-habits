package com.mudita.habits.util

import android.content.Context
import android.content.SharedPreferences
import com.mudita.habits.domain.Period
import java.time.DayOfWeek

/**
 * Thin wrapper over SharedPreferences for the two persisted app settings.
 * Uses a fixed filename so backup rules pick it up.
 */
object Prefs {

    private const val FILE = "habits_prefs"
    private const val KEY_FIRST_DOW = "first_day_of_week"
    private const val KEY_DEFAULT_PERIOD = "default_period"

    private fun sp(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun firstDayOfWeek(context: Context): DayOfWeek =
        when (sp(context).getString(KEY_FIRST_DOW, "MONDAY")) {
            "SUNDAY" -> DayOfWeek.SUNDAY
            else -> DayOfWeek.MONDAY
        }

    fun setFirstDayOfWeek(context: Context, day: DayOfWeek) {
        sp(context).edit().putString(KEY_FIRST_DOW, day.name).apply()
    }

    fun defaultPeriod(context: Context): Period =
        runCatching {
            Period.valueOf(sp(context).getString(KEY_DEFAULT_PERIOD, "ANY") ?: "ANY")
        }.getOrDefault(Period.ANY)

    fun setDefaultPeriod(context: Context, period: Period) {
        sp(context).edit().putString(KEY_DEFAULT_PERIOD, period.name).apply()
    }
}
