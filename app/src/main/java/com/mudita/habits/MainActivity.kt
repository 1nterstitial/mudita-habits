package com.mudita.habits

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mudita.habits.ui.day.DayScreen
import com.mudita.habits.ui.day.DayViewModel
import com.mudita.habits.ui.edit.EditTaskScreen
import com.mudita.habits.ui.edit.EditTaskViewModel
import com.mudita.habits.ui.manage.ManageTasksScreen
import com.mudita.habits.ui.manage.ManageTasksViewModel
import com.mudita.habits.ui.settings.SettingsScreen
import com.mudita.habits.ui.stats.StatsScreen
import com.mudita.habits.ui.stats.StatsViewModel
import com.mudita.habits.ui.theme.MuditaHabitsTheme
import com.mudita.mmd.components.nav_bar.NavigationBarItemMMD
import com.mudita.mmd.components.nav_bar.NavigationBarMMD
import com.mudita.mmd.components.text.TextMMD
import com.mudita.mmd.components.top_app_bar.TopAppBarMMD

private enum class Tab(val label: String, val iconRes: Int) {
    DAY("Today", R.drawable.ic_calendar),
    MANAGE("Tasks", R.drawable.ic_add),
    STATS("Stats", R.drawable.ic_stats),
    SETTINGS("Settings", R.drawable.ic_settings)
}

class MainActivity : ComponentActivity() {

    private val app get() = application as HabitsApp

    private val dayVm: DayViewModel by viewModels {
        DayViewModel.Factory(app.repository)
    }
    private val manageVm: ManageTasksViewModel by viewModels {
        ManageTasksViewModel.Factory(app.repository)
    }
    private val statsVm: StatsViewModel by viewModels {
        StatsViewModel.Factory(app.repository)
    }
    private val editVm: EditTaskViewModel by viewModels {
        EditTaskViewModel.Factory(app.repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0)
        setContent {
            MuditaHabitsTheme {
                Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
                    AppScaffold(
                        dayVm = dayVm,
                        manageVm = manageVm,
                        statsVm = statsVm,
                        editVm = editVm
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        overridePendingTransition(0, 0)
        if (isTaskRoot) dayVm.today()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppScaffold(
    dayVm: DayViewModel,
    manageVm: ManageTasksViewModel,
    statsVm: StatsViewModel,
    editVm: EditTaskViewModel
) {
    var tab by remember { mutableStateOf(Tab.DAY) }
    // null = not editing. 0L = new task. non-zero = editing existing task by id.
    var editingId by remember { mutableStateOf<Long?>(null) }
    val ctx = LocalContext.current

    BackHandler(enabled = editingId != null) { editingId = null }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .background(Color.White)
    ) {
        TopAppBarMMD(
            title = {
                TextMMD(
                    when {
                        editingId != null -> ctx.getString(
                            if (editingId == 0L) R.string.edit_title_new
                            else R.string.edit_title_edit
                        )
                        tab == Tab.DAY -> ctx.getString(R.string.app_name)
                        tab == Tab.MANAGE -> ctx.getString(R.string.manage_title)
                        tab == Tab.STATS -> ctx.getString(R.string.stats_title)
                        else -> ctx.getString(R.string.settings_title)
                    }
                )
            }
        )

        Box(Modifier.weight(1f)) {
            if (editingId != null) {
                EditTaskScreen(
                    vm = editVm,
                    taskId = editingId!!,
                    onDone = { editingId = null }
                )
            } else {
                when (tab) {
                    Tab.DAY -> DayScreen(
                        vm = dayVm,
                        onEditTask = { editingId = it }
                    )
                    Tab.MANAGE -> ManageTasksScreen(
                        vm = manageVm,
                        onNewTask = { editingId = 0L },
                        onEditTask = { editingId = it }
                    )
                    Tab.STATS -> StatsScreen(statsVm)
                    Tab.SETTINGS -> SettingsScreen(
                        onMessage = { msg ->
                            Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        if (editingId == null) {
            NavigationBarMMD {
                for (t in Tab.entries) {
                    NavigationBarItemMMD(
                        selected = t == tab,
                        onClick = { tab = t },
                        icon = {
                            Icon(
                                painter = painterResource(id = t.iconRes),
                                contentDescription = t.label,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = { TextMMD(t.label) }
                    )
                }
            }
        }
    }
}
