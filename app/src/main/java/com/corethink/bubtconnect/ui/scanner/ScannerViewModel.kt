package com.corethink.bubtconnect.ui.scanner

import android.app.Application
import android.net.Uri
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
import com.corethink.bubtconnect.data.scan.ScanPdfWriter
import java.io.File
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

data class ScannerUiState(
    val step: ScannerStep = ScannerStep.Template,
    val template: CoverTemplate = CoverTemplate.Assignment,
    val details: CoverDetails = CoverDetails(),
    val submittedByIsAuto: Boolean = false,
    val pages: List<ScanPage> = emptyList(),
    val selectedPageId: Long? = null,
    val fileName: String = "",
    val isMerging: Boolean = false,
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
            ScannerUiState(details = CoverDetails(submittedBy = auto), submittedByIsAuto = auto.isNotEmpty())
        }
    )
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    private var fileNameEdited = false
    private var nextPageId = 1L
    private val scanDir = File(application.cacheDir, "scans")

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
        _uiState.update { state ->
            val fileName = if (step == ScannerStep.Review && !fileNameEdited) {
                defaultFileName(state.template, state.details, profile?.name)
            } else state.fileName
            state.copy(step = step, fileName = fileName, mergeError = null)
        }
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

    /** Destination for the next camera shot; added as a page once CameraX has written it. */
    fun newCaptureFile(): File = File(scanDir, "pages/${System.currentTimeMillis()}.jpg").apply { parentFile?.mkdirs() }

    fun addPages(uris: List<Uri>) {
        if (uris.isEmpty()) return
        _uiState.update { state ->
            state.copy(pages = state.pages + uris.map { ScanPage(id = nextPageId++, uri = it.toString()) })
        }
    }

    // ------------------------------------------------------------- 1r · review

    fun selectPage(id: Long) {
        _uiState.update { it.copy(selectedPageId = if (it.selectedPageId == id) null else id) }
    }

    fun rotateSelected() = editSelected { it.copy(rotation = (it.rotation + 90) % 360) }

    fun toggleFilterSelected() = editSelected { it.copy(grayscale = !it.grayscale) }

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
                    val count = ScanPdfWriter.write(getApplication(), target, state.template, state.details, state.pages)
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
