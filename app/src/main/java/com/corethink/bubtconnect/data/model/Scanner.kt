package com.corethink.bubtconnect.data.model

import org.fairscan.imageprocessing.ColorMode
import org.fairscan.imageprocessing.Quad

/** Cover templates offered on 1m. [heading] is the big title printed on the cover. */
enum class CoverTemplate(val label: String, val heading: String) {
    Assignment("Assignment", "ASSIGNMENT"),
    LabReport("Lab Report", "LAB REPORT"),
    ProjectReport("Project Report", "PROJECT REPORT"),
    IndexPage("Index Page", "INDEX"),
    InternshipReport("Internship Report", "INTERNSHIP REPORT"),
    NoCover("No cover", "");

    val hasCover: Boolean get() = this != NoCover

    /** The index page is a table, so it has no assignment title. */
    val hasTitle: Boolean get() = this != IndexPage && this != NoCover
}

/** What the student types on 1n; printed onto the cover. */
data class CoverDetails(
    val courseCode: String = "",
    val courseTitle: String = "",
    val date: SimpleDate = SimpleDate.today(),
    val title: String = "",
    val submittedTo: String = "",
    val submittedBy: String = ""
)

/**
 * One scanned or imported page. [uri] is the straightened, enhanced page image; [sourceUri]
 * keeps the raw photo (from the camera or gallery) so Review can re-crop from the original.
 * [quad] is the page outline in the source photo (normalized 0..1), [colorMode] the
 * user-chosen color mode (null = auto-detected at capture).
 */
data class ScanPage(
    val id: Long,
    val uri: String,
    val rotation: Int = 0,
    val grayscale: Boolean = false,
    val sourceUri: String? = null,
    val quad: Quad? = null,
    val colorMode: ColorMode? = null
)
