package com.swipeit.app.data.media

import android.provider.MediaStore

/** SQL sort clauses for MediaStore image queries. */
enum class MediaSortOrder(val sql: String) {
    NEWEST_FIRST(
        "${MediaStore.Images.Media.DATE_TAKEN} DESC, ${MediaStore.Images.Media.DATE_ADDED} DESC, " +
            "${MediaStore.Images.Media._ID} DESC",
    ),
    OLDEST_FIRST(
        "${MediaStore.Images.Media.DATE_TAKEN} ASC, ${MediaStore.Images.Media.DATE_ADDED} ASC, " +
            "${MediaStore.Images.Media._ID} ASC",
    ),
}
