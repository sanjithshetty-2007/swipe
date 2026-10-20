package com.swipeit.app.feature.trash

import app.cash.turbine.test
import com.swipeit.app.R
import com.swipeit.app.domain.model.Decision
import com.swipeit.app.domain.model.Photo
import com.swipeit.app.domain.usecase.DeletePhotosFromDeviceUseCase
import com.swipeit.app.domain.usecase.ObserveTrashUseCase
import com.swipeit.app.domain.usecase.RestoreFromTrashUseCase
import com.swipeit.app.testing.FakeDeletionRepository
import com.swipeit.app.testing.FakePhotoRepository
import com.swipeit.app.testing.MainDispatcherRule
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TrashViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val photos = (1L..4L).map { id ->
        Photo(id, "content://media/external/images/media/$id", "IMG_$id.jpg", id * 1_000_000, 0L, "Camera", 0, 0)
    }
    private val photoRepository = FakePhotoRepository(photos)

    private suspend fun trashAll() = photos.forEach { photoRepository.decide(it, Decision.TRASHED) }

    private fun TestScope.viewModel(systemConfirms: Boolean = true): TrashViewModel {
        val deletion = FakeDeletionRepository(systemConfirmsDeletion = systemConfirms) { deleted ->
            val gone = deleted.mapTo(HashSet()) { it.id }
            photoRepository.reconcile(photos.map { it.id }.filterNot { it in gone }.toSet())
        }
        return TrashViewModel(
            observeTrash = ObserveTrashUseCase(photoRepository),
            restoreFromTrash = RestoreFromTrashUseCase(photoRepository),
            deletePhotosFromDevice = DeletePhotosFromDeviceUseCase(deletion),
        ).also { vm -> backgroundScope.launch { vm.uiState.collect { } } }
    }

    @Test
    fun header_showsCountAndSize() = runTest(mainDispatcherRule.dispatcher) {
        trashAll()
        val vm = viewModel()

        assertEquals(4, vm.uiState.value.photos.size)
        assertEquals(10_000_000L, vm.uiState.value.totalBytes)
    }

    @Test
    fun longPress_entersSelection_andToggles() = runTest(mainDispatcherRule.dispatcher) {
        trashAll()
        val vm = viewModel()

        vm.onEvent(TrashUiEvent.StartSelectionWith(2L))
        vm.onEvent(TrashUiEvent.ToggleSelected(3L))
        vm.onEvent(TrashUiEvent.ToggleSelected(2L))

        assertTrue(vm.uiState.value.isSelectionMode)
        assertEquals(setOf(3L), vm.uiState.value.selectedIds)
    }

    @Test
    fun restoreSelected_movesToKept_andExitsSelection() = runTest(mainDispatcherRule.dispatcher) {
        trashAll()
        val vm = viewModel()
        vm.onEvent(TrashUiEvent.StartSelectionWith(1L))

        vm.effects.test {
            vm.onEvent(TrashUiEvent.RestoreSelected)
            assertEquals(TrashEffect.ShowRestored(1), awaitItem())
        }
        assertEquals(listOf(2L, 3L, 4L), vm.uiState.value.photos.map { it.photo.id }.sorted())
        assertFalse(vm.uiState.value.isSelectionMode)
    }

    @Test
    fun deleteAll_consentGranted_removesRowsAndReportsFreedBytes() = runTest(mainDispatcherRule.dispatcher) {
        trashAll()
        val vm = viewModel()

        vm.effects.test {
            vm.onEvent(TrashUiEvent.Delete(DeleteTarget.ALL))
            assertTrue(awaitItem() is TrashEffect.LaunchConsent)
            assertTrue(vm.uiState.value.isDeleting)

            vm.onEvent(TrashUiEvent.ConsentResult(granted = true))
            assertEquals(TrashEffect.ShowFreed(10_000_000L), awaitItem())
        }
        assertTrue(vm.uiState.value.isEmpty)
        assertFalse(vm.uiState.value.isDeleting)
    }

    @Test
    fun deleteAll_consentCancelled_keepsPhotos() = runTest(mainDispatcherRule.dispatcher) {
        trashAll()
        val vm = viewModel()

        vm.effects.test {
            vm.onEvent(TrashUiEvent.Delete(DeleteTarget.ALL))
            awaitItem()
            vm.onEvent(TrashUiEvent.ConsentResult(granted = false))
            assertEquals(TrashEffect.ShowMessage(R.string.trash_delete_cancelled), awaitItem())
        }
        assertEquals(4, vm.uiState.value.photos.size)
    }

    @Test
    fun legacyApi_asksInAppConfirmationFirst() = runTest(mainDispatcherRule.dispatcher) {
        trashAll()
        val vm = viewModel(systemConfirms = false)
        vm.onEvent(TrashUiEvent.StartSelectionWith(4L))

        vm.onEvent(TrashUiEvent.Delete(DeleteTarget.SELECTED))
        assertEquals(DeleteTarget.SELECTED, vm.uiState.value.pendingConfirmation)

        vm.effects.test {
            vm.onEvent(TrashUiEvent.ConfirmDelete)
            assertEquals(TrashEffect.ShowFreed(4_000_000L), awaitItem())
        }
        assertNull(vm.uiState.value.pendingConfirmation)
        assertEquals(listOf(1L, 2L, 3L), vm.uiState.value.photos.map { it.photo.id }.sorted())
    }
}
