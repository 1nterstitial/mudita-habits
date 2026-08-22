package com.mudita.habits.ui.stats

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.unit.dp
import com.mudita.habits.R
import com.mudita.habits.domain.OverallStats
import com.mudita.habits.domain.TaskStats
import com.mudita.habits.ui.SectionCard
import com.mudita.habits.ui.VSpace
import com.mudita.mmd.components.text.TextMMD
import kotlin.math.roundToInt

@Composable
fun StatsScreen(vm: StatsViewModel) {
    val state by vm.state.collectAsState()
    val ctx = LocalContext.current
    val hasData = state.overall.totalScheduled > 0 || state.perTask.isNotEmpty()

    if (!hasData) {
        Box(
            Modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            TextMMD(
                ctx.getString(R.string.stats_no_data),
                style = MaterialTheme.typography.bodyLarge
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item("overall") {
            OverallCard(state.overall)
            VSpace(16)
        }
        items(state.perTask, key = { it.taskId }) { t ->
            TaskCard(t)
            VSpace(12)
        }
    }
}

@Composable
private fun OverallCard(o: OverallStats) {
    val ctx = LocalContext.current
    SectionCard {
        TextMMD(
            ctx.getString(R.string.stats_overall),
            style = MaterialTheme.typography.titleMedium
        )
        VSpace(6)
        TextMMD(
            ctx.getString(R.string.stats_overall_pct, pct(o.completionRate)),
            style = MaterialTheme.typography.displaySmall
        )
        VSpace(12)
        Column {
            TextMMD(
                ctx.getString(R.string.stats_daily_rate, pct(o.dailyRate)),
                style = MaterialTheme.typography.bodyMedium
            )
            TextMMD(
                ctx.getString(R.string.stats_weekly_rate, pct(o.weeklyRate)),
                style = MaterialTheme.typography.bodyMedium
            )
            TextMMD(
                ctx.getString(R.string.stats_monthly_rate, pct(o.monthlyRate)),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun TaskCard(t: TaskStats) {
    val ctx = LocalContext.current
    SectionCard {
        TextMMD(t.name, style = MaterialTheme.typography.titleMedium)
        VSpace(6)
        TextMMD(
            ctx.getString(R.string.stats_current_streak, t.currentStreak, unit(ctx, t.currentStreak)),
            style = MaterialTheme.typography.bodyMedium
        )
        TextMMD(
            ctx.getString(R.string.stats_best_streak, t.bestStreak, unit(ctx, t.bestStreak)),
            style = MaterialTheme.typography.bodyMedium
        )
        VSpace(6)
        TextMMD(
            ctx.getString(R.string.stats_completion, pct(t.completionRate)) +
                "  ·  " + ctx.getString(R.string.stats_completions, t.completions),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun pct(rate: Double): Int = (rate * 100).roundToInt()

private fun unit(ctx: android.content.Context, n: Int): String =
    ctx.getString(if (n == 1) R.string.unit_day_singular else R.string.unit_day_plural)
