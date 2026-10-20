package com.swipeit.app.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.swipeit.app.R
import com.swipeit.app.core.designsystem.SwipeItIcons
import kotlin.reflect.KClass

/** Bottom-bar destinations, in display order. */
enum class TopLevelDestination(
    val route: Any,
    val routeClass: KClass<*>,
    @DrawableRes val iconRes: Int,
    @StringRes val labelRes: Int,
) {
    SORT(SortRoute, SortRoute::class, SwipeItIcons.Sort, R.string.nav_sort),
    TRASH(TrashRoute, TrashRoute::class, SwipeItIcons.Trash, R.string.nav_trash),
    KEPT(KeptRoute, KeptRoute::class, SwipeItIcons.Keep, R.string.nav_kept),
}
