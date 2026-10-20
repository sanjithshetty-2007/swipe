package com.swipeit.app.domain.usecase

import com.swipeit.app.domain.model.DeletionOutcome
import com.swipeit.app.domain.model.DeletionResult
import com.swipeit.app.domain.model.Photo
import com.swipeit.app.testing.FakeConsent
import com.swipeit.app.testing.FakeDeletionRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DeletePhotosFromDeviceUseCaseTest {

    private fun photos(count: Int) = (1L..count).map { id ->
        Photo(id, "content://media/external/images/media/$id", "IMG_$id.jpg", 1_000L, 0L, "Camera", 0, 0)
    }

    @Test
    fun systemPath_chunksOf500_oneConsentPerChunk() = runTest {
        val repository = FakeDeletionRepository(systemConfirmsDeletion = true)
        val consents = mutableListOf<FakeConsent>()

        val result = DeletePhotosFromDeviceUseCase(repository)(photos(1_200)) { consent ->
            consents += consent as FakeConsent
            true
        }

        assertEquals(listOf(500, 500, 200), repository.deleteCalls.map { it.size })
        assertEquals(3, consents.size)
        assertEquals(DeletionResult(1_200, 1_200_000L, DeletionOutcome.COMPLETED), result)
    }

    @Test
    fun systemPath_cancelOnSecondChunk_keepsFirstChunkDeleted() = runTest {
        val repository = FakeDeletionRepository(systemConfirmsDeletion = true)
        var asked = 0

        val result = DeletePhotosFromDeviceUseCase(repository)(photos(700)) { asked++ == 0 }

        assertEquals(DeletionOutcome.CANCELLED, result.outcome)
        assertEquals(500, result.deletedCount)
    }

    @Test
    fun legacyPath_recoverableConsent_retriesThatPhotoAndContinues() = runTest {
        val repository = FakeDeletionRepository(systemConfirmsDeletion = false, needsConsentIds = setOf(2L, 4L))
        val consents = mutableListOf<List<Long>>()

        val result = DeletePhotosFromDeviceUseCase(repository)(photos(5)) { consent ->
            consents += (consent as FakeConsent).forIds
            true
        }

        assertEquals(listOf(listOf(2L), listOf(4L)), consents)
        assertEquals(DeletionOutcome.COMPLETED, result.outcome)
        assertEquals(5, result.deletedCount)
    }

    @Test
    fun legacyPath_userRefuses_stopsWithPartialResult() = runTest {
        val repository = FakeDeletionRepository(systemConfirmsDeletion = false, needsConsentIds = setOf(3L))

        val result = DeletePhotosFromDeviceUseCase(repository)(photos(5)) { false }

        assertEquals(DeletionOutcome.CANCELLED, result.outcome)
        assertEquals(2, result.deletedCount)
    }

    @Test
    fun legacyPath_consentAskedAgainForSamePhoto_failsInsteadOfLooping() = runTest {
        val repository = FakeDeletionRepository(
            systemConfirmsDeletion = false,
            needsConsentIds = setOf(1L),
            repeatConsentForever = true,
        )
        var asked = 0

        val result = DeletePhotosFromDeviceUseCase(repository)(photos(3)) { asked++; true }

        assertEquals(DeletionOutcome.FAILED, result.outcome)
        assertEquals(1, asked)
    }

    @Test
    fun legacyPath_failure_stops() = runTest {
        val repository = FakeDeletionRepository(systemConfirmsDeletion = false, failingIds = setOf(2L))

        val result = DeletePhotosFromDeviceUseCase(repository)(photos(4)) { true }

        assertEquals(DeletionOutcome.FAILED, result.outcome)
        assertEquals(1, result.deletedCount)
    }
}
