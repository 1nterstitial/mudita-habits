package com.mudita.habits

import android.app.Application
import com.mudita.habits.data.HabitsDatabase
import com.mudita.habits.data.HabitsRepository

/**
 * Single Application instance. Owns the database + repository so we don't need
 * a DI framework. Everything is created lazily on first access.
 */
class HabitsApp : Application() {

    // Lazy so Room doesn't touch disk until the first UI thread that needs it.
    val database: HabitsDatabase by lazy { HabitsDatabase.build(this) }
    val repository: HabitsRepository by lazy { HabitsRepository(database.taskDao(), database.completionDao()) }
}
