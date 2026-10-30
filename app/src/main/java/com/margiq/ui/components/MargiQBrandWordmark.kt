package com.margiq.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.margiq.theme.FinanceTheme

/**
 * Reusable brand wordmark component that displays "MargiQ":
 * - "Margi" adapts dynamically to the active light/dark theme text color.
 * - The terminal "Q" is highlighted in MargiQ's signature brand green.
 * - Screen readers announce the complete brand as "MargiQ" via accessibility semantics.
 */
@Composable
fun MargiQBrandWordmark(
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
    margiColor: Color = MaterialTheme.colorScheme.onSurface,
    qColor: Color = FinanceTheme.colors.profit
) {
    val annotatedString = buildAnnotatedString {
        withStyle(SpanStyle(color = margiColor)) {
            append("Margi")
        }
        withStyle(SpanStyle(color = qColor)) {
            append("Q")
        }
    }

    Text(
        text = annotatedString,
        style = style,
        maxLines = 1,
        modifier = modifier.semantics {
            contentDescription = "MargiQ"
        }
    )
}

