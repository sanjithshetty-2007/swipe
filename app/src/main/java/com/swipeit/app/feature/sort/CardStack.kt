package com.swipeit.app.feature.sort

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.swipeit.app.core.designsystem.SwipeItDimens
import com.swipeit.app.core.designsystem.SwipeItTheme
import com.swipeit.app.domain.model.Photo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

const val MAX_VISIBLE_CARDS = 3

/**
 * Up to [MAX_VISIBLE_CARDS] cards, top card last so it draws above. Each card is keyed by
 * photo id, so animation and image state follow the photo, never the slot.
 */
@Composable
fun CardStack(
    photos: ImmutableList<Photo>,
    topCardState: SwipeCardState,
    onSwiped: (Photo, SwipeDirection) -> Unit,
    onExpandClick: (Photo) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.padding(horizontal = SwipeItDimens.StackHorizontalInset)) {
        val visible = photos.take(MAX_VISIBLE_CARDS)
        for (depth in visible.indices.reversed()) {
            val photo = visible[depth]
            key(photo.id) {
                SwipeCard(
                    photo = photo,
                    depth = depth,
                    state = if (depth == 0) topCardState else null,
                    onSwiped = { direction -> onSwiped(photo, direction) },
                    onExpandClick = { onExpandClick(photo) },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Preview(widthDp = 380, heightDp = 560, showBackground = true, backgroundColor = 0xFF0B0B10)
@Composable
private fun CardStackPreview() {
    SwipeItTheme {
        CardStack(
            photos = previewPhotos.toImmutableList(),
            topCardState = remember { SwipeCardState() },
            onSwiped = { _, _ -> },
            onExpandClick = {},
            modifier = Modifier.padding(SwipeItDimens.SpacingXLarge),
        )
    }
}
