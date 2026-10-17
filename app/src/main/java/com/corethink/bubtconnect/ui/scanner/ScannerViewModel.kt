package com.corethink.bubtconnect.ui.scanner

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.corethink.bubtconnect.data.model.CommunityPost
import com.corethink.bubtconnect.data.model.CoverDetails
import com.corethink.bubtconnect.data.model.CoverTemplate
import com.corethink.bubtconnect.data.model.Course
import com.corethink.bubtconnect.data.model.PostCategory
import com.corethink.bubtconnect.data.model.ScanPage
import com.corethink.bubtconnect.data.model.StudentProfile
import com.corethink.bubtconnect.data.repository.CommunityRepository
import com.corethink.bubtconnect.data.repository.ProfileRepository
import com.corethink.bubtconnect.data.repository.SampleAcademicsRepository
import com.corethink.bubtconnect.data.repository.SampleCommunityRepository
import com.corethink.bubtconnect.data.repository.SettingsRepository
import com.corethink.bubtconnect.data.scan.DocumentPipeline
import com.corethink.bubtconnect.data.scan.ProcessedPage
import com.corethink.bubtconnect.data.scan.QuadStabilizer
import com.corethink.bubtconnect.data.scan.ScanOcr
import com.corethink.bubtconnect.data.scan.ScanPdfWriter
import org.fairscan.imageprocessing.ColorMode
import org.fairscan.imageprocessing.ImageSize
import org.fairscan.imageprocessing.Mode
import org.fairscan.imageprocessing.Quad
import org.fairscan.imageprocessing.detectDocumentQuad
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** 1o → 1p → 1q → 1r → 1s. Capture and Review are both "Step 3 of 4". */
enum class ScannerStep(val number: Int) { Template(1), Details(2), Capture(3), Review(3), Success(4) }

data class ScanResult(val file: File, val pageCount: Int, val sizeBytes: Long)

/** A stabilized document quad from live analysis, in rotated camera-frame coordinates. */
data class ScanDetection(val quad: Quad, val frameWidth: Double, val frameHeight: Double)

data class ScannerUiState(
    val step: ScannerStep = ScannerStep.Template,
    val template: CoverTemplate = CoverTemplate.Assignment,
    val details: CoverDetails = CoverDetails(),
    val submittedByIsAuto: Boolean = false,
    val pages: List<ScanPage> = emptyList(),
    val selectedPageId: Long? = null,
    val fileName: String = "",
    val searchablePdf: Boolean = true,
    val isMerging: Boolean = false,
    val isProcessing: Boolean = false,
    val processingPageId: Long? = null,
    val mergeError: String? = null,
    val result: ScanResult? = null,
    val publishToCommunity: Boolean = true,
    val tags: List<String> = emptyList(),
    val postAnonymously: Boolean = false
) {
    val canContinueDetails: Boolean
        get() = details.courseCode.isNotBlank() && (!template.hasTitle || details.title.isNotBlank())

    val canMerge: Boolean get() = !isMerging && (pages.isNotEmpty() || template.hasCover)

    val selectedPage: ScanPage? get() = pages.firstOrNull { it.id == selectedPageId }
}

class ScannerViewModel(application: Application) : AndroidViewModel(application) {

    private val profileRepository: ProfileRepository = ProfileRepository.Default
    private val communityRepository: CommunityRepository = SampleCommunityRepository
    private val courses: List<Course> = SampleAcademicsRepository.curriculum().map { it.course }
    private val profile: StudentProfile? = profileRepository.profile.value

    private val _uiState = MutableStateFlow(
        submittedByFromProfile(profile).let { auto ->
            ScannerUiState(
                // The Profile default (Default cover page) pre-selects the 1o tile.
                template = SettingsRepository.Default.settings.value.defaultCoverTemplate,
                details = CoverDetails(submittedBy = auto),
                submittedByIsAuto = auto.isNotEmpty()
            )
        }
    )
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    private var fileNameEdited = false
    private var nextPageId = 1L
    private var sourceCounter = 0L
    private val scanDir = File(application.cacheDir, "scans")
    private val ocr = ScanOcr(application)

    private val pipeline = DocumentPipeline(application)
    private var quadStabilizer = QuadStabilizer()
    private val _liveDetection = MutableStateFlow<ScanDetection?>(null)
    val liveDetection: StateFlow<ScanDetection?> = _liveDetection.asStateFlow()

    // ------------------------------------------------------------- navigation

    fun next() {
        val state = _uiState.value
        when (state.step) {
            ScannerStep.Template -> goTo(if (state.template.hasCover) ScannerStep.Details else ScannerStep.Capture)
            ScannerStep.Details -> if (state.canContinueDetails) goTo(ScannerStep.Capture)
            ScannerStep.Capture -> goTo(ScannerStep.Review)
            ScannerStep.Review -> merge()
            ScannerStep.Success -> Unit
        }
    }

