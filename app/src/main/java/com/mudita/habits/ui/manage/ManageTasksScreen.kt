package com.mudita.habits.ui.manage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.mudita.habits.R
import com.mudita.habits.domain.Frequency
import com.mudita.habits.domain.Period
import com.mudita.habits.domain.TaskDefinition
import com.mudita.habits.domain.Weekdays
import com.mudita.habits.ui.BlockButton
import com.mudita.habits.ui.Divider
import com.mudita.habits.ui.VSpace
import com.mudita.habits.util.DateFormat
import com.mudita.mmd.components.text.TextMMD
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun ManageTasksScreen(
    vm: ManageTasksViewModel,
    onNewTask: () -> Unit,
    onEditTask: (Long) -> Unit
) {
    val tasks by vm.tasks.collectAsState()
    val ctx = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        BlockButton(
            label = ctx.getString(R.string.menu_new_task),
            onClick = onNewTask,
            filled = true
        )
        VSpace(12)

        if (tasks.isEmpty()) {
            Box(
                Modifier.fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                TextMMD(
                    ctx.getString(R.string.manage_empty),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(tasks, key = { it.id }) { task ->
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onEditTask(task.id) }
                            .padding(vertical = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextMMD(
                                text = task.name,
                                style = MaterialTheme.typography.titleMedium,
                                textDecoration = if (!task.active)
                                    TextDecoration.LineThrough else TextDecoration.None
                            )
                            if (!task.active) {
                                TextMMD(
                                    text = ctx.getString(R.string.tag_archived),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        TextMMD(
                            text = summary(ctx, task),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Divider()
                }
            }
        }
    }
}

private fun summary(ctx: android.content.Context, task: TaskDefinition): String {
    val freq = when (task.frequency) {
        Frequency.DAILY -> ctx.getString(R.string.freq_daily)
        Frequency.WEEKLY -> ctx.getString(R.string.freq_weekly) + weekdaysSuffix(task.weekdaysMask)
        Frequency.MONTHLY -> ctx.getString(R.string.freq_monthly) +
            (task.monthDay?.let { " · day $it" } ?: "")
        Frequency.ONCE -> ctx.getString(R.string.freq_once) +
            (task.onceDate?.let { " · " + DateFormat.monthDay(it) } ?: "")
    }
    val period = when (task.period) {
        Period.MORNING -> ctx.getString(R.string.period_morning)
        Period.AFTERNOON -> ctx.getString(R.string.period_afternoon)
        Period.NIGHT -> ctx.getString(R.string.period_night)
        Period.ANY -> ctx.getString(R.string.period_any)
    }
    return "$freq · $period"
}

private fun weekdaysSuffix(mask: Int): String {
    if (mask == Weekdays.NONE) return ""
    val parts = (1..7).mapNotNull { dow ->
        if (Weekdays.contains(mask, dow))
            DayOfWeek.of(dow).getDisplayName(TextStyle.SHORT, Locale.getDefault())
        else null
    }
    return " · " + parts.joinToString(" ")
}
