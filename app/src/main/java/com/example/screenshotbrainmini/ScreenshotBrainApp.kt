package com.example.screenshotbrainmini

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.screenshotbrainmini.ui.MainViewModel
import com.example.screenshotbrainmini.ui.ScreenshotBrainScreen

@Composable
fun ScreenshotBrainApp(
    viewModel: MainViewModel = viewModel(factory = MainViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ScreenshotBrainScreen(
        uiState = uiState,
        onTextChanged = viewModel::updateInputText,
        onClassify = viewModel::classifyInput,
        onImageSelected = viewModel::importScreenshot,
        onDismissMessage = viewModel::clearMessage,
    )
}