package com.mudita.habits.ui.day

import android.app.DatePickerDialog
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.mudita.habits.R
import com.mudita.habits.domain.Period
import com.mudita.habits.domain.TaskDefinition
import com.mudita.habits.ui.Divider
import com.mudita.habits.util.DateFormat
import com.mudita.mmd.components.text.TextMMD
import java.time.LocalDate

@Composable
fun DayScreen(
    vm: DayViewModel,
    onEditTask: (Long) -> Unit
) {
    val state by vm.dayUi.collectAsState()
    val ctx = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconTapButton(
                iconRes = R.drawable.ic_prev,
                contentDescription = ctx.getString(R.string.previous_day),
                onClick = { vm.previousDay() }
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { vm.today() },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextMMD(
                    DateFormat.longDate(state.date),
                    style = MaterialTheme.typography.titleLarge
                )
                TextMMD(
                    DateFormat.relativeLabel(ctx, state.date, LocalDate.now()),
                    style = MaterialTheme.typography.labelMedium
                )
            }
            IconTapButton(
                iconRes = R.drawable.ic_next,
                contentDescription = ctx.getString(R.string.next_day),
                onClick = { vm.nextDay() }
            )
        }

        Spacer(Modifier.size(8.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextMMD(
                ctx.getString(R.string.completion_summary, state.completedCount, state.totalCount),
                style = MaterialTheme.typography.bodyLarge
            )
            IconTapButton(
                iconRes = R.drawable.ic_calendar,
                contentDescription = ctx.getString(R.string.pick_date),
                onClick = { showDatePicker(ctx, state.date) { vm.setDate(it) } }
            )
        }

        Spacer(Modifier.size(8.dp))
        Divider()

        if (state.totalCount == 0) {
            Box(
                Modifier.fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                TextMMD(
                    ctx.getString(R.string.no_tasks),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(top = 8.dp)
            ) {
                for ((period, tasks) in state.groups) {
                    if (tasks.isEmpty()) continue
                    item(key = "hdr-${period.name}") {
                        PeriodHeader(headerFor(ctx, period))
                    }
                    items(tasks, key = { it.id }) { task ->
                        TaskRow(
                            task = task,
                            completed = state.completedIds.contains(task.id),
                            onToggle = { vm.toggle(task) },
                            onLongClick = { onEditTask(task.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IconTapButton(iconRes: Int, contentDescription: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(48.dp).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
private fun PeriodHeader(label: String) {
    TextMMD(
        text = label,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 6.dp)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TaskRow(
    task: TaskDefinition,
    completed: Boolean,
    onToggle: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onToggle, onLongClick = onLongClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(
                id = if (completed) R.drawable.ic_check_box_checked
                else R.drawable.ic_check_box_unchecked
            ),
            contentDescription = null,
            modifier = Modifier.size(28.dp)
        )
        Spacer(Modifier.size(12.dp))
        TextMMD(
            text = task.name,
            style = MaterialTheme.typography.bodyLarge,
            textDecoration = if (completed) TextDecoration.LineThrough else TextDecoration.None
        )
    }
}

private fun showDatePicker(
    ctx: android.content.Context,
    current: LocalDate,
    onPicked: (LocalDate) -> Unit
) {
    DatePickerDialog(
        ctx,
        { _, y, m, d -> onPicked(LocalDate.of(y, m + 1, d)) },
        current.year, current.monthValue - 1, current.dayOfMonth
    ).show()
}

private fun headerFor(ctx: android.content.Context, period: Period): String = when (period) {
    Period.MORNING -> ctx.getString(R.string.period_morning)
    Period.AFTERNOON -> ctx.getString(R.string.period_afternoon)
    Period.NIGHT -> ctx.getString(R.string.period_night)
    Period.ANY -> ctx.getString(R.string.period_any)
}
