import java.net.URI
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

// Downloads the FairScan document-segmentation model (GPLv3, pynicolas/fairscan-segmentation-model)
// into the APK assets at build time — same approach as FairScan itself.
abstract class DownloadTFLiteModelTask : DefaultTask() {
    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun download() {
        val modelVersion = "v1.2.0"
        val modelFileName = "fairscan-segmentation-model.tflite"
        val modelUrl = "https://github.com/pynicolas/fairscan-segmentation-model/releases/download/$modelVersion/$modelFileName"

        val outputFile = outputDirectory.file(modelFileName).get().asFile

        if (!outputFile.exists()) {
            println("Downloading $modelFileName from $modelUrl")
            outputFile.parentFile.mkdirs()

            URI(modelUrl).toURL().openStream().use { input ->
                outputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        } else {
            println("Model already downloaded: ${outputFile.absolutePath}")
        }
    }
}

val downloadTFLiteModel = tasks.register<DownloadTFLiteModelTask>("downloadTFLiteModel") {
    outputDirectory.set(layout.buildDirectory.dir("downloads/tflite"))
}

// Downloads the English Tesseract model (~1.6 MB, Apache-2.0, tesseract-ocr/tessdata_fast)
// into assets at build time — the exported PDFs get a searchable text layer offline,
// without an INTERNET permission or any runtime download.
abstract class DownloadOcrLanguageTask : DefaultTask() {
    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun download() {
        val modelUrl = "https://raw.githubusercontent.com/tesseract-ocr/tessdata_fast/main/eng.traineddata"
        val outputFile = outputDirectory.file("tessdata/eng.traineddata").get().asFile

        if (!outputFile.exists()) {
            println("Downloading eng.traineddata from $modelUrl")
            outputFile.parentFile.mkdirs()
            URI(modelUrl).toURL().openStream().use { input ->
                outputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        } else {
            println("OCR language data already downloaded: ${outputFile.absolutePath}")
        }
    }
}

val downloadOcrLanguage = tasks.register<DownloadOcrLanguageTask>("downloadOcrLanguage") {
    outputDirectory.set(layout.buildDirectory.dir("downloads/tessdata"))
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.corethink.bubtconnect"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.corethink.bubtconnect"
        // LiteRT (document segmentation) needs API 26; matches FairScan's floor.
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }

    // OpenCV ships native libraries for every ABI; split APKs so installs stay ~40 MB
    // instead of one 190 MB package (FairScan splits the same way).
    splits {
        abi {
            isEnable = true
            isUniversalApk = false
            reset()
            include("arm64-v8a", "armeabi-v7a", "x86_64")
        }
    }
}

androidComponents {
    onVariants { variant ->
        variant.sources.assets?.addGeneratedSourceDirectory(
            downloadTFLiteModel,
            DownloadTFLiteModelTask::outputDirectory,
        )
        variant.sources.assets?.addGeneratedSourceDirectory(
            downloadOcrLanguage,
            DownloadOcrLanguageTask::outputDirectory,
        )
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.remote.creation.core)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.ui.text.google.fonts)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    // Scanning stack (FairScan, GPLv3 — same license as this app): LiteRT segmentation model,
    // OpenCV quad extraction / perspective warp / enhancement.
    implementation(project(":imageprocessing")) {
        exclude(group = "org.openpnp", module = "opencv")
    }
    implementation(libs.opencv)
    implementation(libs.litert)
    implementation(libs.litert.support)
    implementation(libs.litert.metadata)
    implementation(libs.tesseract4android)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}