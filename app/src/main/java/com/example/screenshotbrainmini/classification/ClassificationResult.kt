package com.example.screenshotbrainmini.classification

data class ClassificationResult(
    val predictedCategory: String,
    val confidence: Float,
    val confidences: Map<String, Float>,
)
