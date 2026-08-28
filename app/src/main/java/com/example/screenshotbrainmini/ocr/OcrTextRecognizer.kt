package com.example.screenshotbrainmini.ocr

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.Closeable

class OcrTextRecognizer(
    private val context: Context,
) : Closeable {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    fun recognize(uri: Uri, onComplete: (Result<String>) -> Unit) {
        val inputImage = createInputImage(uri, onComplete) ?: return

        recognizer.process(inputImage).addOnSuccessListener { result ->
            onComplete(Result.success(result.text.trim()))
        }.addOnFailureListener { exception ->
            onComplete(Result.failure(exception))
        }
    }

    override fun close() {
        recognizer.close()
    }

    private fun createInputImage(
        uri: Uri,
        onComplete: (Result<String>) -> Unit,
    ): InputImage? = runCatching {
        InputImage.fromFilePath(context, uri)
    }.getOrElse { exception ->
        onComplete(Result.failure(exception))
        null
    }
}
