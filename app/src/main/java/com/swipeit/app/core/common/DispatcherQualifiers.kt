package com.swipeit.app.core.common

import javax.inject.Qualifier

/** Dispatcher for blocking I/O: MediaStore queries, Room writes, DataStore. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

/** Dispatcher for CPU-bound work. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher
