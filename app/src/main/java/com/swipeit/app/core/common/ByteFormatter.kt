package com.swipeit.app.core.common

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/** Human-readable sizes using 1024-based units, e.g. "3.4 MB". */
object ByteFormatter {
    private const val UNIT = 1024.0
    private val UNITS = listOf("B", "KB", "MB", "GB", "TB")

    fun format(bytes: Long, locale: Locale = Locale.getDefault()): String {
        if (bytes < UNIT) return "${bytes.coerceAtLeast(0)} ${UNITS.first()}"
        var value = bytes.toDouble()
        var unitIndex = 0
        while (value >= UNIT && unitIndex < UNITS.lastIndex) {
            value /= UNIT
            unitIndex++
        }
        val number = DecimalFormat("#,##0.#", DecimalFormatSymbols.getInstance(locale)).format(value)
        return "$number ${UNITS[unitIndex]}"
    }
}
