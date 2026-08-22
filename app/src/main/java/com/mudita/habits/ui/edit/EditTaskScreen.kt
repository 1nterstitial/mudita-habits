package com.mudita.habits.ui.edit

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mudita.habits.R
import com.mudita.habits.domain.Frequency
import com.mudita.habits.domain.Period
import com.mudita.habits.domain.Weekdays
import com.mudita.habits.ui.BlockButton
import com.mudita.habits.ui.EInkDialog
import com.mudita.habits.ui.VSpace
import com.mudita.habits.util.DateFormat
import com.mudita.habits.util.Prefs
import com.mudita.mmd.components.text.TextMMD
import com.mudita.mmd.components.text_field.TextFieldMMD
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun EditTaskScreen(
    vm: EditTaskViewModel,
    taskId: Long,       // 0L = new task
    onDone: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val loaded by vm.loaded.collectAsState()

    var name by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf(Frequency.DAILY) }
    var period by remember { mutableStateOf(Prefs.defaultPeriod(ctx)) }
    var weekdaysMask by remember { mutableStateOf(0) }
    var monthDay by remember { mutableStateOf(1) }
    var onceDate by remember { mutableStateOf(LocalDate.now()) }
    var active by remember { mutableStateOf(true) }
    var showConfirmArchive by remember { mutableStateOf(false) }
    var showConfirmDelete by remember { mutableStateOf(false) }
    var showMonthDayPicker by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var weekdayError by remember { mutableStateOf<String?>(null) }
    var hydrated by remember { mutableStateOf(false) }

    LaunchedEffect(taskId) {
        if (taskId != 0L) vm.load(taskId) else hydrated = true
    }

    LaunchedEffect(loaded) {
        val t = loaded ?: return@LaunchedEffect
        if (hydrated) return@LaunchedEffect
        name = t.name
        frequency = Frequency.valueOf(t.frequency)
        period = Period.valueOf(t.period)
        weekdaysMask = t.weekdaysMask
        t.monthDay?.let { monthDay = it }
        t.onceDate?.let { onceDate = LocalDate.parse(it) }
        active = t.active
        hydrated = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        TextMMD(
            text = ctx.getString(
                if (taskId == 0L) R.string.edit_title_new else R.string.edit_title_edit
            ),
            style = MaterialTheme.typography.headlineSmall
        )
        VSpace(16)

        // Name
        SectionLabel(ctx.getString(R.string.field_name))
        TextFieldMMD(
            value = name,
            onValueChange = { name = it; nameError = null },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { TextMMD("e.g. Take vitamins") },
            isError = nameError != null,
            singleLine = true
        )
        nameError?.let {
            VSpace(4)
            TextMMD(it, style = MaterialTheme.typography.labelMedium)
        }
        VSpace(20)

        // Frequency
        SectionLabel(ctx.getString(R.string.field_frequency))
        Column {
            RadioRow(
                label = ctx.getString(R.string.freq_daily),
                selected = frequency == Frequency.DAILY
            ) { frequency = Frequency.DAILY }
            RadioRow(
                label = ctx.getString(R.string.freq_weekly),
                selected = frequency == Frequency.WEEKLY
            ) { frequency = Frequency.WEEKLY }
            RadioRow(
                label = ctx.getString(R.string.freq_monthly),
                selected = frequency == Frequency.MONTHLY
            ) { frequency = Frequency.MONTHLY }
            RadioRow(
                label = ctx.getString(R.string.freq_once),
                selected = frequency == Frequency.ONCE
            ) { frequency = Frequency.ONCE }
        }
        VSpace(16)

        // Period
        SectionLabel(ctx.getString(R.string.field_period))
        PeriodIconGrid(
            selected = period,
            onSelect = { period = it }
        )
        VSpace(16)

        // Conditional pickers
        if (frequency == Frequency.WEEKLY) {
            SectionLabel(ctx.getString(R.string.field_weekdays))
            WeekdayToggles(
                mask = weekdaysMask,
                onToggle = { dow ->
                    weekdaysMask = Weekdays.toggle(weekdaysMask, dow)
                    weekdayError = null
                }
            )
            weekdayError?.let {
                VSpace(4)
                TextMMD(it, style = MaterialTheme.typography.labelMedium)
            }
            VSpace(16)
        }

        if (frequency == Frequency.MONTHLY) {
            SectionLabel(ctx.getString(R.string.field_monthday))
            BlockButton(
                label = "Day $monthDay",
                onClick = { showMonthDayPicker = true }
            )
            VSpace(16)
        }

        if (frequency == Frequency.ONCE) {
            SectionLabel(ctx.getString(R.string.field_date))
            BlockButton(
                label = DateFormat.monthDay(onceDate),
                onClick = {
                    DatePickerDialog(
                        ctx,
                        { _, y, m, d -> onceDate = LocalDate.of(y, m + 1, d) },
                        onceDate.year, onceDate.monthValue - 1, onceDate.dayOfMonth
                    ).show()
                }
            )
            VSpace(16)
        }

        VSpace(8)

        BlockButton(
            label = ctx.getString(R.string.save),
            filled = true,
            onClick = {
                val trimmed = name.trim()
                if (trimmed.isEmpty()) {
                    nameError = "Required"
                    return@BlockButton
                }
                if (frequency == Frequency.WEEKLY && weekdaysMask == Weekdays.NONE) {
                    weekdayError = "Pick at least one weekday"
                    return@BlockButton
                }
                scope.launch {
                    vm.save(
                        existingId = taskId,
                        name = trimmed,
                        frequency = frequency,
                        period = period,
                        weekdaysMask = weekdaysMask,
                        monthDay = if (frequency == Frequency.MONTHLY) monthDay else null,
                        onceDate = if (frequency == Frequency.ONCE) onceDate else null
                    )
                    onDone()
                }
            }
        )

        if (taskId != 0L) {
            VSpace(8)
            BlockButton(
                label = ctx.getString(if (active) R.string.archive else R.string.restore),
                onClick = {
                    if (active) showConfirmArchive = true
                    else { vm.restore(taskId); onDone() }
                }
            )
            VSpace(8)
            BlockButton(
                label = ctx.getString(R.string.delete_permanent),
                onClick = { showConfirmDelete = true }
            )
        }
    }

    if (showConfirmArchive) {
        EInkDialog(
            title = ctx.getString(R.string.confirm_archive),
            onDismiss = { showConfirmArchive = false },
            confirmLabel = ctx.getString(R.string.proceed),
            onConfirm = {
                vm.archive(taskId)
                showConfirmArchive = false
                onDone()
            }
        ) { }
    }

    if (showConfirmDelete) {
        EInkDialog(
            title = ctx.getString(R.string.confirm_delete),
            onDismiss = { showConfirmDelete = false },
            confirmLabel = ctx.getString(R.string.delete_permanent),
            onConfirm = {
                vm.hardDelete(taskId)
                showConfirmDelete = false
                onDone()
            }
        ) { }
    }

    if (showMonthDayPicker) {
        MonthDayPickerDialog(
            initial = monthDay,
            onDismiss = { showMonthDayPicker = false },
            onConfirm = {
                monthDay = it
                showMonthDayPicker = false
            }
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    TextMMD(text = text, style = MaterialTheme.typography.titleSmall)
    Spacer(Modifier.height(6.dp))
}

@Composable
private fun RadioRow(label: String, selected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onSelect)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Spacer(Modifier.height(0.dp))
        TextMMD(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun PeriodIconGrid(
    selected: Period,
    onSelect: (Period) -> Unit
) {
    val ctx = LocalContext.current
    val cells = listOf(
        Period.MORNING to (R.drawable.ic_period_morning to R.string.period_morning),
        Period.AFTERNOON to (R.drawable.ic_period_afternoon to R.string.period_afternoon),
        Period.NIGHT to (R.drawable.ic_period_night to R.string.period_night),
        Period.ANY to (R.drawable.ic_period_any to R.string.period_any)
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        for ((p, art) in cells) {
            val (iconRes, labelRes) = art
            val isSelected = p == selected
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
                    .clickable { onSelect(p) }
                    .then(
                        if (isSelected)
                            Modifier.border(
                                BorderStroke(2.dp, Color.Black),
                                RoundedCornerShape(10.dp)
                            )
                        else
                            Modifier.border(
                                BorderStroke(1.dp, Color.Black.copy(alpha = 0.35f)),
                                RoundedCornerShape(10.dp)
                            )
                    )
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = ctx.getString(labelRes),
                    modifier = Modifier.size(32.dp),
                    tint = Color.Black
                )
            }
        }
    }
}

