/*
 * Copyright 2025-2026 The FairScan authors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <https://www.gnu.org/licenses/>.
 */
package com.corethink.bubtconnect.data.scan

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.fairscan.imageprocessing.ColorMode
import org.fairscan.imageprocessing.ImageSize
import org.fairscan.imageprocessing.Mode
import org.fairscan.imageprocessing.Quad
import org.fairscan.imageprocessing.autoColorMode
import org.fairscan.imageprocessing.detectDocumentQuad
import org.fairscan.imageprocessing.extractDocument
import org.fairscan.imageprocessing.resizeForMaxPixels
import org.fairscan.imageprocessing.scaledTo
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.io.File
import java.io.FileOutputStream

/** One page after the scanning pipeline: [file] is warped + enhanced, [sourceFile] is the raw photo. */
data class ProcessedPage(
    val file: File,
    val sourceFile: File,
    val normalizedQuad: Quad?,
    val colorMode: ColorMode
)

/**
 * The FairScan capture pipeline: segment → fit quad → perspective warp → auto enhancement.
 * Runs on Dispatchers.IO; callers pass the photo file and where the page should land.
 */
class DocumentPipeline(private val context: Context) {

    companion object {
        private const val TAG = "DocumentPipeline"

        /** Decodes are capped so a full-frame phone photo stays a sane size in memory. */
        private const val MAX_DECODE_SIDE = 3200

        /** FairScan's "Balanced" export budget: ~2 MP pages (8 MP for B&W, which compresses to ~nothing). */
        private const val MAX_PIXELS = 2_000_000L
        private const val JPEG_QUALITY = 90
    }

    private val segmentation = DocumentSegmentation(context)
    @Volatile private var opencvReady = false

    private fun ensureReady() {
        if (!opencvReady) {
            synchronized(this) {
                if (!OpenCVLoader.initLocal()) {
                    Log.e(TAG, "OpenCV initialization failed")
                    error("OpenCV unavailable")
                }
                opencvReady = true
            }
        }
        segmentation.initialize()
    }

    /** Mask for a live camera frame, for the viewfinder overlay. */
    suspend fun segmentFrame(bitmap: Bitmap): DocumentSegmentation.SegmentationResult? =
        withContext(Dispatchers.IO) {
            runCatching {
                ensureReady()
                segmentation.runSegmentationAndReturn(bitmap)
            }.onFailure { Log.e(TAG, "Live segmentation failed", it) }.getOrNull()
        }

    /**
     * Detects the page in [sourceFile] (honouring EXIF rotation), warps it flat,
     * enhances it with the auto-detected color mode and writes the result to [outFile].
     */
    suspend fun process(sourceFile: File, outFile: File, mode: Mode): ProcessedPage =
        withContext(Dispatchers.IO) {
            ensureReady()
            val bitmap = decodeUpright(sourceFile)
                ?: error("Couldn't read ${sourceFile.name}")

            val frameSize = ImageSize(bitmap.width, bitmap.height)
            val result = segmentation.runSegmentationAndReturn(bitmap)
            val mask = result?.segmentation
            val quadInMask = mask?.let {
                detectDocumentQuad(mask, frameSize, mode)
            }

            val rgba = Mat()
            Utils.bitmapToMat(bitmap, rgba)
            val bgr = Mat()
            Imgproc.cvtColor(rgba, bgr, Imgproc.COLOR_RGBA2BGR)
            rgba.release()
            bitmap.recycle()

            var normalizedQuad: Quad? = null
            val colorMode: ColorMode
            val page: Mat = if (mask == null || quadInMask == null) {
                // No document detected: keep the whole (upright) photo, enhanced as color.
                colorMode = ColorMode.COLOR
                resizeForMaxPixels(bgr, MAX_PIXELS.toDouble())
            } else {
                val quad = quadInMask.scaledTo(mask.width, mask.height, bgr.width(), bgr.height())
                normalizedQuad = quad.scaledTo(bgr.width(), bgr.height(), 1, 1)
                colorMode = autoColorMode(bgr, mask, quad)
                extractDocument(bgr, quad, 0, colorMode, MAX_PIXELS)
            }
            bgr.release()

            writePage(page, outFile, colorMode)
            page.release()

            ProcessedPage(outFile, sourceFile, normalizedQuad, colorMode)
        }

    private fun writePage(page: Mat, outFile: File, colorMode: ColorMode) {
        val bitmap = Bitmap.createBitmap(page.cols(), page.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(page, bitmap)
        outFile.parentFile?.mkdirs()
        FileOutputStream(outFile).use { output ->
            // B&W pages are flat, so PNG keeps them artifact-free at a small size.
            val format = if (colorMode == ColorMode.BLACK_AND_WHITE) {
                Bitmap.CompressFormat.PNG
            } else {
                Bitmap.CompressFormat.JPEG
            }
            bitmap.compress(format, JPEG_QUALITY, output)
        }
        bitmap.recycle()
    }

    /** Decodes with EXIF orientation applied, downsampling past [MAX_DECODE_SIDE]. */
    private fun decodeUpright(file: File): Bitmap? = runCatching {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        var sampleSize = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sampleSize * 2) >= MAX_DECODE_SIDE) {
            sampleSize *= 2
        }
        val decoded = BitmapFactory.decodeFile(
            file.absolutePath,
            BitmapFactory.Options().apply { inSampleSize = sampleSize }
        ) ?: return null

        val degrees = when (runCatching {
            ExifInterface(file.absolutePath).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
        if (degrees == 0) {
            decoded
        } else {
            val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
            Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
                .also { if (it !== decoded) decoded.recycle() }
        }
    }.getOrNull()
}
