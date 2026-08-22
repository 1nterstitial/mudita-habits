package com.mudita.habits.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mudita.habits.BuildConfig
import com.mudita.habits.HabitsApp
import com.mudita.habits.R
import com.mudita.habits.data.JsonBackup
import com.mudita.habits.domain.Period
import com.mudita.habits.ui.BlockButton
import com.mudita.habits.ui.EInkDialog
import com.mudita.habits.ui.SectionCard
import com.mudita.habits.ui.VSpace
import com.mudita.habits.util.Prefs
import com.mudita.mmd.components.text.TextMMD
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.DayOfWeek

@Composable
fun SettingsScreen(
    onMessage: (String) -> Unit
) {
    val ctx = LocalContext.current
    val app = ctx.applicationContext as HabitsApp
    val scope = rememberCoroutineScope()

    var firstDow by remember { mutableStateOf(Prefs.firstDayOfWeek(ctx)) }
    var defaultPeriod by remember { mutableStateOf(Prefs.defaultPeriod(ctx)) }
    var showFirstDowPicker by remember { mutableStateOf(false) }
    var showPeriodPicker by remember { mutableStateOf(false) }
    var confirmImport by remember { mutableStateOf<Uri?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            try {
                val (tasks, comps) = app.repository.snapshot()
                val payload = JsonBackup.encode(tasks, comps)
                withContext(Dispatchers.IO) {
                    ctx.contentResolver.openOutputStream(uri)?.use {
                        it.write(payload.toByteArray(Charsets.UTF_8))
                    }
                }
                onMessage(ctx.getString(R.string.export_success))
            } catch (t: Throwable) {
                onMessage(ctx.getString(R.string.export_failure))
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) confirmImport = uri
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // First day of week
        SectionCard {
            TextMMD(
                ctx.getString(R.string.pref_first_dow_title),
                style = MaterialTheme.typography.titleMedium
            )
            VSpace(4)
            TextMMD(
                text = firstDowLabel(ctx, firstDow),
                style = MaterialTheme.typography.bodyMedium
            )
            VSpace(12)
            BlockButton("Change", { showFirstDowPicker = true })
        }
        VSpace(16)

        // Default period
        SectionCard {
            TextMMD(
                ctx.getString(R.string.pref_default_period_title),
                style = MaterialTheme.typography.titleMedium
            )
            VSpace(4)
            TextMMD(
                text = periodLabel(ctx, defaultPeriod),
                style = MaterialTheme.typography.bodyMedium
            )
            VSpace(12)
            BlockButton("Change", { showPeriodPicker = true })
        }
        VSpace(16)

        // Export
        SectionCard {
            TextMMD(
                ctx.getString(R.string.pref_export_title),
                style = MaterialTheme.typography.titleMedium
            )
            VSpace(4)
            TextMMD(
                ctx.getString(R.string.pref_export_summary),
                style = MaterialTheme.typography.bodyMedium
            )
            VSpace(12)
            BlockButton(
                label = ctx.getString(R.string.pref_export_title),
                filled = true,
                onClick = { exportLauncher.launch("habits-backup.json") }
            )
        }
        VSpace(16)

        // Import
        SectionCard {
            TextMMD(
                ctx.getString(R.string.pref_import_title),
                style = MaterialTheme.typography.titleMedium
            )
            VSpace(4)
            TextMMD(
                ctx.getString(R.string.pref_import_summary),
                style = MaterialTheme.typography.bodyMedium
            )
            VSpace(12)
            BlockButton(
                label = ctx.getString(R.string.pref_import_title),
                onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) }
            )
        }
        VSpace(16)

        // About
        SectionCard {
            TextMMD(
                ctx.getString(R.string.pref_about_title),
                style = MaterialTheme.typography.titleMedium
            )
            VSpace(4)
            TextMMD(
                ctx.getString(R.string.pref_about_summary, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    if (showFirstDowPicker) {
        ChoicePicker(
            title = ctx.getString(R.string.pref_first_dow_title),
            options = listOf(
                DayOfWeek.MONDAY to ctx.getString(R.string.pref_first_dow_monday),
                DayOfWeek.SUNDAY to ctx.getString(R.string.pref_first_dow_sunday)
            ),
            selected = firstDow,
            onDismiss = { showFirstDowPicker = false },
            onPick = {
                firstDow = it
                Prefs.setFirstDayOfWeek(ctx, it)
                showFirstDowPicker = false
            }
        )
    }

    if (showPeriodPicker) {
        ChoicePicker(
            title = ctx.getString(R.string.pref_default_period_title),
            options = listOf(
                Period.MORNING to ctx.getString(R.string.period_morning),
                Period.AFTERNOON to ctx.getString(R.string.period_afternoon),
                Period.NIGHT to ctx.getString(R.string.period_night),
                Period.ANY to ctx.getString(R.string.period_any)
            ),
            selected = defaultPeriod,
            onDismiss = { showPeriodPicker = false },
            onPick = {
                defaultPeriod = it
                Prefs.setDefaultPeriod(ctx, it)
                showPeriodPicker = false
            }
        )
    }

    confirmImport?.let { uri ->
        EInkDialog(
            title = ctx.getString(R.string.import_confirm_title),
            onDismiss = { confirmImport = null },
            confirmLabel = ctx.getString(R.string.proceed),
            onConfirm = {
                confirmImport = null
                scope.launch {
                    try {
                        val text = withContext(Dispatchers.IO) {
                            ctx.contentResolver.openInputStream(uri)!!.use { s ->
                                BufferedReader(InputStreamReader(s, Charsets.UTF_8)).readText()
                            }
                        }
                        val decoded = JsonBackup.decode(text)
                        app.repository.restoreFromSnapshot(decoded.tasks, decoded.completions)
                        onMessage(
                            ctx.getString(
                                R.string.import_success,
                                decoded.tasks.size,
                                decoded.completions.size
                            )
                        )
                    } catch (t: Throwable) {
                        onMessage(ctx.getString(R.string.import_failure))
                    }
                }
            }
        ) {
            TextMMD(
                ctx.getString(R.string.import_confirm_msg),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun <T> ChoicePicker(
    title: String,
    options: List<Pair<T, String>>,
    selected: T,
    onDismiss: () -> Unit,
    onPick: (T) -> Unit
) {
    EInkDialog(
        title = title,
        onDismiss = onDismiss,
        confirmLabel = null,
        onConfirm = null,
        dismissLabel = "Close"
    ) {
        for ((value, label) in options) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { onPick(value) }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextMMD(
                    text = (if (value == selected) "● " else "○ ") + label,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

private fun firstDowLabel(ctx: android.content.Context, dow: DayOfWeek): String = when (dow) {
    DayOfWeek.SUNDAY -> ctx.getString(R.string.pref_first_dow_sunday)
    else -> ctx.getString(R.string.pref_first_dow_monday)
}

private fun periodLabel(ctx: android.content.Context, p: Period): String = when (p) {
    Period.MORNING -> ctx.getString(R.string.period_morning)
    Period.AFTERNOON -> ctx.getString(R.string.period_afternoon)
    Period.NIGHT -> ctx.getString(R.string.period_night)
    Period.ANY -> ctx.getString(R.string.period_any)
}

