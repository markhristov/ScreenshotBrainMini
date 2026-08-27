package com.example.screenshotbrainmini

import android.app.Activity.ScreenCaptureCallback
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.screenshotbrainmini.ui.ScreenshotBrainScreen
import com.example.screenshotbrainmini.ui.theme.ScreenshotBrainMiniTheme

class MainActivity : ComponentActivity() {
    private val viewModel: ScreenshotBrainViewModel by viewModels()
    private var screenCaptureCallback: ScreenCaptureCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ScreenshotBrainMiniTheme {
                val uiState by viewModel.uiState.collectAsState()
                ScreenshotBrainScreen(
                    uiState = uiState,
                    screenshotDetectionAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE,
                    onTextChanged = viewModel::updateInputText,
                    onClassify = viewModel::classifyInput,
                    onImageSelected = viewModel::importScreenshot,
                    onDismissMessage = viewModel::clearMessage,
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val callback = ScreenCaptureCallback(viewModel::onScreenshotDetected)
            screenCaptureCallback = callback
            registerScreenCaptureCallback(mainExecutor, callback)
        }
    }

    override fun onStop() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            screenCaptureCallback?.let(::unregisterScreenCaptureCallback)
            screenCaptureCallback = null
        }
        super.onStop()
    }
}
