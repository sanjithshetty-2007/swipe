package com.swipeit.app.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swipeit.app.R
import com.swipeit.app.core.designsystem.SwipeItColors
import com.swipeit.app.core.designsystem.SwipeItDimens
import com.swipeit.app.core.designsystem.SwipeItMotion
import com.swipeit.app.core.designsystem.SwipeItTheme

/** "342 photos left" with a thin green bar showing how much of the library is sorted. */
@Composable
fun ProgressHeaderCard(
    remaining: Int,
    total: Int,
    modifier: Modifier = Modifier,
) {
    val sorted = (total - remaining).coerceAtLeast(0)
    val targetProgress = if (total > 0) sorted.toFloat() / total else 0f
    val progress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = SwipeItMotion.feedback(),
        label = "sortProgress",
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(SwipeItDimens.BorderWidth, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier.padding(SwipeItDimens.SpacingMedium),
            verticalArrangement = Arrangement.spacedBy(SwipeItDimens.SpacingSmall),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = pluralStringResource(R.plurals.progress_photos_left, remaining, remaining),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = pluralStringResource(R.plurals.progress_sorted_label, total, sorted, total),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(SwipeItDimens.ProgressBarHeight),
                color = SwipeItColors.Keep,
                trackColor = SwipeItColors.SurfaceVariant,
                strokeCap = StrokeCap.Round,
                gapSize = 0.dp,
                drawStopIndicator = {},
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0B10)
@Composable
private fun ProgressHeaderCardPreview() {
    SwipeItTheme {
        Column(
            modifier = Modifier.padding(SwipeItDimens.SpacingMedium),
            verticalArrangement = Arrangement.spacedBy(SwipeItDimens.SpacingMedium),
        ) {
            ProgressHeaderCard(remaining = 342, total = 1_200)
            ProgressHeaderCard(remaining = 1, total = 1)
            ProgressHeaderCard(remaining = 0, total = 50)
        }
    }
}
