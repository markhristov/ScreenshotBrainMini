package com.example.screenshotbrainmini.classification

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.Closeable

class OnnxCategoryClassifier(context: Context) : Classifier, Closeable {
    private val environment = OrtEnvironment.getEnvironment()
    private val session = createSession(context)
    private val labels = loadLabels(context)

    override suspend fun classify(text: String): ClassificationResult =
        withContext(Dispatchers.Default) {
            val inputValues = arrayOf(arrayOf(text))

            OnnxTensor.createTensor(environment, inputValues).use { inputTensor ->
                val inputs = mapOf(session.inputNames.first() to inputTensor)

                session.run(inputs).use { outputs ->
                    val scores = readProbabilityScores(outputs)
                    val predictedIndex = scores.indices.maxBy { scores[it] }

                    ClassificationResult(
                        predictedCategory = labels[predictedIndex],
                        confidence = scores[predictedIndex].toDouble(),
                        confidences = labels.indices.associate { index ->
                            labels[index] to scores[index].toDouble()
                        },
                    )
                }
            }
        }

    override fun close() {
        session.close()
    }

    private fun createSession(context: Context): OrtSession {
        val modelBytes = context.assets.open(MODEL_ASSET_NAME).use { input ->
            input.readBytes()
        }
        return environment.createSession(modelBytes, OrtSession.SessionOptions())
    }

    private fun loadLabels(context: Context): List<String> {
        val labelsJson = context.assets.open(LABELS_ASSET_NAME).bufferedReader().use { reader ->
            reader.readText()
        }
        val jsonArray = JSONArray(labelsJson)
        return List(jsonArray.length()) { index -> jsonArray.getString(index) }
    }

    @Suppress("UNCHECKED_CAST")
    private fun readProbabilityScores(outputs: OrtSession.Result): FloatArray {
        val probabilityTensor = outputs[PROBABILITIES_OUTPUT_INDEX] as OnnxTensor
        return (probabilityTensor.value as Array<FloatArray>).first()
    }

    private companion object {
        const val MODEL_ASSET_NAME = "category_classifier.onnx"
        const val LABELS_ASSET_NAME = "labels.json"
        const val PROBABILITIES_OUTPUT_INDEX = 1
    }
}
