package com.swipeit.app.feature.sort

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle

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

    // Access may have changed while away (partial picker, settings); the first resume is the initial load.
    var hasResumedOnce by remember { mutableStateOf(false) }
    LifecycleResumeEffect(viewModel) {
        if (hasResumedOnce) viewModel.onEvent(SortUiEvent.Refresh) else hasResumedOnce = true
        onPauseOrDispose { }
    }

    SortContent(
        uiState = uiState,
        isPartialAccess = isPartialAccess,
        onSelectMoreClick = onSelectMoreClick,
        onAllowAllClick = onAllowAllClick,
        onSettingsClick = onSettingsClick,
        onOpenPreview = onOpenPreview,
        modifier = modifier,
    )
}
