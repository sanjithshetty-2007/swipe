package com.swipeit.app.core.common

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Locale

class FormattersTest {

    @Test
    fun bytes_belowOneKb_areShownInBytes() {
        assertEquals("0 B", ByteFormatter.format(0, Locale.US))
        assertEquals("1023 B", ByteFormatter.format(1023, Locale.US))
    }

    @Test
    fun bytes_scaleToKbMbGb() {
        assertEquals("1 KB", ByteFormatter.format(1024, Locale.US))
        assertEquals("1.5 KB", ByteFormatter.format(1536, Locale.US))
        assertEquals("3.4 MB", ByteFormatter.format((3.4 * 1024 * 1024).toLong(), Locale.US))
        assertEquals("2 GB", ByteFormatter.format(2L * 1024 * 1024 * 1024, Locale.US))
    }

    @Test
    fun bytes_useLocaleDecimalSeparator() {
        assertEquals("3,4 MB", ByteFormatter.format((3.4 * 1024 * 1024).toLong(), Locale.GERMANY))
    }

    @Test
    fun date_usesDayShortMonthYear() {
        val millis = LocalDate.of(2024, 11, 12).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

        assertEquals("12 Nov 2024", DateFormatter.format(millis, Locale.US, ZoneOffset.UTC))
    }

    @Test
    fun date_singleDigitDay_hasNoLeadingZero() {
        val millis = LocalDate.of(2025, 3, 5).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

        assertEquals("5 Mar 2025", DateFormatter.format(millis, Locale.US, ZoneOffset.UTC))
    }
}
