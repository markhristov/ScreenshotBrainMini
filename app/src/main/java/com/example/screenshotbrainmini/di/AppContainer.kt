package com.example.screenshotbrainmini.di

import android.content.Context
import com.example.screenshotbrainmini.classification.Classifier
import com.example.screenshotbrainmini.classification.CloudCategoryClassifier
import com.example.screenshotbrainmini.ocr.OcrTextRecognizer

class AppContainer(
    private val context: Context,
) {
    val classifier: Classifier by lazy {
        CloudCategoryClassifier()
    }

    val ocrTextRecognizer: OcrTextRecognizer by lazy {
        OcrTextRecognizer(context)
    }
}
