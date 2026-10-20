package com.swipeit.app.core.common

import javax.inject.Inject

/** Wall-clock source, injectable so tests control timestamps. */
fun interface TimeProvider {
    fun nowMillis(): Long
}

class SystemTimeProvider @Inject constructor() : TimeProvider {
    override fun nowMillis(): Long = System.currentTimeMillis()
}
