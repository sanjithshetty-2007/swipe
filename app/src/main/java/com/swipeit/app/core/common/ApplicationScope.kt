package com.swipeit.app.core.common

import javax.inject.Qualifier

/** Process-wide CoroutineScope for work shared across screens. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
