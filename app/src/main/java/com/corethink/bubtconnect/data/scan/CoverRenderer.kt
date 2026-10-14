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
 * The live preview (1n) and the generated PDF both call this, so what you see is what you get.
 */
object CoverRenderer {
    const val PAGE_WIDTH = 595
    const val PAGE_HEIGHT = 842

    private const val MARGIN = 28f
    private const val UNIVERSITY = "Bangladesh University of Business and Technology"

    private val ink = Color.rgb(0x1C, 0x1B, 0x1F)
    private val muted = Color.rgb(0x5F, 0x5E, 0x66)
    private val rule = Color.rgb(0xC9, 0xC7, 0xCF)

    /** The crest is the launcher foreground, whose artwork sits in the middle ~66% (adaptive-icon safe zone). */
    fun loadLogo(context: Context): Bitmap? =
        BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher_foreground)

    fun draw(canvas: Canvas, template: CoverTemplate, details: CoverDetails, logo: Bitmap? = null) {
        canvas.drawColor(Color.WHITE)
        if (!template.hasCover) return

        val frame = stroke(ink, 1.5f)
        canvas.drawRect(MARGIN, MARGIN, PAGE_WIDTH - MARGIN, PAGE_HEIGHT - MARGIN, frame)

        val cx = PAGE_WIDTH / 2f
        if (logo != null) {
            val half = 60f
            canvas.drawBitmap(logo, null, RectF(cx - half, 120f - half, cx + half, 120f + half), Paint(Paint.FILTER_BITMAP_FLAG))
        } else {
            canvas.drawCircle(cx, 120f, 38f, stroke(ink, 1.5f))
            drawCentered(canvas, "BUBT", 126f, text(15f, ink, bold = true))
        }
        drawCentered(canvas, UNIVERSITY, 192f, text(15f, ink, bold = true))

        if (template == CoverTemplate.IndexPage) drawIndex(canvas, details) else drawCover(canvas, template, details)
    }

    private fun drawCover(canvas: Canvas, template: CoverTemplate, details: CoverDetails) {
        val heading = text(30f, ink, bold = true).apply { letterSpacing = 0.08f }
        drawCentered(canvas, template.heading, 290f, heading)
        courseLine(details)?.let { drawCentered(canvas, it, 324f, text(14f, muted)) }

        var y = 380f
        if (details.title.isNotBlank()) {
            val titlePaint = text(17f, ink, bold = true)
            wrap(details.title, PAGE_WIDTH - 2 * 80f, titlePaint).take(3).forEach { line ->
                drawCentered(canvas, line, y, titlePaint)
                y += 24f
            }
        }

        val top = 470f
        val left = 72f
        val columnWidth = (PAGE_WIDTH - 2 * left - 32f) / 2
        drawBlock(canvas, "Submitted To", details.submittedTo, left, top, columnWidth)
        drawBlock(canvas, "Submitted By", details.submittedBy, left + columnWidth + 32f, top, columnWidth)

        canvas.drawLine(left, 740f, PAGE_WIDTH - left, 740f, stroke(rule, 1f))
        drawCentered(canvas, "Date of submission: ${longDate(details.date)}", 770f, text(13f, ink))
    }

    /** Index page: who it belongs to, then an empty table to fill in by hand. */
    private fun drawIndex(canvas: Canvas, details: CoverDetails) {
        val heading = text(26f, ink, bold = true).apply { letterSpacing = 0.08f }
        drawCentered(canvas, "INDEX", 256f, heading)
        courseLine(details)?.let { drawCentered(canvas, it, 284f, text(13f, muted)) }
        if (details.submittedBy.isNotBlank()) {
            drawCentered(canvas, details.submittedBy, 306f, text(12f, muted))
        }

        val left = 56f
        val right = PAGE_WIDTH - 56f
        val top = 330f
        val rowHeight = 28f
        val rows = 15
        val columns = listOf("No." to 0.08f, "Date" to 0.16f, "Name of the experiment" to 0.46f, "Page" to 0.1f, "Sign" to 0.2f)
        val line = stroke(ink, 1f)
        val bottom = top + rowHeight * (rows + 1)

        canvas.drawRect(left, top, right, bottom, line)
        for (row in 1..rows) canvas.drawLine(left, top + rowHeight * row, right, top + rowHeight * row, line)
        val headerPaint = text(11f, ink, bold = true)
        var x = left
        columns.forEachIndexed { index, (label, fraction) ->
            val width = (right - left) * fraction
            canvas.drawText(label, x + 6f, top + 18f, headerPaint)
            if (index < columns.lastIndex) canvas.drawLine(x + width, top, x + width, bottom, line)
            x += width
        }
    }

    private fun drawBlock(canvas: Canvas, label: String, value: String, left: Float, top: Float, width: Float) {
        canvas.drawText(label.uppercase(Locale.US), left, top, text(10f, muted, bold = true).apply { letterSpacing = 0.1f })
        canvas.drawLine(left, top + 8f, left + width, top + 8f, stroke(rule, 1f))
        val valuePaint = text(13f, ink)
        var y = top + 30f
        // "Rafi Ahmed · 22235103045" reads better as one item per line.
        value.split(" · ", ",", "\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .flatMap { wrap(it, width, valuePaint) }
            .take(5)
            .forEach { line ->
                canvas.drawText(line, left, y, valuePaint)
                y += 20f
            }
    }

    private fun courseLine(details: CoverDetails): String? = listOf(details.courseCode, details.courseTitle)
        .filter { it.isNotBlank() }
        .joinToString(" · ")
        .ifBlank { null }

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
