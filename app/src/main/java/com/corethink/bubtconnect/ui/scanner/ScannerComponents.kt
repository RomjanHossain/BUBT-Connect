package com.corethink.bubtconnect.ui.scanner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.corethink.bubtconnect.data.model.CoverDetails
import com.corethink.bubtconnect.data.model.CoverTemplate
import com.corethink.bubtconnect.data.model.ScanPage
import com.corethink.bubtconnect.data.scan.CoverRenderer
import com.corethink.bubtconnect.data.scan.PageBitmaps
import com.corethink.bubtconnect.ui.design.BubtShapes
import com.corethink.bubtconnect.ui.design.BubtSpacing
import com.corethink.bubtconnect.ui.onboarding.StepProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

const val ScannerTotalSteps = 4
const val PageAspectRatio = CoverRenderer.PAGE_WIDTH.toFloat() / CoverRenderer.PAGE_HEIGHT

enum class WizardLeading { Close, Back, None }

/** Boxed close/back button, title, "Step n of 4" and the segmented progress bar. */
@Composable
fun WizardTopBar(
    title: String,
    step: Int,
    leading: WizardLeading,
    onLeading: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leading != WizardLeading.None) {
                Surface(
                    onClick = onLeading,
                    shape = BubtShapes.Field,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (leading == WizardLeading.Close) Icons.Filled.Close else Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (leading == WizardLeading.Close) "Close" else "Back",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(BubtSpacing.Md))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Step $step of $ScannerTotalSteps",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        StepProgress(totalSteps = ScannerTotalSteps, completedSteps = step)
    }
}

/** A4 cover drawn by [CoverRenderer], scaled to the composable's width. */
@Composable
fun CoverPreview(template: CoverTemplate, details: CoverDetails, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val logo = remember(context) { CoverRenderer.loadLogo(context) }
    Canvas(modifier = modifier.aspectRatio(PageAspectRatio)) {
        drawIntoCanvas { canvas ->
            val native = canvas.nativeCanvas
            native.save()
            native.scale(size.width / CoverRenderer.PAGE_WIDTH, size.height / CoverRenderer.PAGE_HEIGHT)
            CoverRenderer.draw(native, template, details, logo)
            native.restore()
        }
    }
}

/** Full-screen cover on a scrim; tap anywhere or hit × to close. */
@Composable
fun CoverPreviewDialog(template: CoverTemplate, details: CoverDetails, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.88f))
                .clickable(onClick = onDismiss)
                .padding(BubtSpacing.Screen),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                onClick = onDismiss,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = BubtSpacing.Md)
                    .size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close preview",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            CoverPreview(
                template = template,
                details = details,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(PageAspectRatio)
            )
        }
    }
}

/** Page image decoded off the main thread at thumbnail size, with rotate / filter applied. */
@Composable
fun PageThumbnail(page: ScanPage, modifier: Modifier = Modifier, maxSize: Int = 480) {
    val context = LocalContext.current
    val thumbnail by produceState<Thumbnail>(Thumbnail.Loading, page.uri, page.rotation, page.grayscale) {
        value = withContext(Dispatchers.IO) {
            PageBitmaps.decode(context, page, maxSize)?.let { Thumbnail.Ready(it.asImageBitmap()) } ?: Thumbnail.Failed
        }
    }
    Box(
        modifier = modifier.background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        when (val state = thumbnail) {
            Thumbnail.Loading -> CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            Thumbnail.Failed -> Icon(
                Icons.Outlined.BrokenImage,
                contentDescription = "Couldn't load page",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            is Thumbnail.Ready -> Image(
                bitmap = state.image,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp)
            )
        }
    }
}

private sealed interface Thumbnail {
    data object Loading : Thumbnail
    data object Failed : Thumbnail
    data class Ready(val image: ImageBitmap) : Thumbnail
}
