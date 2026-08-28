package com.example.screenshotbrainmini

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.screenshotbrainmini.ui.theme.ScreenshotBrainMiniTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ScreenshotBrainMiniTheme {
                ScreenshotBrainApp()
            }
        }
    }

}
