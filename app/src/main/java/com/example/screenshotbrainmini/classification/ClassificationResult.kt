package com.example.screenshotbrainmini.classification

data class ClassificationResult(
    val predictedCategory: String,
    val confidence: Double,
    val confidences: Map<String, Double>,
)
