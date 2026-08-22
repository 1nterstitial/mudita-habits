package com.mudita.habits.ui

import android.view.WindowManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.mudita.mmd.components.text.TextMMD

/**
 * Dialog styled for e-ink: pure white background, no grey scrim behind it,
 * rounded black border.
 */
@Composable
fun EInkDialog(
    title: String,
    onDismiss: () -> Unit,
    confirmLabel: String? = null,
    onConfirm: (() -> Unit)? = null,
    confirmEnabled: Boolean = true,
    dismissLabel: String? = "Cancel",
    onDismissAction: () -> Unit = onDismiss,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
        SideEffect {
            dialogWindow?.setDimAmount(0f)
            dialogWindow?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(EInkCorner)
                    .background(Color.White)
                    .border(BorderStroke(2.dp, Color.Black), EInkCorner)
                    .padding(PaddingValues(16.dp))
            ) {
                TextMMD(title, style = MaterialTheme.typography.titleLarge)
                VSpace(12)
                content()
                if (confirmLabel != null || dismissLabel != null) {
                    VSpace(16)
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (dismissLabel != null) {
                            Box(Modifier.weight(1f)) {
                                BlockButton(dismissLabel, onDismissAction)
                            }
                        }
                        if (confirmLabel != null && onConfirm != null) {
                            Box(Modifier.weight(1f)) {
                                BlockButton(
                                    label = confirmLabel,
                                    onClick = onConfirm,
                                    filled = true,
                                    enabled = confirmEnabled
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
