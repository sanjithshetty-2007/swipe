package com.swipeit.app.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swipeit.app.R
import com.swipeit.app.core.common.ByteFormatter
import com.swipeit.app.core.common.DateFormatter
import com.swipeit.app.core.designsystem.SwipeItColors
import com.swipeit.app.core.designsystem.SwipeItDimens
import com.swipeit.app.core.designsystem.SwipeItIcons
import com.swipeit.app.core.designsystem.SwipeItTheme

/** Bottom-of-card overlay: gradient scrim, filename, "date · size" and an expand button. */
@Composable
fun PhotoInfoOverlay(
    title: String,
    subtitle: String,
    onExpandClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(SwipeItColors.Transparent, SwipeItColors.OverlayScrim)))
            .padding(
                start = SwipeItDimens.SpacingMedium,
                end = SwipeItDimens.SpacingSmall,
                top = SwipeItDimens.OverlayFadeHeight,
                bottom = SwipeItDimens.SpacingMedium,
            ),
        verticalAlignment = Alignment.Bottom,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = SwipeItColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = SwipeItColors.TextSecondary,
                maxLines = 1,
            )
        }
        IconButton(onClick = onExpandClick) {
            Icon(
                painter = painterResource(SwipeItIcons.Expand),
                contentDescription = stringResource(R.string.action_expand),
                tint = SwipeItColors.TextPrimary,
            )
        }
    }
}

/** "12 Nov 2024 · 3.4 MB" in the current locale; size only when the date is unknown. */
@Composable
@ReadOnlyComposable
fun photoInfoSubtitle(dateTakenMillis: Long, sizeBytes: Long): String {
    val locale = LocalConfiguration.current.locales[0]
    val size = ByteFormatter.format(sizeBytes, locale)
    if (dateTakenMillis <= 0L) return size
    return stringResource(R.string.photo_info_subtitle, DateFormatter.format(dateTakenMillis, locale), size)
}

@Preview
@Composable
private fun PhotoInfoOverlayPreview() {
    SwipeItTheme {
        Box(
            modifier = Modifier
                .width(300.dp)
                .aspectRatio(3f / 4f)
                .clip(MaterialTheme.shapes.large)
                .background(SwipeItColors.Info),
            contentAlignment = Alignment.BottomCenter,
        ) {
            PhotoInfoOverlay(
                title = "IMG_20241112_181455.jpg",
                subtitle = photoInfoSubtitle(dateTakenMillis = 1_731_434_095_000L, sizeBytes = 3_565_158L),
                onExpandClick = {},
            )
        }
    }
}
