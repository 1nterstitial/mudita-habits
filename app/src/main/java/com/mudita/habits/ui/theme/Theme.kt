package com.mudita.habits.ui.theme

import androidx.compose.runtime.Composable
import com.mudita.mmd.ThemeMMD

/**
 * Thin wrapper around [ThemeMMD] so the app has a single named theme entry
 * point. MMD supplies its own e-ink color scheme and typography — we accept
 * those defaults rather than fighting them.
 */
@Composable
fun MuditaHabitsTheme(content: @Composable () -> Unit) {
    ThemeMMD(content = content)
}
