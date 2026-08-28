package com.example.screenshotbrainmini.classification

interface Classifier {
    suspend fun classify(text: String): ClassificationResult

}
