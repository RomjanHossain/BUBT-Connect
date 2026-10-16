package com.corethink.bubtconnect.data.scan

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.corethink.bubtconnect.R
import com.corethink.bubtconnect.data.model.CoverDetails
import com.corethink.bubtconnect.data.model.CoverTemplate
import com.corethink.bubtconnect.data.model.SimpleDate
import java.util.Calendar
import java.util.Locale

/**
 * Draws a cover onto an A4 canvas measured in PDF points (595 × 842).
 * The live preview (1p), the "Default cover page" gallery and the generated PDF all call
 * this, so what you see is what you get.
 *
 * Each template is laid out the way BUBT actually prints it — the same five documents
 * bubtcoverpage.vercel.app produces, each in its own format:
 *
 *  - Assignment   framed form: course rows, title, Submitted By / To cards, date footer
 *  - Lab Report   framed form: experiment date/no rows and a five-line student card
 *  - Project      BUBT letterhead, centred title, supervisor block, team-member table
 *  - Index page   letterhead, underlined identity fields, 16-row experiment table
 *  - Internship   unframed, everything centred around supervised-by / submitted-by blocks
 */
object CoverRenderer {
    const val PAGE_WIDTH = 595
    const val PAGE_HEIGHT = 842

    private const val MARGIN = 56f
    private const val COLON = 120f
    private const val UNIVERSITY = "Bangladesh University of Business and Technology"
    private const val UNIVERSITY_BUBT = "Bangladesh University of Business and Technology (BUBT)"
    private const val DEPT_LINE_1 = "Dept. of"
    private const val DEPT_LINE_2 = "Bangladesh University of Business"
    private const val DEPT_LINE_3 = "& Technology"

    private val ink = Color.rgb(0x1C, 0x1B, 0x1F)
    private val blue = Color.rgb(0x1D, 0x4E, 0xD8)

    private val bitmapPaint = Paint(Paint.FILTER_BITMAP_FLAG)
    private val boxWidth = (PAGE_WIDTH - 2 * MARGIN - 18f) / 2

