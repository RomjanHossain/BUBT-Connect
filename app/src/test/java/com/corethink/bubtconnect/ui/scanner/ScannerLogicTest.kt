package com.corethink.bubtconnect.ui.scanner

import com.corethink.bubtconnect.data.model.CoverDetails
import com.corethink.bubtconnect.data.model.CoverTemplate
import com.corethink.bubtconnect.data.model.Course
import com.corethink.bubtconnect.data.model.SimpleDate
import com.corethink.bubtconnect.data.model.StudentProfile
import com.corethink.bubtconnect.ui.scanner.ScannerViewModel.Companion.defaultFileName
import com.corethink.bubtconnect.ui.scanner.ScannerViewModel.Companion.lookupCourseTitle
import com.corethink.bubtconnect.ui.scanner.ScannerViewModel.Companion.moveItem
import com.corethink.bubtconnect.ui.scanner.ScannerViewModel.Companion.normalizeTag
import com.corethink.bubtconnect.ui.scanner.ScannerViewModel.Companion.sanitizeFileName
import com.corethink.bubtconnect.ui.scanner.ScannerViewModel.Companion.submittedByFromProfile
import com.corethink.bubtconnect.ui.scanner.ScannerViewModel.Companion.suggestedTags
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScannerLogicTest {

    private val details = CoverDetails(
        courseCode = "CSE 231",
        courseTitle = "Data Structures",
        date = SimpleDate(2026, 10, 11),
        title = "Linked Lists"
    )

    @Test
    fun fileNameUsesCourseTemplateAndFirstName() {
        assertEquals("CSE231_Assignment_Rafi.pdf", defaultFileName(CoverTemplate.Assignment, details, "Rafi Ahmed"))
        assertEquals("CSE231_LabReport.pdf", defaultFileName(CoverTemplate.LabReport, details, null))
        assertEquals("Scan_2026-10-11.pdf", defaultFileName(CoverTemplate.NoCover, details, "Rafi Ahmed"))
    }

    @Test
    fun sanitizeKeepsOnePdfExtensionAndDropsIllegalCharacters() {
        assertEquals("a_b.pdf", sanitizeFileName("a/b.pdf"))
        assertEquals("Report.pdf", sanitizeFileName(" Report "))
        assertEquals("Scan.pdf", sanitizeFileName("  "))
    }

    @Test
    fun courseTitleLookupIgnoresSpacesAndCase() {
        val courses = listOf(Course("CSE 231", "Data Structures"))
        assertEquals("Data Structures", lookupCourseTitle("cse231", courses))
        assertEquals("", lookupCourseTitle("MAT 101", courses))
    }

    @Test
    fun tagsComeFromCourseCodeAndTitle() {
        assertEquals(listOf("CSE231", "DataStructures"), suggestedTags(details))
        assertEquals("lab", normalizeTag("#lab"))
        assertNull(normalizeTag(" # "))
    }

    @Test
    fun submittedByFallsBackToRoll() {
        assertEquals("Rafi Ahmed · 222", submittedByFromProfile(StudentProfile("222", "CSE", "5th", "Rafi Ahmed")))
        assertEquals("222", submittedByFromProfile(StudentProfile("222", "CSE", "5th")))
        assertEquals("", submittedByFromProfile(null))
    }

    @Test
    fun moveItemClampsTarget() {
        assertEquals(listOf("b", "c", "a"), moveItem(listOf("a", "b", "c"), 0, 2))
        assertEquals(listOf("c", "a", "b"), moveItem(listOf("a", "b", "c"), 2, -5))
        assertEquals(listOf("a", "b", "c"), moveItem(listOf("a", "b", "c"), 1, 1))
    }

    @Test
    fun indexPageNeedsNoTitleToContinue() {
        val noTitle = details.copy(title = "")
        assertTrue(ScannerUiState(template = CoverTemplate.IndexPage, details = noTitle).canContinueDetails)
        assertFalse(ScannerUiState(template = CoverTemplate.Assignment, details = noTitle).canContinueDetails)
    }

    @Test
    fun fileSizeFormatting() {
        assertEquals("1.4 MB", formatFileSize(1_468_006))
        assertEquals("84 KB", formatFileSize(86_000))
    }
}
