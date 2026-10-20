package com.swipeit.app.core.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes

/** `large` is the photo-card shape (24dp). */
internal val SwipeItShapes = Shapes(
    extraSmall = RoundedCornerShape(SwipeItDimens.CornerExtraSmall),
    small = RoundedCornerShape(SwipeItDimens.CornerSmall),
    medium = RoundedCornerShape(SwipeItDimens.CornerMedium),
    large = RoundedCornerShape(SwipeItDimens.CardCornerRadius),
    extraLarge = RoundedCornerShape(SwipeItDimens.CornerExtraLarge),
)
