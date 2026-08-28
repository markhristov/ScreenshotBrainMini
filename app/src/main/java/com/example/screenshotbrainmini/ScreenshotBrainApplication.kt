package com.example.screenshotbrainmini

import android.app.Application
import com.example.screenshotbrainmini.di.AppContainer

class ScreenshotBrainApplication : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
