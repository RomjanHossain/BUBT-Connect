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
 * The live preview (1p) and the generated PDF both call this, so what you see is what you get.
 *
 * Layout follows the BUBT standard cover (the shape bubtcoverpage.vercel.app produces):
 * double frame → crest and university name → document-type badge → course rows →
 * bordered Submitted By / Submitted To cards → "Date of Submission" footer.
 */
object CoverRenderer {
    const val PAGE_WIDTH = 595
    const val PAGE_HEIGHT = 842

    private const val FRAME = 22f
    private const val FRAME_INNER = 34f
    private const val MARGIN = 56f
    private const val UNIVERSITY = "Bangladesh University of Business and Technology"

    private val ink = Color.rgb(0x1C, 0x1B, 0x1F)
    private val muted = Color.rgb(0x5F, 0x5E, 0x66)

    /** The crest is the launcher foreground, whose artwork sits in the middle ~66% (adaptive-icon safe zone). */
    fun loadLogo(context: Context): Bitmap? =
        BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher_foreground)

    fun draw(canvas: Canvas, template: CoverTemplate, details: CoverDetails, logo: Bitmap? = null) {
        canvas.drawColor(Color.WHITE)
        if (!template.hasCover) return

        canvas.drawRect(FRAME, FRAME, PAGE_WIDTH - FRAME, PAGE_HEIGHT - FRAME, stroke(ink, 2f))
        canvas.drawRect(FRAME_INNER, FRAME_INNER, PAGE_WIDTH - FRAME_INNER, PAGE_HEIGHT - FRAME_INNER, stroke(ink, 1f))

        val cx = PAGE_WIDTH / 2f
        drawCentered(canvas, UNIVERSITY, 84f, fit(UNIVERSITY, PAGE_WIDTH - 2 * MARGIN, 16f))
        if (logo != null) {
            val half = 54f
            canvas.drawBitmap(logo, null, RectF(cx - half, 174f - half, cx + half, 174f + half), Paint(Paint.FILTER_BITMAP_FLAG))
        } else {
            canvas.drawCircle(cx, 174f, 40f, stroke(ink, 1.5f))
            drawCentered(canvas, "BUBT", 180f, text(16f, ink, bold = true))
        }
        drawHeadingBadge(canvas, template.heading)

        if (template == CoverTemplate.IndexPage) drawIndex(canvas, details) else drawCover(canvas, details)
    }

    /** The document type sits in a rounded box, as on the printed BUBT cover. */
    private fun drawHeadingBadge(canvas: Canvas, heading: String) {
        val label = text(20f, ink, bold = true).apply { letterSpacing = 0.06f }
        val width = label.measureText(heading) + 56f
        val height = 46f
        val centerY = 274f
        val cx = PAGE_WIDTH / 2f
        canvas.drawRoundRect(
            RectF(cx - width / 2, centerY - height / 2, cx + width / 2, centerY + height / 2),
            10f,
            10f,
            stroke(ink, 1.5f)
        )
        drawCentered(canvas, heading, centerY + 7f, label)
    }

    private fun drawCover(canvas: Canvas, details: CoverDetails) {
        val labelPaint = text(13f, ink, bold = true)
        val valuePaint = text(13f, ink)

        var y = 372f
        y = formRow(canvas, "Course Code", details.courseCode, 76f, 148f, y, labelPaint, valuePaint)
        formRow(canvas, "Course Title", details.courseTitle, 76f, 148f, y, labelPaint, valuePaint)

        if (details.title.isNotBlank()) {
            val titlePaint = text(18f, ink, bold = true)
            var titleY = 452f
            wrap(details.title, PAGE_WIDTH - 144f, titlePaint).take(3).forEach { line ->
                drawCentered(canvas, line, titleY, titlePaint)
                titleY += 24f
            }
        }

        // Two bordered cards, left blank below the filled lines like the printed form.
        val boxTop = 552f
        val boxHeight = 172f
        val boxGap = 18f
        val boxWidth = (PAGE_WIDTH - 2 * MARGIN - boxGap) / 2
        drawPeopleBox(
            canvas = canvas,
            heading = "Submitted By:",
            people = details.submittedBy,
            left = MARGIN,
            top = boxTop,
            width = boxWidth,
            height = boxHeight,
            dept = false
        )
        drawPeopleBox(
            canvas = canvas,
            heading = "Submitted To:",
            people = details.submittedTo,
            left = MARGIN + boxWidth + boxGap,
            top = boxTop,
            width = boxWidth,
            height = boxHeight,
            dept = true
        )

        drawCentered(canvas, "Date of Submission : ${longDate(details.date)}", 784f, text(13f, ink))
    }

    /** Index page: who it belongs to, then an empty table to fill in by hand. */
    private fun drawIndex(canvas: Canvas, details: CoverDetails) {
        courseLine(details)?.let { drawCentered(canvas, it, 344f, text(13f, muted)) }
        if (details.submittedBy.isNotBlank()) {
            drawCentered(canvas, details.submittedBy, 366f, text(12f, muted))
        }

        val left = MARGIN
        val right = PAGE_WIDTH - MARGIN
        val top = 396f
        val rowHeight = 24f
        val rows = 14
        val columns = listOf("No." to 0.08f, "Date" to 0.16f, "Name of the experiment" to 0.46f, "Page" to 0.1f, "Sign" to 0.2f)
        val line = stroke(ink, 1f)
        val bottom = top + rowHeight * (rows + 1)

        canvas.drawRect(left, top, right, bottom, line)
        for (row in 1..rows) canvas.drawLine(left, top + rowHeight * row, right, top + rowHeight * row, line)
        val headerPaint = text(11f, ink, bold = true)
        var x = left
        columns.forEachIndexed { index, (label, fraction) ->
            val width = (right - left) * fraction
            canvas.drawText(label, x + 6f, top + 17f, headerPaint)
            if (index < columns.lastIndex) canvas.drawLine(x + width, top, x + width, bottom, line)
            x += width
        }
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
        colonOffset: Float,
        baseline: Float,
        labelPaint: Paint,
        valuePaint: Paint
    ): Float {
        canvas.drawText(label, left, baseline, labelPaint)
        canvas.drawText(":", left + colonOffset, baseline, labelPaint)
        val valueX = left + colonOffset + 16f
        val lines = if (value.isBlank()) emptyList() else wrap(value, PAGE_WIDTH - MARGIN - valueX, valuePaint).take(2)
        lines.forEachIndexed { index, line ->
            canvas.drawText(line, valueX, baseline + index * 17f, valuePaint)
        }
        return baseline + maxOf(24f, lines.size * 17f + 7f)
    }

    /** The bordered Submitted By / Submitted To card; the teacher card signs off with the department. */
    private fun drawPeopleBox(
        canvas: Canvas,
        heading: String,
        people: String,
        left: Float,
        top: Float,
        width: Float,
        height: Float,
        dept: Boolean
    ) {
        canvas.drawRoundRect(RectF(left, top, left + width, top + height), 14f, 14f, stroke(ink, 1.5f))
        drawCentered(canvas, heading, top + 28f, text(14f, ink, bold = true))

        // "Name · ID" for the student; the teacher string stays on one Name line.
        val parts = people.split(" · ", ",", "\n").map(String::trim).filter(String::isNotBlank)
        val rows = if (dept) {
            listOf("Name" to people.trim())
        } else {
            listOf("Name" to parts.getOrElse(0) { "" }, "ID No" to parts.getOrElse(1) { "" })
        }

        val labelPaint = text(12f, ink, bold = true)
        val valuePaint = text(12f, ink)
        val x = left + 16f
        val valueX = left + 74f
        val maxWidth = width - 88f

        var y = top + 64f
        rows.forEach { (label, value) ->
            canvas.drawText("$label :", x, y, labelPaint)
            val lines = if (value.isBlank()) emptyList() else wrap(value, maxWidth, valuePaint).take(3)
            lines.forEachIndexed { index, line -> canvas.drawText(line, valueX, y + index * 16f, valuePaint) }
            y += maxOf(24f, lines.size * 16f + 8f)
        }

        if (dept) {
            var deptY = top + height - 46f
            listOf("Dept. of", "Bangladesh University of Business", "& Technology").forEach { line ->
                canvas.drawText(line, x, deptY, text(10f, ink, bold = true))
                deptY += 14f
            }
        }
    }

    private fun courseLine(details: CoverDetails): String? = listOf(details.courseCode, details.courseTitle)
        .filter { it.isNotBlank() }
        .joinToString(" · ")
        .ifBlank { null }

    /** Shrinks until [value] fits [maxWidth]; the university name must stay on a single line. */
    private fun fit(value: String, maxWidth: Float, size: Float, smallest: Float = size * 0.78f): Paint {
        var current = size
        while (current > smallest) {
            val paint = text(current, ink, bold = true)
            if (paint.measureText(value) <= maxWidth) return paint
            current -= 0.5f
        }
        return text(smallest, ink, bold = true)
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

    private fun text(size: Float, color: Int, bold: Boolean = false) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = size
        this.color = color
        typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
    }

    private fun stroke(color: Int, width: Float) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = width
        this.color = color
    }
}
