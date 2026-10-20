package com.swipeit.app.feature.trash

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.IntentSender
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalResources
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swipeit.app.R
import com.swipeit.app.core.common.ByteFormatter
import com.swipeit.app.core.permissions.IntentSenderConsent
import kotlinx.coroutines.launch

private const val TAG = "TrashScreen"

@Composable
fun TrashScreen(
    onOpenPreview: (photoId: Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TrashViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    val locale = LocalConfiguration.current.locales[0]

    // System delete confirmation (API 30+) or RecoverableSecurityException consent (API 29).
    val consentLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        viewModel.onEvent(TrashUiEvent.ConsentResult(granted = result.resultCode == Activity.RESULT_OK))
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            if (effect is TrashEffect.LaunchConsent) {
                val intentSender = (effect.consent as? IntentSenderConsent)?.intentSender
                val launched = intentSender != null && launchConsent(intentSender) { consentLauncher.launch(it) }
                if (!launched) viewModel.onEvent(TrashUiEvent.ConsentResult(granted = false))
                return@collect
            }
            snackbarHostState.currentSnackbarData?.dismiss()
            val message = when (effect) {
                is TrashEffect.ShowFreed -> resources.getString(R.string.trash_freed, ByteFormatter.format(effect.bytes, locale))
                is TrashEffect.ShowRestored -> resources.getQuantityString(R.plurals.trash_restored, effect.count, effect.count)
                is TrashEffect.ShowMessage -> resources.getString(effect.messageRes)
                is TrashEffect.LaunchConsent -> return@collect
            }
            launch { snackbarHostState.showSnackbar(message) }
        }
    }

    TrashContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
        onOpenPreview = onOpenPreview,
        modifier = modifier,
    )
}

private fun launchConsent(intentSender: IntentSender, launch: (IntentSenderRequest) -> Unit): Boolean =
    try {
        launch(IntentSenderRequest.Builder(intentSender).build())
        true
    } catch (e: ActivityNotFoundException) {
        Log.w(TAG, "No activity for delete confirmation", e)
        false
    } catch (e: IntentSender.SendIntentException) {
        Log.w(TAG, "Delete confirmation could not be sent", e)
        false
    }
