package com.corethink.bubtconnect.ui.scanner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.corethink.bubtconnect.data.model.ScanPage
import com.corethink.bubtconnect.ui.design.BubtShapes
import com.corethink.bubtconnect.ui.design.BubtSpacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.fairscan.imageprocessing.Point
import org.fairscan.imageprocessing.Quad
import org.fairscan.imageprocessing.createQuad
import kotlin.math.min
import kotlin.math.roundToInt

/** Corner grab size, big enough to catch without covering the corner itself. */
private val HandleSize = 28.dp
private const val MIN_CORNER = 0.02
private val FullFrame = createQuad(
    listOf(Point(0.0, 0.0), Point(1.0, 0.0), Point(1.0, 1.0), Point(0.0, 1.0))
)

private fun Quad.corner(index: Int): Point = when (index) {
    0 -> topLeft
    1 -> topRight
    2 -> bottomRight
    else -> bottomLeft
}

private fun Quad.withCorner(index: Int, point: Point): Quad = when (index) {
    0 -> copy(topLeft = point)
    1 -> copy(topRight = point)
    2 -> copy(bottomRight = point)
    else -> copy(bottomLeft = point)
}

/**
 * FairScan-style re-crop: the raw photo with the current page outline, four draggable
 * corners and everything outside the quad dimmed. Confirming returns the normalized quad.
 */
@Composable
fun CropPageDialog(
    page: ScanPage,
    onConfirm: (Quad) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val bitmap by produceState<ImageBitmap?>(initialValue = null, key1 = page.sourceUri, key2 = page.id) {
        value = withContext(Dispatchers.IO) {
            loadSourceBitmap(context, page.sourceUri, 1600)?.asImageBitmap()
        }
    }
    val quadState = remember(page.id) { mutableStateOf(page.quad ?: FullFrame) }
    val quad = quadState.value

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = BubtSpacing.Screen)
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CropIconButton(Icons.Filled.Close, "Cancel crop", onClick = onDismiss)
                Spacer(modifier = Modifier.width(BubtSpacing.Md))
                Text(
                    text = "Crop page",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    onClick = { onConfirm(quad) },
                    enabled = bitmap != null,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Check, contentDescription = "Apply crop", modifier = Modifier.size(20.dp))
                    }
                }
            }

            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(BubtSpacing.Screen)
            ) {
                val image = bitmap
                if (image == null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
                    }
                } else {
                    val density = LocalDensity.current
                    val boxWidthPx = with(density) { maxWidth.toPx() }
                    val boxHeightPx = with(density) { maxHeight.toPx() }
                    // Fit-center transform shared by the canvas and the corner handles.
                    val scale = min(boxWidthPx / image.width, boxHeightPx / image.height)
                    val offsetX = (boxWidthPx - image.width * scale) / 2f
                    val offsetY = (boxHeightPx - image.height * scale) / 2f
                    fun toDisplay(point: Point) = Offset(
                        x = point.x.toFloat() * image.width * scale + offsetX,
                        y = point.y.toFloat() * image.height * scale + offsetY
                    )
                    fun dragToNormalized(delta: Offset) = Offset(
                        delta.x / (image.width * scale),
                        delta.y / (image.height * scale)
                    )

                    val accent = MaterialTheme.colorScheme.primary
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawImage(
                            image = image,
                            dstOffset = IntOffset(offsetX.roundToInt(), offsetY.roundToInt()),
                            dstSize = IntSize((image.width * scale).toInt(), (image.height * scale).toInt())
                        )
                        val outline = Path().apply {
                            val tl = toDisplay(quad.topLeft)
                            moveTo(tl.x, tl.y)
                            val tr = toDisplay(quad.topRight)
                            lineTo(tr.x, tr.y)
                            val br = toDisplay(quad.bottomRight)
                            lineTo(br.x, br.y)
                            val bl = toDisplay(quad.bottomLeft)
                            lineTo(bl.x, bl.y)
                            close()
                        }
                        // Dim everything outside the quad by punching a hole in a dark layer.
                        drawIntoCanvas { canvas ->
                            canvas.saveLayer(Rect(0f, 0f, size.width, size.height), Paint())
                            canvas.drawRect(
                                0f, 0f, size.width, size.height,
                                Paint().apply { color = Color.Black.copy(alpha = 0.55f) }
                            )
                            canvas.drawPath(outline, Paint().apply { blendMode = BlendMode.Clear })
                            canvas.restore()
                        }
                        drawPath(outline, accent, style = Stroke(width = 2.dp.toPx()))
                    }

                    val handleRadiusPx = with(density) { HandleSize.toPx() / 2f }
                    for (index in 0..3) {
                        Handle(
                            quadState = quadState,
                            index = index,
                            display = toDisplay(quad.corner(index)),
                            radiusPx = handleRadiusPx,
                            toNormalized = ::dragToNormalized
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = BubtSpacing.Screen, vertical = BubtSpacing.Md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Drag the corners to the edges of the page.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { quadState.value = FullFrame }, enabled = bitmap != null) {
                    Text("Reset")
                }
            }
        }
    }
}

/** One draggable corner. Reads/writes the shared quad state so drags never go stale. */
@Composable
private fun Handle(
    quadState: MutableState<Quad>,
    index: Int,
    display: Offset,
    radiusPx: Float,
    toNormalized: (Offset) -> Offset
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier
            .offset {
                IntOffset((display.x - radiusPx).roundToInt(), (display.y - radiusPx).roundToInt())
            }
            .size(HandleSize)
            .border(3.dp, Color.White, CircleShape)
            .pointerInput(index, toNormalized) {
                detectDragGestures { change, drag ->
                    change.consume()
                    val normalized = toNormalized(drag)
                    val current = quadState.value.corner(index)
                    val next = Point(
                        (current.x + normalized.x).coerceIn(MIN_CORNER, 1.0 - MIN_CORNER),
                        (current.y + normalized.y).coerceIn(MIN_CORNER, 1.0 - MIN_CORNER)
                    )
                    quadState.value = quadState.value.withCorner(index, next)
                }
            }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onPrimary)
            )
        }
    }
}

/** Square outlined icon button, matching the capture screen's chrome. */
@Composable
private fun CropIconButton(icon: ImageVector, contentDescription: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = BubtShapes.Field,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.size(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                icon,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/** Decodes the stored original (EXIF-rotated upright), downsampling past [maxSide]. */
private fun loadSourceBitmap(context: Context, uriString: String?, maxSide: Int): Bitmap? {
    uriString ?: return null
    return runCatching {
        val uri = Uri.parse(uriString)
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }

        var sampleSize = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sampleSize * 2) >= maxSide) {
            sampleSize *= 2
        }
        val decoded = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sampleSize })
        } ?: return null

        val orientation = resolver.openInputStream(uri)?.use {
            ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        } ?: ExifInterface.ORIENTATION_NORMAL
        val degrees = when (orientation) {
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