    /** Returns false when the wizard should close instead (Back on the first or last step). */
    fun back(): Boolean {
        val state = _uiState.value
        val previous = when (state.step) {
            ScannerStep.Template, ScannerStep.Success -> return false
            ScannerStep.Details -> ScannerStep.Template
            ScannerStep.Capture -> if (state.template.hasCover) ScannerStep.Details else ScannerStep.Template
            ScannerStep.Review -> ScannerStep.Capture
        }
        goTo(previous)
        return true
    }

    fun editCover() = goTo(ScannerStep.Details)

    fun addMorePages() = goTo(ScannerStep.Capture)

    private fun goTo(step: ScannerStep) {
        if (step != ScannerStep.Capture) resetLiveDetection()
        _uiState.update { state ->
            val fileName = if (step == ScannerStep.Review && !fileNameEdited) {
                defaultFileName(state.template, state.details, profile?.name)
            } else state.fileName
            state.copy(step = step, fileName = fileName, mergeError = null)
        }
    }

    private fun resetLiveDetection() {
        quadStabilizer = QuadStabilizer()
        _liveDetection.value = null
    }

    // ------------------------------------------------------------- 1o · template

    fun selectTemplate(template: CoverTemplate) {
        _uiState.update { it.copy(template = template) }
    }

    // ------------------------------------------------------------- 1p · details

    fun onCourseCodeChange(value: String) {
        _uiState.update { state ->
            state.copy(details = state.details.copy(courseCode = value, courseTitle = lookupCourseTitle(value, courses)))
        }
    }

    fun updateDetails(transform: (CoverDetails) -> CoverDetails) {
        _uiState.update { it.copy(details = transform(it.details)) }
    }

    fun onSubmittedByChange(value: String) {
        _uiState.update { it.copy(details = it.details.copy(submittedBy = value), submittedByIsAuto = false) }
    }

    // ------------------------------------------------------------- 1q · capture

    /** Raw camera shot destination; the pipeline straightens it into [processedCaptureFile]. */
    fun newCaptureFile(): File =
        File(scanDir, "pages/${System.currentTimeMillis()}_${sourceCounter++}_src.jpg")
            .apply { parentFile?.mkdirs() }

    private fun processedCaptureFile(sourceFile: File): File =
        File(sourceFile.parentFile, sourceFile.name.removeSuffix("_src.jpg") + ".jpg")

    /** Feeds camera frames to the segmentation model; the stable quad drives the 1q overlay. */
    fun analyzeFrame(imageProxy: ImageProxy) {
        val state = _uiState.value
        if (state.step != ScannerStep.Capture || state.isProcessing) {
            imageProxy.close()
            return
        }
        viewModelScope.launch {
            try {
                val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                val bitmap = imageProxy.toBitmap()
                val frameSize = ImageSize(bitmap.width, bitmap.height)
                val result = pipeline.segmentFrame(bitmap)
                bitmap.recycle()
                _liveDetection.value = result?.let { detection ->
                    val mask = detection.segmentation
                    val maskSize = mask.maskSize()
                    val rawQuad = withContext(Dispatchers.Default) {
                        detectDocumentQuad(mask, frameSize, Mode.LIVE_ANALYSIS)
                            ?.rotate90(rotationDegrees / 90, maskSize)
                    }
                    val stableQuad = quadStabilizer.update(rawQuad) ?: return@let null
                    val frame =
                        if (rotationDegrees % 180 == 0) maskSize
                        else ImageSize(maskSize.height, maskSize.width)
                    ScanDetection(stableQuad, frame.width, frame.height)
                }
            } finally {
                imageProxy.close()
            }
        }
    }

