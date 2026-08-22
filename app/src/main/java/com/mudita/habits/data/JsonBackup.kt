package com.mudita.habits.data

import org.json.JSONArray
import org.json.JSONObject

/**
 * Minimal JSON export/import. Uses Android's built-in org.json — no third-party dep.
 * Format is intentionally boring and forward-compatible: unknown fields are ignored.
 *
 * {
 *   "version": 1,
 *   "tasks": [ {id, name, frequency, period, weekdaysMask, monthDay, onceDate, startDate, active, createdAt}, ... ],
 *   "completions": [ {taskId, date, completedAt}, ... ]
 * }
 */
object JsonBackup {

    const val VERSION = 1

    fun encode(tasks: List<TaskEntity>, completions: List<CompletionEntity>): String {
        val root = JSONObject()
        root.put("version", VERSION)

        val tasksArr = JSONArray()
        for (t in tasks) {
            val o = JSONObject()
            o.put("id", t.id)
            o.put("name", t.name)
            o.put("frequency", t.frequency)
            o.put("period", t.period)
            o.put("weekdaysMask", t.weekdaysMask)
            if (t.monthDay != null) o.put("monthDay", t.monthDay)
            if (t.onceDate != null) o.put("onceDate", t.onceDate)
            o.put("startDate", t.startDate)
            o.put("active", t.active)
            o.put("createdAt", t.createdAt)
            tasksArr.put(o)
        }
        root.put("tasks", tasksArr)

        val compArr = JSONArray()
        for (c in completions) {
            val o = JSONObject()
            o.put("taskId", c.taskId)
            o.put("date", c.date)
            o.put("completedAt", c.completedAt)
            compArr.put(o)
        }
        root.put("completions", compArr)
        return root.toString(2)
    }

    data class Decoded(val tasks: List<TaskEntity>, val completions: List<CompletionEntity>)

    /** Throws IllegalArgumentException on malformed input. */
    fun decode(text: String): Decoded {
        val root = JSONObject(text)
        require(root.optInt("version", 0) == VERSION) { "Unsupported backup version" }

        val tasks = mutableListOf<TaskEntity>()
        val tarr = root.getJSONArray("tasks")
        for (i in 0 until tarr.length()) {
            val o = tarr.getJSONObject(i)
            tasks += TaskEntity(
                id = o.getLong("id"),
                name = o.getString("name"),
                frequency = o.getString("frequency"),
                period = o.getString("period"),
                weekdaysMask = o.optInt("weekdaysMask", 0),
                monthDay = if (o.has("monthDay") && !o.isNull("monthDay")) o.getInt("monthDay") else null,
                onceDate = if (o.has("onceDate") && !o.isNull("onceDate")) o.getString("onceDate") else null,
                startDate = o.getString("startDate"),
                active = o.optBoolean("active", true),
                createdAt = o.optLong("createdAt", System.currentTimeMillis())
            )
        }

        val comps = mutableListOf<CompletionEntity>()
        val carr = root.getJSONArray("completions")
        for (i in 0 until carr.length()) {
            val o = carr.getJSONObject(i)
            comps += CompletionEntity(
                taskId = o.getLong("taskId"),
                date = o.getString("date"),
                completedAt = o.optLong("completedAt", System.currentTimeMillis())
            )
        }
        return Decoded(tasks, comps)
    }
}
