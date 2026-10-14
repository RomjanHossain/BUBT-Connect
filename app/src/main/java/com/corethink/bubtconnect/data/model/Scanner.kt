package com.corethink.bubtconnect.data.model

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

/** One scanned or imported page. [uri] is a file:// capture or a photo-picker content:// uri. */
data class ScanPage(
    val id: Long,
    val uri: String,
    val rotation: Int = 0,
    val grayscale: Boolean = false
)
