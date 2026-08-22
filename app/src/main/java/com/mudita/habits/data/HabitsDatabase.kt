package com.mudita.habits.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [TaskEntity::class, CompletionEntity::class],
    version = 1,
    exportSchema = true
)
abstract class HabitsDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun completionDao(): CompletionDao

    companion object {
        const val DB_NAME = "habits.db"

        fun build(context: Context): HabitsDatabase =
            Room.databaseBuilder(context.applicationContext, HabitsDatabase::class.java, DB_NAME)
                // No destructive migration — we'd rather error loudly than lose history.
                .build()
    }
}
