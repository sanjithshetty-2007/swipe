package com.swipeit.app.core.common

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Short dates in "d MMM yyyy" form, e.g. "12 Nov 2024". */
object DateFormatter {
    private const val PATTERN = "d MMM yyyy"

    fun format(
        epochMillis: Long,
        locale: Locale = Locale.getDefault(),
        zone: ZoneId = ZoneId.systemDefault(),
    ): String = DateTimeFormatter.ofPattern(PATTERN, locale)
        .withZone(zone)
        .format(Instant.ofEpochMilli(epochMillis))
}
