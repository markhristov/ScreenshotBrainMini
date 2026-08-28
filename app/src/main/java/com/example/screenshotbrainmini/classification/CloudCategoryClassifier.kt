package com.example.screenshotbrainmini.classification

import com.example.screenshotbrainmini.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class CloudCategoryClassifier : Classifier {
    override suspend fun classify(text: String): ClassificationResult =
        withContext(Dispatchers.IO) {
            val connection = URL("${BuildConfig.CLASSIFIER_API_URL.trimEnd('/')}/predict")
                .openConnection() as HttpURLConnection

            try {
                connection.requestMethod = "POST"
                connection.setRequestProperty(
                    "Content-Type",
                    "application/json",
                )
                connection.connectTimeout = 15_000
                connection.readTimeout = 30_000
                connection.doOutput = true

                val requestBody = JSONObject()
                    .put("text", text)
                    .toString()

                connection.outputStream.bufferedWriter().use { writer ->
                    writer.write(requestBody)
                }

                val responseCode = connection.responseCode
                val responseStream = if (responseCode in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

                val responseBody = responseStream
                    .bufferedReader()
                    .use { reader -> reader.readText() }

                if (responseCode !in 200..299) {
                    error("Cloud prediction failed: $responseBody")
                }

                val response = JSONObject(responseBody)

                ClassificationResult(
                    predictedCategory =
                        response.getString("predictedCategory"),
                    confidence =
                        response.getDouble("confidence"),
                    confidences = response
                        .getJSONObject("confidences")
                        .toDoubleMap(),
                )
            } finally {
                connection.disconnect()
            }
        }

    private fun JSONObject.toDoubleMap(): Map<String, Double> =
        keys().asSequence().associateWith { key ->
            getDouble(key)
        }
}
