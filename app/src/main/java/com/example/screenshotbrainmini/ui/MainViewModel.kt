package com.example.screenshotbrainmini.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.screenshotbrainmini.ScreenshotBrainApplication
import com.example.screenshotbrainmini.classification.Classifier
import com.example.screenshotbrainmini.ocr.OcrTextRecognizer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val classifier: Classifier,
    private val ocrRecognizer: OcrTextRecognizer,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ScreenshotBrainUiState())

    val uiState: StateFlow<ScreenshotBrainUiState> = _uiState.asStateFlow()

    init {
        updateState { copy(isModelReady = true) }
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
        val text = _uiState.value.inputText

        when {
            text.isBlank() -> showError("Enter text or import a screenshot first.")
            else -> classifyText(text)
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

    fun clearMessage() {
        updateState {
            copy(
                informationMessage = null,
                errorMessage = null,
            )
        }
    }

    private fun classifyText(text: String) {
        viewModelScope.launch {
            try {
                val result = classifier.classify(text)
                updateState {
                    copy(
                        classificationResult = result,
                        informationMessage = null,
                        errorMessage = null,
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                showError("The text could not be classified: ${exception.readableMessage()}")
            }
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
                informationMessage = "OCR complete. The extracted text is ready for classification.",
                errorMessage = null,
            )
        }
        classifyInput()
    }

    private fun showError(message: String) {
        updateState { copy(errorMessage = message) }
    }

    private fun updateState(transform: ScreenshotBrainUiState.() -> ScreenshotBrainUiState) {
        _uiState.value = _uiState.value.transform()
    }

    private fun Throwable.readableMessage(): String = message ?: javaClass.simpleName

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application =
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ScreenshotBrainApplication

                MainViewModel(
                    classifier = application.container.classifier,
                    ocrRecognizer = application.container.ocrTextRecognizer,
                )
            }
        }
    }
}