    /** The crest is the launcher foreground, whose artwork sits in the middle ~66% (adaptive-icon safe zone). */
    fun loadLogo(context: Context): Bitmap? =
        BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher_foreground)

    fun draw(canvas: Canvas, template: CoverTemplate, details: CoverDetails, logo: Bitmap? = null) {
        canvas.drawColor(Color.WHITE)
        if (!template.hasCover) return
        when (template) {
            CoverTemplate.Assignment -> assignment(canvas, details, logo)
            CoverTemplate.LabReport -> labReport(canvas, details, logo)
            CoverTemplate.ProjectReport -> projectReport(canvas, details, logo)
            CoverTemplate.IndexPage -> indexPage(canvas, details, logo)
            CoverTemplate.InternshipReport -> internshipReport(canvas, details, logo)
            CoverTemplate.NoCover -> Unit
        }
    }

    // ------------------------------------------------------------- assignment (framed form)

    private fun assignment(canvas: Canvas, details: CoverDetails, logo: Bitmap?) {
        frame(canvas)
        headerCentered(canvas, logo)
        badge(canvas, "ASSIGNMENT")

        val labelPaint = text(13f, ink, bold = true)
        val valuePaint = text(13f, ink)
        var y = 372f
        y = formRow(canvas, "Course Code", details.courseCode, 76f, y, labelPaint, valuePaint)
        formRow(canvas, "Course Title", details.courseTitle, 76f, y, labelPaint, valuePaint)

        drawTitle(canvas, details, baseline = 452f, size = 18f)
        peopleBox(canvas, "Submitted By:", details.submittedBy, MARGIN, 552f, boxWidth, 172f, dept = false)
        peopleBox(canvas, "Submitted To:", details.submittedTo, MARGIN + boxWidth + 18f, 552f, boxWidth, 172f, dept = true)
        dateFooter(canvas, details)
    }

    // ------------------------------------------------------------- lab report (experiment form)

    private fun labReport(canvas: Canvas, details: CoverDetails, logo: Bitmap?) {
        frame(canvas)
        headerCentered(canvas, logo)
        badge(canvas, "LAB REPORT")

        val labelPaint = text(13f, ink, bold = true)
        val valuePaint = text(13f, ink)
        var y = 350f
        y = formRow(canvas, "Experiment Date", longDate(details.date), 76f, y, labelPaint, valuePaint)
        y = formRow(canvas, "Experiment No", "", 76f, y, labelPaint, valuePaint)
        y = formRow(canvas, "Course Title", details.courseTitle, 76f, y, labelPaint, valuePaint)
        y = formRow(canvas, "Course Code", details.courseCode, 76f, y, labelPaint, valuePaint)
        formRow(canvas, "Experiment Name", details.title, 76f, y, labelPaint, valuePaint)

        peopleBox(
            canvas, "Submitted By:", details.submittedBy, MARGIN, 516f, boxWidth, 210f,
            dept = false, trailingLabels = listOf("Intake", "Section", "Program")
        )
        peopleBox(canvas, "Submitted To:", details.submittedTo, MARGIN + boxWidth + 18f, 516f, boxWidth, 210f, dept = true)
        dateFooter(canvas, details)
    }

    // ------------------------------------------------------------- project report (letterhead + team table)

    private fun projectReport(canvas: Canvas, details: CoverDetails, logo: Bitmap?) {
        wordmarkHeader(canvas, logo)
        badge(canvas, "PROJECT REPORT", centerY = 190f)

        drawTitle(canvas, details, baseline = 258f, size = 15f, lines = 2, maxWidth = PAGE_WIDTH - 140f)
        val coursePaint = text(11f, ink)
        if (details.courseTitle.isNotBlank()) {
            drawCentered(canvas, "Course Title : ${details.courseTitle}", 322f, coursePaint)
        }
        if (details.courseCode.isNotBlank()) {
            drawCentered(canvas, "Course Code : ${details.courseCode}", 340f, coursePaint)
        }

        sectionHeading(canvas, "Submitted to :", 384f)
        val parts = splitPeople(details.submittedTo)
        if (parts.isNotEmpty()) drawCentered(canvas, parts[0], 416f, text(12f, ink, bold = true))
        val role = parts.drop(1).joinToString(", ")
        if (role.isNotBlank()) drawCentered(canvas, role, 436f, text(10f, ink, italic = true))
        deptBlock(canvas, baseline = 462f, size = 10f, italic = true)

        sectionHeading(canvas, "Submitted by :", 544f)
        teamTable(canvas, details)

        drawCentered(canvas, "$UNIVERSITY (BUBT)", 766f, text(9.5f, ink, bold = true))
        dateFooter(canvas, details, baseline = 786f, size = 9.5f)
    }

    /** Project cover: seven numbered rows, the first one filled from "Submitted by". */
    private fun teamTable(canvas: Canvas, details: CoverDetails) {
        val left = MARGIN
        val right = PAGE_WIDTH - MARGIN
        val top = 568f
        val rowHeight = 20f
        val rows = 8
        val fractions = listOf(0.12f, 0.53f, 0.35f)
        grid(canvas, left, top, right, rowHeight, rows, fractions)

        val headerPaint = text(10f, ink, bold = true)
        listOf("Sl No", "Name", "ID").forEachIndexed { index, label ->
            val width = (right - left) * fractions[index]
            val columnLeft = left + (right - left) * fractions.take(index).sum()
            cell(canvas, label, columnLeft, width, top, rowHeight, headerPaint, center = index > 0)
        }

        val bodyPaint = text(10f, ink)
        val parts = splitPeople(details.submittedBy)
        for (row in 1 until rows) {
            val rowTop = top + rowHeight * row
            val numberLeft = left
            val numberWidth = (right - left) * fractions[0]
            cell(canvas, "$row", numberLeft, numberWidth, rowTop, rowHeight, bodyPaint, center = false)
            if (row == 1 && parts.isNotEmpty()) {
                val nameLeft = numberLeft + numberWidth
                val nameWidth = (right - left) * fractions[1]
                cell(canvas, parts[0], nameLeft, nameWidth, rowTop, rowHeight, bodyPaint, center = false, inset = 8f)
                if (parts.size > 1) {
                    val idLeft = nameLeft + nameWidth
                    cell(canvas, parts[1], idLeft, (right - left) * fractions[2], rowTop, rowHeight, bodyPaint, center = false, inset = 8f)
                }
            }
        }
    }

    // ------------------------------------------------------------- index page (underlined fields + table)

    private fun indexPage(canvas: Canvas, details: CoverDetails, logo: Bitmap?) {
        wordmarkHeader(canvas, logo)
        drawCentered(canvas, "INDEX", 176f, text(16f, ink, bold = true))

        val labelPaint = text(11f, ink, bold = true)
        val parts = splitPeople(details.submittedBy)
        field(canvas, "Name :", parts.getOrElse(0) { "" }, 76f, 122f, 292f, 222f, labelPaint)
        field(canvas, "ID No :", parts.getOrElse(1) { "" }, 312f, 360f, 519f, 222f, labelPaint)
        field(canvas, "Intake :", "", 76f, 126f, 292f, 252f, labelPaint)
        field(canvas, "Section :", "", 312f, 370f, 519f, 252f, labelPaint)
        field(canvas, "Course Title :", details.courseTitle, 76f, 162f, 519f, 282f, labelPaint)

        val left = MARGIN
        val right = PAGE_WIDTH - MARGIN
        val top = 312f
        val rowHeight = 26f
        val rows = 17
        val fractions = listOf(0.07f, 0.15f, 0.46f, 0.12f, 0.2f)
        grid(canvas, left, top, right, rowHeight, rows, fractions)

        val headerPaint = text(10f, ink, bold = true)
        val columns = listOf("Sl.", "Date", "Title of Experiment / Topic", "Page", "Remarks")
        val centered = listOf(false, true, false, true, true)
        var x = left
        columns.forEachIndexed { index, label ->
            val width = (right - left) * fractions[index]
            cell(canvas, label, x, width, top, rowHeight, headerPaint, center = centered[index], inset = 6f)
            x += width
        }

        val bodyPaint = text(9f, ink)
        for (row in 1 until rows) {
            val rowTop = top + rowHeight * row
            cell(canvas, "$row", left, (right - left) * fractions[0], rowTop, rowHeight, bodyPaint, center = false, inset = 6f)
        }
    }

    /** One "Label : ____" line; the rule keeps the blank form readable when a value is empty. */
    private fun field(
        canvas: Canvas,
        label: String,
        value: String,
        labelX: Float,
        valueX: Float,
        ruleTo: Float,
        baseline: Float,
        labelPaint: Paint
    ) {
        canvas.drawText(label, labelX, baseline, labelPaint)
        if (value.isNotBlank()) {
            val paint = fit(value, ruleTo - valueX, 11f, smallest = 8f)
            canvas.drawText(value, valueX, baseline, paint)
        }
        canvas.drawLine(valueX - 4f, baseline + 5f, ruleTo, baseline + 5f, stroke(ink, 1f))
    }

    // ------------------------------------------------------------- internship report (all centred, no frame)

    private fun internshipReport(canvas: Canvas, details: CoverDetails, logo: Bitmap?) {
        headerCentered(canvas, logo, name = UNIVERSITY_BUBT, nameSize = 14f)

        drawCentered(canvas, "Internship Report", 276f, text(15f, ink, bold = true))
        drawCentered(canvas, "on", 300f, text(12f, ink))
        drawTitle(canvas, details, baseline = 332f, size = 13f, lines = 2, maxWidth = PAGE_WIDTH - 150f)

        sectionHeading(canvas, "Supervised By", 400f)
        val mentor = splitPeople(details.submittedTo)
        if (mentor.isNotEmpty()) drawCentered(canvas, mentor[0], 432f, text(12f, ink, bold = true))
        val role = mentor.drop(1).joinToString(", ")
        if (role.isNotBlank()) drawCentered(canvas, role, 452f, text(10.5f, ink, italic = true))
        deptBlock(canvas, baseline = 480f, size = 10.5f, italic = false)

        sectionHeading(canvas, "Submitted By", 574f)
        val student = splitPeople(details.submittedBy)
        if (student.isNotEmpty()) drawCentered(canvas, student[0], 606f, text(12f, ink, bold = true))
        val id = student.getOrElse(1) { "" }
        drawCentered(canvas, if (id.isBlank()) "ID :" else "ID : $id", 628f, text(10.5f, ink))
        deptBlock(canvas, baseline = 664f, size = 10.5f, italic = false)

        dateFooter(canvas, details, baseline = 784f, size = 11.5f, bold = true)
    }

    // ------------------------------------------------------------- shared furniture

    private fun frame(canvas: Canvas) {
        canvas.drawRect(22f, 22f, PAGE_WIDTH - 22f, PAGE_HEIGHT - 22f, stroke(ink, 2f))
        canvas.drawRect(34f, 34f, PAGE_WIDTH - 34f, PAGE_HEIGHT - 34f, stroke(ink, 1f))
    }

    /** Framed covers: university name, then the crest below it. */
    private fun headerCentered(canvas: Canvas, logo: Bitmap?, name: String = UNIVERSITY, nameSize: Float = 16f) {
        drawCentered(canvas, name, 84f, fit(name, PAGE_WIDTH - 2 * MARGIN, nameSize, bold = true))
        crest(canvas, logo, PAGE_WIDTH / 2f, 174f, 54f)
    }

    /** Letterhead used by the project and index covers: crest left, BUBT wordmark beside it. */
    private fun wordmarkHeader(canvas: Canvas, logo: Bitmap?) {
        crest(canvas, logo, 90f, 88f, 34f)
        canvas.drawText("BUBT", 134f, 84f, text(26f, blue, bold = true))
        canvas.drawText("Commitment to Academic Excellence", 134f, 100f, text(8.5f, ink, italic = true))
        canvas.drawText(UNIVERSITY, 134f, 118f, text(9.5f, ink, bold = true))
    }

    private fun crest(canvas: Canvas, logo: Bitmap?, centerX: Float, centerY: Float, half: Float) {
        if (logo != null) {
            canvas.drawBitmap(logo, null, RectF(centerX - half, centerY - half, centerX + half, centerY + half), bitmapPaint)
        } else {
            canvas.drawCircle(centerX, centerY, half * 0.74f, stroke(ink, 1.5f))
            val paint = text(half * 0.32f, ink, bold = true)
            canvas.drawText("BUBT", centerX - paint.measureText("BUBT") / 2, centerY + half * 0.11f, paint)
        }
    }

    /** Document type in a rounded box, as on the printed BUBT cover. */
    private fun badge(canvas: Canvas, heading: String, centerY: Float = 274f) {
        val label = text(20f, ink, bold = true).apply { letterSpacing = 0.06f }
        val width = label.measureText(heading) + 56f
        val cx = PAGE_WIDTH / 2f
        canvas.drawRoundRect(
            RectF(cx - width / 2, centerY - 23f, cx + width / 2, centerY + 23f),
            10f,
            10f,
            stroke(ink, 1.5f)
        )
        drawCentered(canvas, heading, centerY + 7f, label)
    }

    /** Centred heading with a rule under it — "Submitted to :", "Supervised By". */
    private fun sectionHeading(canvas: Canvas, value: String, baseline: Float) {
        val paint = text(13f, ink, bold = true)
        val width = paint.measureText(value)
        val x = (PAGE_WIDTH - width) / 2
        canvas.drawText(value, x, baseline, paint)
        canvas.drawLine(x, baseline + 5f, x + width, baseline + 5f, stroke(ink, 1f))
    }

    /**
     * "Course Code : CSE 231" — label, colon in a fixed column, then the value.
     * Returns the baseline for the next row so a wrapped value pushes it down.
     */
    private fun formRow(
        canvas: Canvas,
        label: String,
        value: String,
        left: Float,
        baseline: Float,
        labelPaint: Paint,
        valuePaint: Paint
    ): Float {
        canvas.drawText(label, left, baseline, labelPaint)
        canvas.drawText(":", left + COLON, baseline, labelPaint)
        val valueX = left + COLON + 16f
        val lines = if (value.isBlank()) emptyList() else wrap(value, PAGE_WIDTH - MARGIN - valueX, valuePaint).take(2)
        lines.forEachIndexed { index, line -> canvas.drawText(line, valueX, baseline + index * 17f, valuePaint) }
        return baseline + maxOf(24f, lines.size * 17f + 7f)
    }

    /**
     * The bordered Submitted By / Submitted To card. The heading is centred *inside the card*
     * (not the page — that was the bug that parked it in the gap between the two cards).
     */
    private fun peopleBox(
        canvas: Canvas,
        heading: String,
        people: String,
        left: Float,
        top: Float,
        width: Float,
        height: Float,
        dept: Boolean,
        trailingLabels: List<String> = emptyList()
    ) {
        canvas.drawRoundRect(RectF(left, top, left + width, top + height), 14f, 14f, stroke(ink, 1.5f))
        val headingPaint = text(14f, ink, bold = true)
        canvas.drawText(
            heading,
            left + (width - headingPaint.measureText(heading)) / 2,
            top + 28f,
            headingPaint
        )

        // "Name · ID" for the student; the teacher string stays on one Name line.
        val parts = splitPeople(people)
        val rows = if (dept) {
            listOf("Name" to people.trim())
        } else {
            listOf("Name" to parts.getOrElse(0) { "" }, "ID No" to parts.getOrElse(1) { "" }) +
                trailingLabels.map { it to "" }
        }

        val labelPaint = text(12f, ink, bold = true)
        val valuePaint = text(12f, ink)
        val x = left + 16f
        val valueX = left + 74f

        var y = top + 64f
        rows.forEach { (label, value) ->
            canvas.drawText("$label :", x, y, labelPaint)
            val lines = if (value.isBlank()) emptyList() else wrap(value, width - 88f, valuePaint).take(3)
            lines.forEachIndexed { index, line -> canvas.drawText(line, valueX, y + index * 16f, valuePaint) }
            y += maxOf(24f, lines.size * 16f + 8f)
        }

        if (dept) deptBlock(canvas, baseline = top + height - 46f, size = 10f, bold = true, leftX = left + 16f)
    }

    private fun deptBlock(
        canvas: Canvas,
        baseline: Float,
        size: Float,
        bold: Boolean = false,
        italic: Boolean = false,
        leftX: Float? = null
    ) {
        val paint = text(size, ink, bold = bold, italic = italic)
        listOf(DEPT_LINE_1, DEPT_LINE_2, DEPT_LINE_3).forEachIndexed { index, line ->
            val x = leftX ?: (PAGE_WIDTH - paint.measureText(line)) / 2
            canvas.drawText(line, x, baseline + index * (size + 4f), paint)
        }
    }

    private fun drawTitle(
        canvas: Canvas,
        details: CoverDetails,
        baseline: Float,
        size: Float,
        lines: Int = 3,
        maxWidth: Float = PAGE_WIDTH - 144f
    ) {
        if (details.title.isBlank()) return
        val paint = text(size, ink, bold = true)
        var y = baseline
        wrap(details.title, maxWidth, paint).take(lines).forEach { line ->
            drawCentered(canvas, line, y, paint)
            y += size + 6f
        }
    }

    private fun dateFooter(
        canvas: Canvas,
        details: CoverDetails,
        baseline: Float = 784f,
        size: Float = 13f,
        bold: Boolean = false
    ) {
        drawCentered(canvas, "Date of Submission : ${longDate(details.date)}", baseline, text(size, ink, bold = bold))
    }

    /** Table rules only; callers place the labels and cells. Returns the table bottom. */
    private fun grid(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        rowHeight: Float,
        rows: Int,
        fractions: List<Float>
    ): Float {
        val line = stroke(ink, 1f)
        val bottom = top + rowHeight * rows
        canvas.drawRect(left, top, right, bottom, line)
        for (row in 1 until rows) {
            val y = top + rowHeight * row
            canvas.drawLine(left, y, right, y, line)
        }
        var x = left
        fractions.dropLast(1).forEach { fraction ->
            x += (right - left) * fraction
            canvas.drawLine(x, top, x, bottom, line)
        }
        return bottom
    }

    private fun cell(
        canvas: Canvas,
        value: String,
        columnLeft: Float,
        columnWidth: Float,
        rowTop: Float,
        rowHeight: Float,
        paint: Paint,
        center: Boolean,
        inset: Float = 6f
    ) {
        val baseline = rowTop + rowHeight / 2 + paint.textSize * 0.35f
        if (center) {
            canvas.drawText(value, columnLeft + (columnWidth - paint.measureText(value)) / 2, baseline, paint)
        } else {
            canvas.drawText(value, columnLeft + inset, baseline, paint)
        }
    }

    private fun splitPeople(value: String): List<String> =
        value.split(" · ", ",", "\n").map(String::trim).filter(String::isNotBlank)

    /** Shrinks the size until [value] fits [maxWidth]; used wherever text must not cross a rule. */
    private fun fit(value: String, maxWidth: Float, size: Float, bold: Boolean = false, smallest: Float = size * 0.78f): Paint {
        var current = size
        while (current > smallest) {
            val paint = text(current, ink, bold)
            if (paint.measureText(value) <= maxWidth) return paint
            current -= 0.5f
        }
        return text(smallest, ink, bold)
    }

    private fun drawCentered(canvas: Canvas, value: String, baseline: Float, paint: Paint) {
        canvas.drawText(value, (PAGE_WIDTH - paint.measureText(value)) / 2, baseline, paint)
    }

    /** Greedy word wrap; a single over-long word stays on its own line. */
    internal fun wrap(value: String, maxWidth: Float, paint: Paint): List<String> {
        val lines = mutableListOf<String>()
        var current = ""
        value.trim().split(Regex("\\s+")).forEach { word ->
            val candidate = if (current.isEmpty()) word else "$current $word"
            if (current.isNotEmpty() && paint.measureText(candidate) > maxWidth) {
                lines += current
                current = word
            } else {
                current = candidate
            }
        }
        if (current.isNotEmpty()) lines += current
        return lines
    }

    /** SimpleDate(2026, 10, 11) → "11 October 2026" */
    fun longDate(date: SimpleDate): String {
        val calendar = Calendar.getInstance().apply {
            clear()
            set(date.year, date.month - 1, date.day)
        }
        val month = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US).orEmpty()
        return "${date.day} $month ${date.year}"
    }

    private fun text(size: Float, color: Int, bold: Boolean = false, italic: Boolean = false) =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size
            this.color = color
            typeface = when {
                bold && italic -> Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
                bold -> Typeface.DEFAULT_BOLD
                italic -> Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                else -> Typeface.DEFAULT
            }
        }

    private fun stroke(color: Int, width: Float) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = width
        this.color = color
    }
}
