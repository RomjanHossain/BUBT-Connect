package com.corethink.bubtconnect.data.scan

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.googlecode.tesseract.android.TessBaseAPI
import com.googlecode.tesseract.android.TessBaseAPI.PageIteratorLevel
import java.io.File

/**
 * One recognized word plus its text-line metrics, in source-bitmap pixels.
 * [lineHeight] / [baseline] come from the surrounding line so the invisible
 * text layer matches what Tesseract saw, not the word's own box.
 */
data class OcrWord(
    val text: String,
    val left: Int,
    val lineHeight: Int,
    val baseline: Int
)

/**
 * Tesseract-backed recognition for the invisible text layer of exported PDFs
 * (the same idea as FairScan's OCR export, drawing words at near-zero alpha so
 * pages stay printable but searchable and copyable).
 *
 * The English model ships in `assets/tessdata` — fetched at build time by the
 * `downloadOcrLanguage` Gradle task — and is copied to filesDir once, so OCR
 * works fully offline with no runtime download and no INTERNET permission.
 */
class ScanOcr(context: Context) {
    private val appContext = context.applicationContext
    private val lock = Any()
    private var tess: TessBaseAPI? = null
    private var unavailable = false

    /** Blocking and idempotent; call from a background thread before [words]. */
    fun ensureReady(): Boolean = synchronized(lock) {
        if (tess != null) return true
        if (unavailable) return false
        return runCatching {
            val dataDir = File(appContext.filesDir, "tessdata").apply { mkdirs() }
            val model = File(dataDir, MODEL)
            if (!model.exists() || model.length() == 0L) {
                appContext.assets.open("$ASSET_DIR/$MODEL").use { input ->
                    model.outputStream().use { output -> input.copyTo(output) }
                }
            }
            // Tesseract expects <path>/tessdata/<lang>.traineddata.
            val api = TessBaseAPI()
            if (!api.init(appContext.filesDir.absolutePath, LANGUAGE)) {
                api.recycle()
                error("Tesseract init failed")
            }
            tess = api
        }.onFailure {
            Log.e(TAG, "OCR unavailable", it)
            unavailable = true
        }.isSuccess
    }

    /** Recognized words for one page; empty when OCR isn't ready or nothing read. */
    fun words(bitmap: Bitmap): List<OcrWord> = synchronized(lock) {
        val api = tess ?: return emptyList()
        runCatching {
            api.setImage(bitmap)
            api.getUTF8Text() // trigger recognition, then walk the result iterator
            val iterator = api.resultIterator ?: return@runCatching emptyList<OcrWord>()
            val words = mutableListOf<OcrWord>()
            iterator.begin()
            do {
                val text = iterator.getUTF8Text(PageIteratorLevel.RIL_WORD)
                val confidence = iterator.confidence(PageIteratorLevel.RIL_WORD)
                if (text != null && text.isNotBlank() && confidence > 50f) {
                    val wordBox = iterator.getBoundingRect(PageIteratorLevel.RIL_WORD)
                    val lineBox = iterator.getBoundingRect(PageIteratorLevel.RIL_TEXTLINE)
                    words += OcrWord(
                        text = text.trim(),
                        left = wordBox.left,
                        lineHeight = lineBox.height(),
                        baseline = lineBox.bottom
                    )
                }
            } while (iterator.next(PageIteratorLevel.RIL_WORD))
            iterator.delete()
            words
        }.getOrElse {
            Log.e(TAG, "OCR failed", it)
            emptyList()
        }
    }

    fun close() = synchronized(lock) {
        tess?.recycle()
        tess = null
    }

    private companion object {
        const val TAG = "ScanOcr"
        const val MODEL = "eng.traineddata"
        const val ASSET_DIR = "tessdata"
        const val LANGUAGE = "eng"
    }
}
