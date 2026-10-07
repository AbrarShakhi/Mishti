package com.abrarshakhi.mishti.common.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.abrarshakhi.mishti.common.ui.theme.Spacing

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmallEmphasized,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .padding(
                start = Spacing.Large,
                end = Spacing.Large,
                top = Spacing.Small,
                bottom = Spacing.Small,
            )
            .semantics { heading() },
    )
}
