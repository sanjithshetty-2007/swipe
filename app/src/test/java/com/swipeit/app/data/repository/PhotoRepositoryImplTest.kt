package com.swipeit.app.data.repository

import app.cash.turbine.test
import com.swipeit.app.core.common.TimeProvider
import com.swipeit.app.data.fake.FakeMediaSource
import com.swipeit.app.data.fake.FakeMediaSource.Companion.image
import com.swipeit.app.data.fake.FakePhotoDecisionDao
import com.swipeit.app.data.media.toPhoto
import com.swipeit.app.domain.model.Decision
import com.swipeit.app.domain.model.Photo
import com.swipeit.app.domain.model.SortOrder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PhotoRepositoryImplTest {

    private val dao = FakePhotoDecisionDao()
    private var clock = 1_000L
    private val timeProvider = TimeProvider { clock++ }

    private fun TestScope.repository(media: FakeMediaSource): PhotoRepositoryImpl {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        return PhotoRepositoryImpl(media, dao, timeProvider, backgroundScope, dispatcher, dispatcher)
    }

    private fun photo(id: Long): Photo = image(id).toPhoto()

    private fun List<Photo>.ids() = map { it.id }

    @Test
    fun decide_removesPhotoFromQueue() = runTest {
        val repo = repository(FakeMediaSource(listOf(image(3), image(2), image(1))))

        repo.unsortedQueue().test {
            assertEquals(listOf(3L, 2L, 1L), awaitItem().ids())

            repo.decide(photo(2), Decision.TRASHED)
            assertEquals(listOf(3L, 1L), awaitItem().ids())

            repo.decide(photo(3), Decision.KEPT)
            assertEquals(listOf(1L), awaitItem().ids())
        }
        assertEquals(Decision.TRASHED, dao.row(2)?.status)
        assertEquals(Decision.KEPT, dao.row(3)?.status)
    }

    @Test
    fun queue_respectsSortOrder() = runTest {
        val repo = repository(FakeMediaSource(listOf(image(3), image(2), image(1))))

        repo.unsortedQueue(SortOrder.OLDEST_FIRST).test {
            assertEquals(listOf(1L, 2L, 3L), awaitItem().ids())
        }
    }

    @Test
    fun undoLast_returnsPhotoToQueueFront() = runTest {
        val repo = repository(FakeMediaSource(listOf(image(3), image(2), image(1))))
        repo.decide(photo(3), Decision.KEPT)
        repo.decide(photo(1), Decision.TRASHED)

        repo.unsortedQueue().test {
            assertEquals(listOf(2L), awaitItem().ids())

            val undone = repo.undoLast()

            assertEquals(1L, undone?.id)
            // Natural newest-first order would be [2, 1]; the undone photo jumps to the front.
            assertEquals(listOf(1L, 2L), awaitItem().ids())
        }
        assertNull(dao.row(1))
        assertEquals(Decision.KEPT, dao.row(3)?.status)
    }

    @Test
    fun undoLast_withNoDecisions_returnsNull() = runTest {
        val repo = repository(FakeMediaSource(listOf(image(1))))

        assertNull(repo.undoLast())
    }

    @Test
    fun reset_putsPhotoAtQueueFront() = runTest {
        val repo = repository(FakeMediaSource(listOf(image(3), image(2), image(1))))
        repo.decide(photo(1), Decision.KEPT)

        repo.unsortedQueue().test {
            assertEquals(listOf(3L, 2L), awaitItem().ids())

            repo.reset(1)
            assertEquals(listOf(1L, 3L, 2L), awaitItem().ids())
        }
    }

    @Test
    fun reconcile_dropsRowsWhosePhotoIsGone() = runTest {
        val repo = repository(FakeMediaSource(emptyList()))
        listOf(1L, 2L, 5L).forEach { repo.decide(photo(it), Decision.TRASHED) }

        repo.reconcile(existingIds = setOf(1L, 2L))

        assertEquals(setOf(1L, 2L), dao.currentIds)
    }

    @Test
    fun mediaChange_withFullAccess_reconcilesAutomatically() = runTest {
        val media = FakeMediaSource(listOf(image(2), image(1)))
        val repo = repository(media)
        repo.decide(photo(1), Decision.KEPT)
        repo.decide(photo(2), Decision.TRASHED)
        backgroundScope.launch { repo.unsortedQueue().collect { } }
        runCurrent()

        media.images.value = listOf(image(1))
        runCurrent()

        assertEquals(setOf(1L), dao.currentIds)
    }

    @Test
    fun mediaChange_withPartialAccess_keepsRows() = runTest {
        val media = FakeMediaSource(listOf(image(2), image(1))).apply { fullAccess = false }
        val repo = repository(media)
        repo.decide(photo(1), Decision.KEPT)
        repo.decide(photo(2), Decision.TRASHED)
        backgroundScope.launch { repo.unsortedQueue().collect { } }

        media.images.value = listOf(image(1))
        runCurrent()

        assertEquals(setOf(1L, 2L), dao.currentIds)
    }

    @Test
    fun failedQuery_keepsRows() = runTest {
        val media = FakeMediaSource(listOf(image(1)))
        val repo = repository(media)
        repo.decide(photo(1), Decision.KEPT)
        backgroundScope.launch { repo.unsortedQueue().collect { } }

        media.images.value = null
        runCurrent()

        assertEquals(setOf(1L), dao.currentIds)
    }

    @Test
    fun restore_movesTrashedToKept() = runTest {
        val repo = repository(FakeMediaSource(listOf(image(2), image(1))))
        repo.decide(photo(1), Decision.TRASHED)
        repo.decide(photo(2), Decision.TRASHED)

        repo.restore(listOf(1L))

        assertEquals(Decision.KEPT, dao.row(1)?.status)
        assertEquals(Decision.TRASHED, dao.row(2)?.status)
    }

    @Test
    fun stats_countsUnsortedKeptTrashedAndTrashBytes() = runTest {
        val repo = repository(FakeMediaSource(listOf(image(3), image(2), image(1))))
        repo.decide(photo(3), Decision.KEPT)
        repo.decide(photo(2), Decision.TRASHED)

        repo.stats().test {
            val stats = awaitItem()
            assertEquals(1, stats.unsorted)
            assertEquals(1, stats.kept)
            assertEquals(1, stats.trashed)
            assertEquals(image(2).sizeBytes, stats.trashBytes)
        }
    }
}