    /** Runs the scan pipeline on a CameraX capture and appends the straightened page. */
    fun addCameraPage(sourceFile: File, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val processed = ingest(listOf(sourceFile to processedCaptureFile(sourceFile)), Mode.CAPTURE)
            onDone(processed.isNotEmpty())
        }
    }

    /** Imports gallery photos, running the same detection + warp + enhancement pipeline. */
    fun addPages(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            val sources = uris.mapNotNull { copyToScanFile(it) }.map { it to processedCaptureFile(it) }
            ingest(sources, Mode.IMPORT)
        }
    }

    /** Persists each processed page; returns the pages that made it. */
    private suspend fun ingest(sources: List<Pair<File, File>>, mode: Mode): List<ProcessedPage> {
        if (sources.isEmpty()) return emptyList()
        resetLiveDetection()
        _uiState.update { it.copy(isProcessing = true) }
        val processed = sources.mapNotNull { (source, target) ->
            runCatching { pipeline.process(source, target, mode) }
                .onFailure { Log.e("Scanner", "Page processing failed", it) }
                .getOrNull()
        }
        if (processed.isNotEmpty()) {
            _uiState.update { state ->
                state.copy(
                    pages = state.pages + processed.map { page ->
                        ScanPage(
                            id = nextPageId++,
                            uri = Uri.fromFile(page.file).toString(),
                            sourceUri = Uri.fromFile(page.sourceFile).toString(),
                            quad = page.normalizedQuad
                        )
                    }
                )
            }
        }
        _uiState.update { it.copy(isProcessing = false) }
        return processed
    }

    /** Copies a photo-picker content:// uri into the scan cache so the pipeline can decode it. */
    private fun copyToScanFile(uri: Uri): File? = runCatching {
        val target = File(scanDir, "pages/${System.currentTimeMillis()}_${sourceCounter++}_src.jpg")
        target.parentFile?.mkdirs()
        getApplication<Application>().contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(target).use { input.copyTo(it) }
        } ?: error("Couldn't open $uri")
        target
    }.onFailure {
        Log.e("Scanner", "Import copy failed", it)
    }.getOrNull()

    // ------------------------------------------------------------- 1r · review

    fun selectPage(id: Long) {
        _uiState.update { it.copy(selectedPageId = if (it.selectedPageId == id) null else id) }
    }

    fun rotateSelected() = editSelected { it.copy(rotation = (it.rotation + 90) % 360) }

    /** Re-warps the stored original with the corners the user dragged in the crop editor. */
    fun applyCrop(quad: Quad) {
        val page = _uiState.value.selectedPage ?: return
        editSelected { it.copy(quad = quad) }
        reprocessPage(page.id)
    }

    /** Auto / Color / Grayscale / B&W; null means back to the auto-detected mode. */
    fun setPageColorMode(mode: ColorMode?) {
        val page = _uiState.value.selectedPage ?: return
        if (page.colorMode == mode) return
        editSelected { it.copy(colorMode = mode) }
        reprocessPage(page.id)
    }

    private fun reprocessPage(pageId: Long) {
        val page = _uiState.value.pages.firstOrNull { it.id == pageId } ?: return
        val sourcePath = page.sourceUri?.let { Uri.parse(it).path } ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(processingPageId = pageId) }
            val processed = runCatching {
                val base = File(scanDir, "pages/${pageId}_v${System.currentTimeMillis()}")
                pipeline.reprocess(File(sourcePath), base, page.quad, page.colorMode)
            }.onFailure {
                Log.e("Scanner", "Page reprocessing failed", it)
            }.getOrNull()
            if (processed != null) {
                val newUri = Uri.fromFile(processed.file).toString()
                _uiState.update { state ->
                    state.copy(
                        pages = state.pages.map { if (it.id == pageId) it.copy(uri = newUri) else it }
                    )
                }
            }
            _uiState.update { it.copy(processingPageId = null) }
        }
    }

    fun deleteSelected() {
        _uiState.update { state ->
            state.copy(pages = state.pages.filterNot { it.id == state.selectedPageId }, selectedPageId = null)
        }
    }

    fun movePage(from: Int, to: Int) {
        _uiState.update { it.copy(pages = moveItem(it.pages, from, to)) }
    }

    fun onFileNameChange(value: String) {
        fileNameEdited = true
        _uiState.update { it.copy(fileName = value) }
    }

    /** Toggles the invisible OCR text layer on exported pages. */
    fun onSearchablePdfChange(value: Boolean) {
        _uiState.update { it.copy(searchablePdf = value) }
    }

    private fun editSelected(transform: (ScanPage) -> ScanPage) {
        _uiState.update { state ->
            state.copy(pages = state.pages.map { if (it.id == state.selectedPageId) transform(it) else it })
        }
    }

    private fun merge() {
        val state = _uiState.value
        if (!state.canMerge) return
        _uiState.update { it.copy(isMerging = true, mergeError = null) }
        viewModelScope.launch {
            val outcome = withContext(Dispatchers.IO) {
                runCatching {
                    val target = File(scanDir, sanitizeFileName(state.fileName))
                    val count = ScanPdfWriter.write(
                        getApplication(),
                        target,
                        state.template,
                        state.details,
                        state.pages,
                        ocr = if (state.searchablePdf) ocr else null
                    )
                    ScanResult(target, count, target.length())
                }
            }
            _uiState.update { current ->
                outcome.fold(
                    onSuccess = { result ->
                        current.copy(
                            isMerging = false,
                            result = result,
                            step = ScannerStep.Success,
                            tags = suggestedTags(current.details)
                        )
                    },
                    onFailure = { current.copy(isMerging = false, mergeError = "Couldn't create the PDF. Try again.") }
                )
            }
        }
    }

    // ------------------------------------------------------------- 1s · success

    /** Copies the PDF to a location the user picked through the system "Save as" sheet. */
    fun saveCopy(target: Uri, onDone: (Boolean) -> Unit) {
        val file = _uiState.value.result?.file ?: return onDone(false)
        viewModelScope.launch {
            val saved = withContext(Dispatchers.IO) {
                runCatching {
                    getApplication<Application>().contentResolver.openOutputStream(target)?.use { output ->
                        file.inputStream().use { it.copyTo(output) }
                    } != null
                }.getOrDefault(false)
            }
            onDone(saved)
        }
    }

    fun setPublishToCommunity(value: Boolean) {
        _uiState.update { it.copy(publishToCommunity = value) }
    }

    fun setPostAnonymously(value: Boolean) {
        _uiState.update { it.copy(postAnonymously = value) }
    }

    fun addTag(raw: String) {
        val tag = normalizeTag(raw) ?: return
        _uiState.update { state ->
            if (state.tags.any { it.equals(tag, ignoreCase = true) }) state else state.copy(tags = state.tags + tag)
        }
    }

    fun removeTag(tag: String) {
        _uiState.update { it.copy(tags = it.tags - tag) }
    }

    /** "Publish & Done" when publishing is on, plain "Done" otherwise. */
    fun finish() {
        val state = _uiState.value
        val result = state.result ?: return
        if (!state.publishToCommunity) return
        communityRepository.publish(
            CommunityPost(
                id = "local-${System.currentTimeMillis()}",
                title = state.details.title.ifBlank { result.file.nameWithoutExtension.replace('_', ' ') },
                author = if (state.postAnonymously) null else profile?.name ?: "You",
                department = departmentOf(state.details.courseCode) ?: profile?.department ?: "CSE",
                category = when (state.template) {
                    CoverTemplate.LabReport, CoverTemplate.IndexPage -> PostCategory.LabReport
                    CoverTemplate.InternshipReport -> PostCategory.Internship
                    else -> PostCategory.Notes
                },
                tags = state.tags.map { it.lowercase() },
                daysAgo = 0,
                views = 0,
                likes = 0
            )
        )
    }

    override fun onCleared() {
        super.onCleared()
        ocr.close()
    }

    companion object {
        fun submittedByFromProfile(profile: StudentProfile?): String = when {
            profile == null -> ""
            profile.name.isNullOrBlank() -> profile.roll
            else -> "${profile.name} · ${profile.roll}"
        }

        /** "cse231" and "CSE 231" both find "Data Structures". */
        fun lookupCourseTitle(code: String, courses: List<Course>): String {
            val needle = code.replace(" ", "")
            if (needle.isEmpty()) return ""
            return courses.firstOrNull { it.code.replace(" ", "").equals(needle, ignoreCase = true) }?.title.orEmpty()
        }

        /** "CSE231_Assignment_Rafi.pdf"; falls back to "Scan_2026-10-11.pdf" without a cover. */
        fun defaultFileName(template: CoverTemplate, details: CoverDetails, name: String?): String {
            if (!template.hasCover) {
                val date = details.date
                return String.format(Locale.US, "Scan_%04d-%02d-%02d.pdf", date.year, date.month, date.day)
            }
            val parts = listOf(
                details.courseCode.replace(" ", "").uppercase(),
                template.label.replace(" ", ""),
                name?.trim()?.substringBefore(' ').orEmpty()
            ).filter { it.isNotEmpty() }
            return parts.joinToString("_") + ".pdf"
        }

        fun sanitizeFileName(value: String): String {
            val base = value.trim().removeSuffix(".pdf").replace(Regex("[\\\\/:*?\"<>|]"), "_").ifBlank { "Scan" }
            return "$base.pdf"
        }

        /** #CSE231 and #DataStructures from the cover details. */
        fun suggestedTags(details: CoverDetails): List<String> = listOfNotNull(
            normalizeTag(details.courseCode),
            normalizeTag(details.courseTitle.split(Regex("\\s+")).joinToString("") { word ->
                word.replaceFirstChar { it.uppercase() }
            })
        ).distinct()

        /** Strips "#", spaces and punctuation; null when nothing usable is left. */
        fun normalizeTag(raw: String): String? =
            raw.trim().removePrefix("#").filter { it.isLetterOrDigit() }.ifEmpty { null }

        fun departmentOf(courseCode: String): String? =
            courseCode.trim().takeWhile { it.isLetter() }.uppercase().ifEmpty { null }

        fun <T> moveItem(items: List<T>, from: Int, to: Int): List<T> {
            if (from !in items.indices || from == to) return items
            val target = to.coerceIn(items.indices)
            return items.toMutableList().apply { add(target, removeAt(from)) }
        }
    }
}