@Composable
private fun WeekdayToggles(mask: Int, onToggle: (Int) -> Unit) {
    val ctx = LocalContext.current
    val labels = listOf(
        R.string.wd_mon, R.string.wd_tue, R.string.wd_wed,
        R.string.wd_thu, R.string.wd_fri, R.string.wd_sat, R.string.wd_sun
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        for (i in 0..6) {
            val dow = i + 1
            val on = Weekdays.contains(mask, dow)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .padding(horizontal = 2.dp)
                    .clickable { onToggle(dow) },
                contentAlignment = Alignment.Center
            ) {
                TextMMD(
                    text = ctx.getString(labels[i]),
                    style = if (on) MaterialTheme.typography.titleMedium
                    else MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun MonthDayPickerDialog(
    initial: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var pick by remember { mutableStateOf(initial) }
    EInkDialog(
        title = "Day of month",
        onDismiss = onDismiss,
        confirmLabel = "OK",
        onConfirm = { onConfirm(pick) }
    ) {
        // Simple stepper: [-]  Day N  [+]
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BlockButton(
                label = "−",
                onClick = { if (pick > 1) pick-- },
                modifier = Modifier.weight(1f)
            )
            Box(Modifier.weight(2f), contentAlignment = Alignment.Center) {
                TextMMD(
                    text = "Day $pick",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            BlockButton(
                label = "+",
                onClick = { if (pick < 31) pick++ },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
