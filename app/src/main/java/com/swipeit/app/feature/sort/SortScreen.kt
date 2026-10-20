package com.swipeit.app.feature.sort

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swipeit.app.R
import kotlinx.coroutines.launch

@Composable
fun SortScreen(
    isPartialAccess: Boolean,
    onSelectMoreClick: () -> Unit,
    onAllowAllClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onOpenPreview: (photoId: Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SortViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            // Newest message wins; never queue a stale "UNDO" behind newer swipes.
            snackbarHostState.currentSnackbarData?.dismiss()
            launch {
                when (effect) {
                    is SortEffect.ShowMessage -> snackbarHostState.showSnackbar(resources.getString(effect.messageRes))
                    SortEffect.TrashedWithUndo -> {
                        val result = snackbarHostState.showSnackbar(
                            message = resources.getString(R.string.sort_moved_to_trash),
                            actionLabel = resources.getString(R.string.sort_undo_action),
                            duration = SnackbarDuration.Short,
                        )
                        if (result == SnackbarResult.ActionPerformed) viewModel.onEvent(SortUiEvent.Undo)
                    }
                }
            }
        }
    }

    // Access may have changed while away (partial picker, settings); the first resume is the initial load.
    var hasResumedOnce by remember { mutableStateOf(false) }
    LifecycleResumeEffect(viewModel) {
        if (hasResumedOnce) viewModel.onEvent(SortUiEvent.Refresh) else hasResumedOnce = true
        onPauseOrDispose { }
    }

    SortContent(
        uiState = uiState,
        isPartialAccess = isPartialAccess,
        snackbarHostState = snackbarHostState,
        onEvent = { event ->
            // A later keep or undo makes the "Moved to Trash · UNDO" offer stale.
            if (event is SortUiEvent.Keep || event is SortUiEvent.Undo) {
                snackbarHostState.currentSnackbarData?.dismiss()
            }
            viewModel.onEvent(event)
        },
        onSelectMoreClick = onSelectMoreClick,
        onAllowAllClick = onAllowAllClick,
        onSettingsClick = onSettingsClick,
        onOpenPreview = onOpenPreview,
        modifier = modifier,
    )
}
