package com.mudita.habits.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * One row per (task, date) completion. A row's existence means "completed".
 * Toggling a task removes or inserts the row.
 *
 * Foreign key cascade delete triggers only when a task is HARD-deleted;
 * archiving does not delete completions.
 */
@Entity(
    tableName = "completions",
    primaryKeys = ["taskId", "date"],
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("date"), Index("taskId")]
)
data class CompletionEntity(
    val taskId: Long,
    val date: String,       // ISO yyyy-MM-dd
    val completedAt: Long = System.currentTimeMillis()
)
