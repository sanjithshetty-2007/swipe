package com.swipeit.app.core.permissions

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class PhotoPermissionsTest {

    @Test
    fun requestSet_api28_addsLegacyWriteForDeletion() {
        assertArrayEquals(
            arrayOf("android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"),
            PhotoPermissions.requestSet(sdkInt = 28),
        )
    }

    @Test
    fun requestSet_api32_usesReadExternalStorage() {
        assertArrayEquals(
            arrayOf("android.permission.READ_EXTERNAL_STORAGE"),
            PhotoPermissions.requestSet(sdkInt = 32),
        )
    }

    @Test
    fun requestSet_api33_usesReadMediaImagesOnly() {
        assertArrayEquals(
            arrayOf("android.permission.READ_MEDIA_IMAGES"),
            PhotoPermissions.requestSet(sdkInt = 33),
        )
    }

    @Test
    fun requestSet_api34_addsVisualUserSelected() {
        assertArrayEquals(
            arrayOf(
                "android.permission.READ_MEDIA_IMAGES",
                "android.permission.READ_MEDIA_VISUAL_USER_SELECTED",
            ),
            PhotoPermissions.requestSet(sdkInt = 34),
        )
    }

    @Test
    fun resolve_fullAccessWinsOverEverything() {
        assertEquals(PhotoPermissionState.Granted, resolve(full = true, partial = true, requested = true, rationale = true))
    }

    @Test
    fun resolve_partialOnly_isPartial() {
        assertEquals(PhotoPermissionState.Partial, resolve(partial = true))
    }

    @Test
    fun resolve_notAskedYet_isNotRequested() {
        assertEquals(PhotoPermissionState.NotRequested, resolve())
    }

    @Test
    fun resolve_rationaleShown_isDenied() {
        assertEquals(PhotoPermissionState.Denied, resolve(rationale = true))
        assertEquals(PhotoPermissionState.Denied, resolve(requested = true, rationale = true))
    }

    @Test
    fun resolve_requestedWithoutRationale_isPermanentlyDenied() {
        assertEquals(PhotoPermissionState.PermanentlyDenied, resolve(requested = true))
    }

    private fun resolve(
        full: Boolean = false,
        partial: Boolean = false,
        requested: Boolean = false,
        rationale: Boolean = false,
    ) = PhotoPermissions.resolve(
        fullAccessGranted = full,
        partialAccessGranted = partial,
        hasRequested = requested,
        shouldShowRationale = rationale,
    )
}
