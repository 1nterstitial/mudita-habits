package com.mudita.habits.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mudita.mmd.components.buttons.ButtonMMD
import com.mudita.mmd.components.buttons.OutlinedButtonMMD
import com.mudita.mmd.components.cards.CardMMD
import com.mudita.mmd.components.divider.HorizontalDividerMMD
import com.mudita.mmd.components.text.TextMMD

/** Uniform corner radius used for anything we build ourselves. */
val EInkCorner = RoundedCornerShape(10.dp)

@Composable
fun BlockButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = false,
    enabled: Boolean = true
) {
    val buttonModifier = modifier
        .fillMaxWidth()
        .defaultMinSize(minHeight = 56.dp)
    if (filled) {
        ButtonMMD(onClick = onClick, modifier = buttonModifier, enabled = enabled) {
            TextMMD(label, textAlign = TextAlign.Center)
        }
    } else {
        OutlinedButtonMMD(onClick = onClick, modifier = buttonModifier, enabled = enabled) {
            TextMMD(label, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(16.dp),
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    CardMMD(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(padding)) { content() }
    }
}

@Composable
fun Divider(modifier: Modifier = Modifier) {
    HorizontalDividerMMD(modifier = modifier.fillMaxWidth())
}

@Composable
fun VSpace(dp: Int) = Spacer(Modifier.height(dp.dp))

@Composable
fun RowSpread(content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit) =
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) { content() }
