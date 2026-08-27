package com.example.screenshotbrainmini

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.example.screenshotbrainmini.classification.OnnxCategoryClassifier
import com.example.screenshotbrainmini.ocr.OcrTextRecognizer
import com.example.screenshotbrainmini.ui.ScreenshotBrainUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ScreenshotBrainViewModel(application: Application) : AndroidViewModel(application) {
    private val ocrRecognizer = OcrTextRecognizer(application)
    private val mutableUiState = MutableStateFlow(ScreenshotBrainUiState())

    private var classifier: OnnxCategoryClassifier? = null

    val uiState: StateFlow<ScreenshotBrainUiState> = mutableUiState.asStateFlow()

    init {
        loadClassifier(application)
    }

    fun updateInputText(text: String) {
        updateState {
            copy(
                inputText = text,
                classificationResult = null,
                errorMessage = null,
            )
        }
    }

    fun classifyInput() {
        val text = mutableUiState.value.inputText
        val loadedClassifier = classifier

        when {
            text.isBlank() -> showError("Enter text or import a screenshot first.")
            loadedClassifier == null -> showError("The classifier is not ready yet.")
            else -> classifyText(loadedClassifier, text)
        }
    }

    fun importScreenshot(uri: Uri) {
        updateState {
            copy(
                isProcessingImage = true,
                classificationResult = null,
                informationMessage = "Reading screenshot text on this device…",
                errorMessage = null,
            )
        }

        ocrRecognizer.recognize(uri) { result ->
            result
                .onSuccess(::handleRecognizedText)
                .onFailure { exception ->
                    updateState {
                        copy(
                            isProcessingImage = false,
                            informationMessage = null,
                            errorMessage = "The screenshot could not be read: ${exception.readableMessage()}",
                        )
                    }
                }
        }
    }

    fun onScreenshotDetected() {
        updateState {
            copy(
                informationMessage = "Screenshot detected. Tap Import screenshot to analyze its text.",
            )
        }
    }

    fun clearMessage() {
        updateState {
            copy(
                informationMessage = null,
                errorMessage = null,
            )
        }
    }

    override fun onCleared() {
        classifier?.close()
        ocrRecognizer.close()
        super.onCleared()
    }

    private fun loadClassifier(application: Application) {
        runCatching { OnnxCategoryClassifier(application) }
            .onSuccess { loadedClassifier ->
                classifier = loadedClassifier
                updateState { copy(isModelReady = true) }
            }
            .onFailure { exception ->
                showError("The classifier model could not be loaded: ${exception.readableMessage()}")
            }
    }

    private fun classifyText(classifier: OnnxCategoryClassifier, text: String) {
        runCatching { classifier.classify(text) }
            .onSuccess { result ->
                updateState {
                    copy(
                        classificationResult = result,
                        informationMessage = null,
                        errorMessage = null,
                    )
                }
            }
            .onFailure { exception ->
                showError("The text could not be classified: ${exception.readableMessage()}")
            }
    }

    private fun handleRecognizedText(text: String) {
        if (text.isBlank()) {
            updateState {
                copy(
                    isProcessingImage = false,
                    informationMessage = null,
                    errorMessage = "No readable text was found in that image.",
                )
            }
            return
        }

        updateState {
            copy(
                inputText = text,
                isProcessingImage = false,
                informationMessage = "OCR complete. The extracted text was classified locally.",
                errorMessage = null,
            )
        }
        classifyInput()
    }

    private fun showError(message: String) {
        updateState { copy(errorMessage = message) }
    }

    private fun updateState(transform: ScreenshotBrainUiState.() -> ScreenshotBrainUiState) {
        mutableUiState.value = mutableUiState.value.transform()
    }

    private fun Throwable.readableMessage(): String = message ?: javaClass.simpleName
}
