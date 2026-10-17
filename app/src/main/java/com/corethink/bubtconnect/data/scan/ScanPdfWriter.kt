package com.corethink.bubtconnect.data.scan

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.media.ExifInterface
import android.net.Uri
import com.corethink.bubtconnect.data.model.CoverDetails
import com.corethink.bubtconnect.data.model.CoverTemplate
import com.corethink.bubtconnect.data.model.ScanPage
import java.io.File
import kotlin.math.max
import kotlin.math.min

/** Decodes page images, honouring EXIF orientation plus the user's rotate / filter edits. */
object PageBitmaps {

    fun decode(context: Context, page: ScanPage, maxSize: Int): Bitmap? = runCatching {
        val uri = Uri.parse(page.uri)
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) } ?: return null
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        var sample = 1
        while (max(bounds.outWidth, bounds.outHeight) / (sample * 2) >= maxSize) sample *= 2
        val decoded = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: return null

        val exifRotation = resolver.openInputStream(uri)?.use { exifRotation(ExifInterface(it)) } ?: 0
        val rotated = rotate(decoded, (exifRotation + page.rotation) % 360)
        if (page.grayscale) grayscale(rotated) else rotated
    }.getOrNull()

    private fun exifRotation(exif: ExifInterface): Int =
        when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }

    private fun rotate(bitmap: Bitmap, degrees: Int): Bitmap {
        if (degrees == 0) return bitmap
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            .also { if (it !== bitmap) bitmap.recycle() }
    }

    private fun grayscale(bitmap: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val paint = Paint().apply { colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) }) }
        Canvas(result).drawBitmap(bitmap, 0f, 0f, paint)
        bitmap.recycle()
        return result
    }
}

/** Merges the cover and pages into one A4 PDF. */
object ScanPdfWriter {
    private const val PAGE_MARGIN = 24f

    /** 2480 px = 300 dpi across A4, so the export never throws away scan detail. */
    private const val MAX_IMAGE_SIDE = 2480

    /** Returns the number of pages written. Pages whose image can't be read are skipped. */
    fun write(
        context: Context,
        target: File,
        template: CoverTemplate,
        details: CoverDetails,
        pages: List<ScanPage>,
        ocr: ScanOcr? = null
    ): Int {
        val document = PdfDocument()
        var pageNumber = 0
        try {
            if (template.hasCover) {
                val page = document.startPage(pageInfo(++pageNumber))
                CoverRenderer.draw(page.canvas, template, details, CoverRenderer.loadLogo(context))
                document.finishPage(page)
            }
            val bitmapPaint = Paint(Paint.FILTER_BITMAP_FLAG)
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.BLACK
                // 1/255 alpha: invisible to the eye, but still a real text run that
                // search, selection and screen readers pick up.
                alpha = 1
                textAlign = Paint.Align.LEFT
            }
            pages.forEach { scan ->
                val bitmap = PageBitmaps.decode(context, scan, MAX_IMAGE_SIDE) ?: return@forEach
                val page = document.startPage(pageInfo(++pageNumber))
                val dest = fitRect(bitmap.width, bitmap.height)
                page.canvas.drawBitmap(bitmap, null, dest, bitmapPaint)
                if (ocr != null && ocr.ensureReady()) {
                    drawTextLayer(page.canvas, ocr.words(bitmap), dest, bitmap.width, bitmap.height, textPaint)
                }
                document.finishPage(page)
                bitmap.recycle()
            }
            target.parentFile?.mkdirs()
            target.outputStream().use { document.writeTo(it) }
        } finally {
            document.close()
        }
        return pageNumber
    }

    /**
     * Paints the recognized words back over the page image with the same transform
     * the bitmap got, so the extractable text lines up with what's printed.
     */
    private fun drawTextLayer(
        canvas: Canvas,
        words: List<OcrWord>,
        dest: RectF,
        bitmapWidth: Int,
        bitmapHeight: Int,
        paint: Paint
    ) {
        if (words.isEmpty()) return
        val scaleX = dest.width() / bitmapWidth
        val scaleY = dest.height() / bitmapHeight
        words.forEach { word ->
            if (word.lineHeight <= 0) return@forEach
            paint.textSize = word.lineHeight * scaleY * 0.8f
            canvas.drawText(
                word.text,
                dest.left + word.left * scaleX,
                dest.top + word.baseline * scaleY,
                paint
            )
        }
    }

    private fun pageInfo(number: Int) =
        PdfDocument.PageInfo.Builder(CoverRenderer.PAGE_WIDTH, CoverRenderer.PAGE_HEIGHT, number).create()

    /** Largest centred rect with the image's aspect ratio inside the page margins. */
    private fun fitRect(width: Int, height: Int): RectF {
        val availableWidth = CoverRenderer.PAGE_WIDTH - 2 * PAGE_MARGIN
        val availableHeight = CoverRenderer.PAGE_HEIGHT - 2 * PAGE_MARGIN
        val scale = min(availableWidth / width, availableHeight / height)
        val drawWidth = width * scale
        val drawHeight = height * scale
        val left = (CoverRenderer.PAGE_WIDTH - drawWidth) / 2
        val top = (CoverRenderer.PAGE_HEIGHT - drawHeight) / 2
        return RectF(left, top, left + drawWidth, top + drawHeight)
    }
}
