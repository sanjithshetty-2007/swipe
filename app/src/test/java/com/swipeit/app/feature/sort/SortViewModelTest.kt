package com.swipeit.app.feature.sort

import app.cash.turbine.test
import com.swipeit.app.R
import com.swipeit.app.domain.model.Photo
import com.swipeit.app.domain.usecase.KeepPhotoUseCase
import com.swipeit.app.domain.usecase.ObserveSortQueueUseCase
import com.swipeit.app.domain.usecase.ObserveStatsUseCase
import com.swipeit.app.domain.usecase.RefreshPhotosUseCase
import com.swipeit.app.domain.usecase.TrashPhotoUseCase
import com.swipeit.app.domain.usecase.UndoLastDecisionUseCase
import com.swipeit.app.testing.FakePhotoRepository
import com.swipeit.app.testing.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SortViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val photos = (1L..5L).map { id ->
        Photo(id, "content://media/external/images/media/$id", "IMG_$id.jpg", id * 100, id * 1_000, "Camera", 10, 10)
    }
    private val repository = FakePhotoRepository(photos)

    private fun viewModel() = SortViewModel(
        observeSortQueue = ObserveSortQueueUseCase(repository),
        observeStats = ObserveStatsUseCase(repository),
        keepPhoto = KeepPhotoUseCase(repository),
        trashPhoto = TrashPhotoUseCase(repository),
        undoLastDecision = UndoLastDecisionUseCase(repository),
        refreshPhotos = RefreshPhotosUseCase(repository),
    )

    private fun SortUiState.ids() = queue.map { it.id }

    @Test
    fun initialState_showsTopThreeAndCounts() = runTest(mainDispatcherRule.dispatcher) {
        val vm = viewModel()
        backgroundScope.launch { vm.uiState.collect { } }

        val state = vm.uiState.value
        assertEquals(listOf(1L, 2L, 3L), state.ids())
        assertEquals(5, state.remaining)
        assertEquals(5, state.total)
        assertFalse(state.isEmpty)
        assertEquals(0f, state.progress)
    }

    @Test
    fun keep_hidesPhotoBeforeWriteCompletes() = runTest(mainDispatcherRule.dispatcher) {
        val gate = CompletableDeferred<Unit>()
        repository.writeGate = gate
        val vm = viewModel()
        backgroundScope.launch { vm.uiState.collect { } }

        vm.onEvent(SortUiEvent.Keep(photos[0]))

        // Write is still blocked, yet the card is already gone.
        assertEquals(listOf(2L, 3L, 4L), vm.uiState.value.ids())
        assertEquals(4, vm.uiState.value.remaining)

        gate.complete(Unit)

        assertEquals(listOf(2L, 3L, 4L), vm.uiState.value.ids())
        assertEquals(0.2f, vm.uiState.value.progress)
    }

    @Test
    fun undo_returnsLastPhotoToTop() = runTest(mainDispatcherRule.dispatcher) {
        val vm = viewModel()
        backgroundScope.launch { vm.uiState.collect { } }
        vm.onEvent(SortUiEvent.Trash(photos[0]))
        vm.onEvent(SortUiEvent.Keep(photos[1]))
        assertEquals(listOf(3L, 4L, 5L), vm.uiState.value.ids())

        vm.onEvent(SortUiEvent.Undo)

        assertEquals(listOf(2L, 3L, 4L), vm.uiState.value.ids())
        assertTrue(vm.uiState.value.canUndo)
    }

    @Test
    fun undo_queuedBehindPendingWrite_revertsThatWrite() = runTest(mainDispatcherRule.dispatcher) {
        val gate = CompletableDeferred<Unit>()
        repository.writeGate = gate
        val vm = viewModel()
        backgroundScope.launch { vm.uiState.collect { } }

        vm.onEvent(SortUiEvent.Keep(photos[0]))
        vm.onEvent(SortUiEvent.Undo)
        gate.complete(Unit)

        assertEquals(listOf(1L, 2L, 3L), vm.uiState.value.ids())
        assertFalse(vm.uiState.value.canUndo)
    }

    @Test
    fun failedWrite_putsCardBackAndShowsMessage() = runTest(mainDispatcherRule.dispatcher) {
        repository.failWrites = true
        val vm = viewModel()
        backgroundScope.launch { vm.uiState.collect { } }

        vm.effects.test {
            vm.onEvent(SortUiEvent.Trash(photos[0]))

            assertEquals(SortEffect.ShowMessage(R.string.sort_error_save), awaitItem())
        }
        assertEquals(listOf(1L, 2L, 3L), vm.uiState.value.ids())
    }

    @Test
    fun undo_withNothingDecided_showsMessage() = runTest(mainDispatcherRule.dispatcher) {
        val vm = viewModel()

        vm.effects.test {
            vm.onEvent(SortUiEvent.Undo)

            assertEquals(SortEffect.ShowMessage(R.string.sort_nothing_to_undo), awaitItem())
        }
    }

    @Test
    fun decidingEverything_isEmpty() = runTest(mainDispatcherRule.dispatcher) {
        val vm = viewModel()
        backgroundScope.launch { vm.uiState.collect { } }

        photos.forEach { vm.onEvent(SortUiEvent.Keep(it)) }

        assertTrue(vm.uiState.value.isEmpty)
        assertEquals(1f, vm.uiState.value.progress)
    }
}
