package com.example.screenshotbrainmini.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.screenshotbrainmini.classification.ClassificationResult
import java.util.Locale

@Composable
fun ScreenshotBrainScreen(
    uiState: ScreenshotBrainUiState,
    onTextChanged: (String) -> Unit,
    onClassify: () -> Unit,
    onImageSelected: (Uri) -> Unit,
    onDismissMessage: () -> Unit,
) {
    val imagePicker = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        uri?.let(onImageSelected)
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Header()
            MessageCard(uiState, onDismissMessage)
            ScreenshotTextField(uiState, onTextChanged)
            ActionButtons(
                uiState = uiState,
                launchImagePicker = imagePicker::launch,
                onClassify = onClassify,
            )

            if (uiState.isProcessingImage) {
                ProcessingIndicator()
            }

            uiState.classificationResult?.let { result ->
                ClassificationCard(result)
            }
        }
    }
}

@Composable
fun Header() {
    Text(
        text = "Screenshot Brain",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
    )
    Text(
        text = "Import a screenshot or paste OCR text. Classification and OCR stay on this device.",
        style = MaterialTheme.typography.bodyLarge,
    )
}

@Composable
fun ScreenshotTextField(
    uiState: ScreenshotBrainUiState,
    onTextChanged: (String) -> Unit,
) {
    OutlinedTextField(
        value = uiState.inputText,
        onValueChange = onTextChanged,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Screenshot text") },
        placeholder = { Text("Paste text here, or import a screenshot to run OCR…") },
        minLines = 7,
        maxLines = 14,
        enabled = !uiState.isProcessingImage,
    )
}

@Composable
fun ActionButtons(
    uiState: ScreenshotBrainUiState,
    launchImagePicker: (PickVisualMediaRequest) -> Unit,
    onClassify: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedButton(
            onClick = {
                launchImagePicker(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
            },
            modifier = Modifier.weight(1f),
            enabled = !uiState.isProcessingImage,
        ) {
            Text("Import screenshot")
        }
        Button(
            onClick = onClassify,
            modifier = Modifier.weight(1f),
            enabled = uiState.isModelReady && !uiState.isProcessingImage,
        ) {
            Text("Classify text")
        }
    }
}

@Composable
fun ProcessingIndicator() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CircularProgressIndicator()
        Text("Extracting text and classifying…")
    }
}

@Composable
fun MessageCard(
    uiState: ScreenshotBrainUiState,
    onDismissMessage: () -> Unit,
) {
    val message = uiState.errorMessage ?: uiState.informationMessage ?: return
    val isError = uiState.errorMessage != null

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = if (isError) "Something needs attention" else "Information",
                fontWeight = FontWeight.SemiBold,
                color = if (isError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
            )
            Text(message)
            OutlinedButton(onClick = onDismissMessage) {
                Text("Dismiss")
            }
        }
    }
}

@Composable
fun ClassificationCard(result: ClassificationResult) {
    val rankedConfidences = result.confidences.entries.sortedByDescending { it.value }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Prediction", style = MaterialTheme.typography.titleMedium)
            Text(
                text = result.predictedCategory,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text("Confidence: ${result.confidence.asPercent()}")
            HorizontalDivider()
            Text("All categories", fontWeight = FontWeight.SemiBold)

            rankedConfidences.forEach { (category, confidence) ->
                CategoryConfidenceRow(category, confidence)
            }
        }
    }
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
fun CategoryConfidenceRow(category: String, confidence: Double) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(category, modifier = Modifier.weight(1f))
            Text(confidence.asPercent())
        }
        LinearProgressIndicator(
            progress = { confidence.toFloat() },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private fun Double.asPercent(): String = "%.1f%%".format(Locale.ROOT, this * 100f)
