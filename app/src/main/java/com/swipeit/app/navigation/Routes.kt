package com.swipeit.app.navigation

import kotlinx.serialization.Serializable

@Serializable
data object SortRoute

@Serializable
data object TrashRoute

@Serializable
data object KeptRoute

@Serializable
data class PreviewRoute(val photoId: Long)

@Serializable
data object SettingsRoute
