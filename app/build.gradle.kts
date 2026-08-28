plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val dotEnvValues = rootProject.file(".env")
    .takeIf { it.exists() }
    ?.readLines()
    ?.map(String::trim)
    ?.filter { line -> line.isNotEmpty() && !line.startsWith("#") && "=" in line }
    ?.associate { line ->
        val (key, value) = line.split("=", limit = 2)
        key.trim() to value.trim().removeSurrounding("\"").removeSurrounding("'")
    }
    .orEmpty()

val classifierApiUrl = providers
    .environmentVariable("CLASSIFIER_API_URL")
    .orNull
    ?: dotEnvValues["CLASSIFIER_API_URL"]
    ?: error("CLASSIFIER_API_URL must be defined in the root .env file or process environment.")

val escapedClassifierApiUrl = classifierApiUrl
    .replace("\\", "\\\\")
    .replace("\"", "\\\"")

android {
    namespace = "com.example.screenshotbrainmini"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.screenshotbrainmini"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField(
            type = "String",
            name = "CLASSIFIER_API_URL",
            value = "\"$escapedClassifierApiUrl\"",
        )
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.mlkit.text.recognition)
    implementation(libs.onnxruntime.android)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
