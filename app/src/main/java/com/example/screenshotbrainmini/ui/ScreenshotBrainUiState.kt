package com.example.screenshotbrainmini.ui

import com.example.screenshotbrainmini.classification.ClassificationResult

data class ScreenshotBrainUiState(
    val inputText: String = "",
    val classificationResult: ClassificationResult? = null,
    val isModelReady: Boolean = false,
    val isProcessingImage: Boolean = false,
    val informationMessage: String? = null,
    val errorMessage: String? = null,
)
