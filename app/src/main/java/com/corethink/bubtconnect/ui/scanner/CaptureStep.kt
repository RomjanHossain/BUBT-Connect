package com.corethink.bubtconnect.ui.scanner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.FlashAuto
import androidx.compose.material.icons.outlined.FlashOff
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.NoPhotography
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.corethink.bubtconnect.data.model.ScanPage
import com.corethink.bubtconnect.data.scan.lerpQuad
import com.corethink.bubtconnect.ui.components.BubtSegmentedControl
import com.corethink.bubtconnect.ui.design.BubtPrimaryButton
import com.corethink.bubtconnect.ui.design.BubtShapes
import org.fairscan.imageprocessing.CameraIntrinsics
import org.fairscan.imageprocessing.OpticalMeasures
import org.fairscan.imageprocessing.cameraIntrinsics
import kotlin.math.max
import com.corethink.bubtconnect.ui.design.BubtSpacing
import org.fairscan.imageprocessing.Point
import java.io.File
import java.util.concurrent.Executors
import kotlin.math.max

private enum class CaptureMode(val label: String) { Camera("Camera"), Gallery("Gallery"), Auto("Auto") }

// The capture screen stays dark in both themes, like a camera app.
private val CaptureBackground = Color(0xFF141416)
private val CaptureSurface = Color(0xFF26262B)
private val CaptureContent = Color.White
private val CaptureMuted = Color(0xFFB5B4BC)

private const val MAX_GALLERY_PICKS = 30

/** 1q · Capture / Upload. Left: last shot (tap to import from gallery). Right: page stack → review. */
@Composable
fun CaptureStep(
    state: ScannerUiState,
    onClose: () -> Unit,
    newCaptureFile: () -> File,
    onCameraPageCaptured: (File, OpticalMeasures?, (Boolean) -> Unit) -> Unit,
    onPagesAdded: (List<Uri>) -> Unit,
    onReview: () -> Unit,
    liveDetection: ScanDetection?,
    onFrameAnalyzed: (ImageProxy) -> Unit
) {
    val context = LocalContext.current
    var mode by rememberSaveable { mutableStateOf(CaptureMode.Camera) }
    var flashOn by rememberSaveable { mutableStateOf(false) }
    var capturing by remember { mutableStateOf(false) }
    var processing by remember { mutableStateOf(false) }
    var hasPermission by remember { mutableStateOf(context.hasCameraPermission()) }
    var askedOnce by rememberSaveable { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasPermission = granted
        askedOnce = true
    }
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(MAX_GALLERY_PICKS)
    ) { uris -> onPagesAdded(uris) }
    val pickFromGallery = {
        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    LaunchedEffect(Unit) {
        if (!hasPermission && !askedOnce) permissionLauncher.launch(Manifest.permission.CAMERA)
    }
    // Picks up a permission granted from system Settings.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { hasPermission = context.hasCameraPermission() }

    val optics = remember { CaptureOptics() }

    @OptIn(ExperimentalCamera2Interop::class)
    val imageCapture = remember {
        ImageCapture.Builder()
            // FairScan parity: shoot at the sensor's 4:3 maximum so pages keep full detail.
            .setResolutionSelector(
                ResolutionSelector.Builder()
                    .setResolutionStrategy(
                        ResolutionStrategy(
                            Size(4400, 3300),
                            ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER_THEN_HIGHER
                        )
                    )
                    .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
                    .build()
            )
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .also { builder ->
                Camera2Interop.Extender(builder).setSessionCaptureCallback(
                    object : CameraCaptureSession.CaptureCallback() {
                        override fun onCaptureCompleted(
                            session: CameraCaptureSession,
                            request: CaptureRequest,
                            result: TotalCaptureResult
                        ) {
                            result.get(CaptureResult.LENS_FOCUS_DISTANCE)?.let {
                                optics.lastFocusDistanceDiopters = it
                            }
                        }
                    }
                )
            }
            .build()
    }
    LaunchedEffect(flashOn) {
        imageCapture.flashMode = if (flashOn) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
    }
    val cameraReady = mode != CaptureMode.Gallery && hasPermission

    fun capture() {
        if (capturing || processing) return
        capturing = true
        val file = newCaptureFile()
        imageCapture.takePicture(
            ImageCapture.OutputFileOptions.Builder(file).build(),
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    // Detect, straighten and enhance before the page joins the stack.
                    processing = true
                    onCameraPageCaptured(file, optics.measures()) { ok ->
                        capturing = false
                        processing = false
                        if (!ok) {
                            Toast.makeText(context, "Couldn't process the page. Try again.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    capturing = false
                    Toast.makeText(context, "Couldn't capture the page. Try again.", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CaptureBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = BubtSpacing.Screen)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DarkIconButton(Icons.Filled.Close, contentDescription = "Close", onClick = onClose)
            Spacer(modifier = Modifier.width(BubtSpacing.Md))
            Text(
                text = "Step 3 of $ScannerTotalSteps · Page ${state.pages.size + 1}",
                style = MaterialTheme.typography.titleMedium,
                color = CaptureContent,
                modifier = Modifier.weight(1f)
            )
            DarkIconButton(
                icon = when {
                    mode == CaptureMode.Auto -> Icons.Outlined.FlashAuto
                    flashOn -> Icons.Outlined.FlashOn
                    else -> Icons.Outlined.FlashOff
                },
                contentDescription = if (flashOn) "Turn flash off" else "Turn flash on",
                onClick = { flashOn = !flashOn },
                enabled = cameraReady
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(BubtShapes.Card)
                .background(CaptureSurface),
            contentAlignment = Alignment.Center
        ) {
            when {
                mode == CaptureMode.Gallery -> ViewfinderMessage(
                    icon = Icons.Outlined.PhotoLibrary,
                    message = "Import photos of your pages from the gallery.",
                    action = "Choose photos",
                    onAction = pickFromGallery
                )
                !hasPermission -> ViewfinderMessage(
                    icon = Icons.Outlined.NoPhotography,
                    message = "Allow camera access to scan pages, or import them from the gallery.",
                    action = if (askedOnce) "Open settings" else "Allow camera",
                    onAction = {
                        if (askedOnce) context.startActivity(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                        ) else permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                )
                else -> {
                    CameraPreview(
                        imageCapture = imageCapture,
                        optics = optics,
                        onFrameAnalyzed = onFrameAnalyzed,
                        modifier = Modifier.fillMaxSize()
                    )
                    DetectionQuadOverlay(detection = liveDetection, modifier = Modifier.fillMaxSize())
                    FrameGuide(modifier = Modifier.fillMaxSize().padding(BubtSpacing.Xl))
                    Surface(
                        shape = BubtShapes.Chip,
                        color = Color.Black.copy(alpha = 0.6f),
                        contentColor = CaptureContent,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = BubtSpacing.Xl + BubtSpacing.Lg)
                    ) {
                        Text(
                            text = when {
                                capturing || processing -> "Straightening & enhancing…"
                                liveDetection != null -> "Edges found · hold steady"
                                else -> "Fit the page inside the frame"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = BubtSpacing.Md, vertical = BubtSpacing.Xs + 2.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(BubtSpacing.Md))
        BubtSegmentedControl(
            options = CaptureMode.entries,
            selected = mode,
            label = { it.label },
            onSelect = { selected ->
                mode = selected
                if (selected == CaptureMode.Gallery) pickFromGallery()
            }
        )
        Spacer(modifier = Modifier.height(BubtSpacing.Lg))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LastShotButton(page = state.pages.lastOrNull(), onClick = pickFromGallery)
            ShutterButton(enabled = cameraReady && !capturing && !processing, onClick = ::capture)
            PageStackButton(
                count = state.pages.size,
                enabled = state.pages.isNotEmpty() || state.template.hasCover,
                onClick = onReview
            )
        }
        Spacer(modifier = Modifier.height(BubtSpacing.Lg))
    }
}

/**
 * Lens metadata FairScan feeds into the page warp: with the real focal length and the
 * focus distance at shutter time, warped pages come out aspect-true instead of slightly
 * trapezoid. Mirrors FairScan's CameraCaptureController.
 */
class CaptureOptics {
    @Volatile
    var intrinsics: CameraIntrinsics? = null

    @Volatile
    var lastFocusDistanceDiopters: Float? = null
    var canUseFocusDistance = false

    fun setCameraCharacteristics(cameraInfo: Camera2CameraInfo) {
        val focalLengths = cameraInfo.getCameraCharacteristic(
            android.hardware.camera2.CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS
        )
        val sensorSize = cameraInfo.getCameraCharacteristic(
            android.hardware.camera2.CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE
        )
        intrinsics = if (focalLengths == null || focalLengths.size != 1 || sensorSize == null) {
            null
        } else {
            cameraIntrinsics(focalLengths[0], max(sensorSize.width, sensorSize.height))
        }
        val calibration = cameraInfo.getCameraCharacteristic(
            android.hardware.camera2.CameraCharacteristics.LENS_INFO_FOCUS_DISTANCE_CALIBRATION
        )
        canUseFocusDistance = calibration == CameraMetadata.LENS_INFO_FOCUS_DISTANCE_CALIBRATION_CALIBRATED ||
            calibration == CameraMetadata.LENS_INFO_FOCUS_DISTANCE_CALIBRATION_APPROXIMATE
    }

    fun measures(): OpticalMeasures? {
        val intrinsics = intrinsics ?: return null
        val diopters = lastFocusDistanceDiopters
        val subjectDistanceInMm = if (canUseFocusDistance && diopters != null && diopters != 0.0f) {
            1000 / diopters
        } else {
            null
        }
        return OpticalMeasures(intrinsics, subjectDistanceInMm)
    }
}

/**
 * Binds a back-camera preview + [imageCapture] + [ImageAnalysis] to the current lifecycle.
 * Analysis frames go to [onFrameAnalyzed] (live document detection) on a single-thread executor.
 */
@Composable
private fun CameraPreview(
    imageCapture: ImageCapture,
    optics: CaptureOptics,
    onFrameAnalyzed: (ImageProxy) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
    }
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    val imageAnalysis = remember {
        ImageAnalysis.Builder()
            .setResolutionSelector(
                ResolutionSelector.Builder()
                    .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
                    .build()
            )
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()
            .also { it.setAnalyzer(analysisExecutor, onFrameAnalyzed) }
    }
    var unavailable by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val future = ProcessCameraProvider.getInstance(context)
        var provider: ProcessCameraProvider? = null
        var disposed = false
        future.addListener({
            if (disposed) return@addListener
            runCatching {
                val cameraProvider = future.get().also { provider = it }
                val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                cameraProvider.unbindAll()
                val camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalysis,
                    imageCapture
                )
                runCatching {
                    optics.setCameraCharacteristics(Camera2CameraInfo.from(camera.cameraInfo))
                }.onFailure { Log.w("CaptureStep", "Lens metadata unavailable for warp math", it) }
            }.onFailure { unavailable = true }
        }, ContextCompat.getMainExecutor(context))
        onDispose {
            disposed = true
            provider?.unbindAll()
            imageAnalysis.clearAnalyzer()
            analysisExecutor.shutdown()
        }
    }

    if (unavailable) {
        ViewfinderMessage(icon = Icons.Outlined.NoPhotography, message = "No camera available on this device.")
    } else {
        AndroidView(factory = { previewView }, modifier = modifier)
    }
}

/**
 * Live document outline over the viewfinder. The quad arrives stabilized from the pipeline
 * and is lerped toward each new target so the outline glides instead of jumping.
 */
@Composable
private fun DetectionQuadOverlay(detection: ScanDetection?, modifier: Modifier = Modifier) {
    var displayed by remember { mutableStateOf(detection) }
    LaunchedEffect(detection) {
        if (detection == null) {
            displayed = null
            return@LaunchedEffect
        }
        while (true) {
            displayed = displayed?.let { current ->
                ScanDetection(
                    lerpQuad(current.quad, detection.quad, 0.15f),
                    detection.frameWidth,
                    detection.frameHeight
                )
            } ?: detection
            withFrameNanos { }
        }
    }

    val accent = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier) {
        val current = displayed ?: return@Canvas
        val quad = current.quad
        // PreviewView is FILL_CENTER: cover-scale the 4:3 frame and centre the leftovers.
        val frameW = current.frameWidth.toFloat()
        val frameH = current.frameHeight.toFloat()
        val scale = max(size.width / frameW, size.height / frameH)
        val dx = (size.width - frameW * scale) / 2f
        val dy = (size.height - frameH * scale) / 2f
        fun map(p: Point) = Offset(p.x.toFloat() * scale + dx, p.y.toFloat() * scale + dy)

        val outline = Path().apply {
            val tl = map(quad.topLeft)
            moveTo(tl.x, tl.y)
            val tr = map(quad.topRight)
            lineTo(tr.x, tr.y)
            val br = map(quad.bottomRight)
            lineTo(br.x, br.y)
            val bl = map(quad.bottomLeft)
            lineTo(bl.x, bl.y)
            close()
        }
        // Soft fill makes the detected page pop out of the dark viewfinder.
        drawPath(outline, accent.copy(alpha = 0.12f))
        quad.edges().forEach {
            drawLine(accent, map(it.from), map(it.to), 3.dp.toPx(), StrokeCap.Round)
        }
        listOf(quad.topLeft, quad.topRight, quad.bottomRight, quad.bottomLeft).forEach {
            drawCircle(accent, radius = 5.dp.toPx(), center = map(it))
        }
    }
}

/** Corner brackets showing where to place the page. */
@Composable
private fun FrameGuide(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val arm = 28.dp.toPx()
        val stroke = 3.dp.toPx()
        val corners = listOf(
            Offset(0f, 0f) to Offset(1f, 1f),
            Offset(size.width, 0f) to Offset(-1f, 1f),
            Offset(0f, size.height) to Offset(1f, -1f),
            Offset(size.width, size.height) to Offset(-1f, -1f)
        )
        corners.forEach { (corner, direction) ->
            drawLine(CaptureContent, corner, corner + Offset(arm * direction.x, 0f), stroke, StrokeCap.Round)
            drawLine(CaptureContent, corner, corner + Offset(0f, arm * direction.y), stroke, StrokeCap.Round)
        }
    }
}

@Composable
private fun ViewfinderMessage(
    icon: ImageVector,
    message: String,
    action: String? = null,
    onAction: () -> Unit = {}
) {
    Column(
        modifier = Modifier.padding(BubtSpacing.Xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(BubtSpacing.Md)
    ) {
        Icon(icon, contentDescription = null, tint = CaptureMuted, modifier = Modifier.size(40.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = CaptureMuted,
            textAlign = TextAlign.Center
        )
        if (action != null) BubtPrimaryButton(text = action, onClick = onAction)
    }
}

@Composable
private fun DarkIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = BubtShapes.Field,
        color = Color.Transparent,
        contentColor = if (enabled) CaptureContent else CaptureMuted.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, CaptureMuted.copy(alpha = 0.6f)),
        modifier = Modifier.size(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = contentDescription, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun LastShotButton(page: ScanPage?, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(BubtShapes.Field)
            .background(CaptureSurface)
            .border(1.5.dp, CaptureMuted, BubtShapes.Field)
            .clickable(onClickLabel = "Import from gallery", onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (page != null) {
            PageThumbnail(page = page, maxSize = 160, modifier = Modifier.fillMaxSize())
        } else {
            Icon(Icons.Outlined.PhotoLibrary, contentDescription = "Import from gallery", tint = CaptureContent)
        }
    }
}

@Composable
private fun ShutterButton(enabled: Boolean, onClick: () -> Unit) {
    val fill = if (enabled) CaptureContent else CaptureMuted.copy(alpha = 0.4f)
    Box(
        modifier = Modifier
            .size(76.dp)
            .clip(CircleShape)
            .border(4.dp, fill, CircleShape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = "Capture page" },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(fill)
        )
    }
}

@Composable
private fun PageStackButton(count: Int, enabled: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = BubtShapes.Field,
        color = Color.Transparent,
        contentColor = if (enabled) CaptureContent else CaptureMuted.copy(alpha = 0.5f),
        border = BorderStroke(1.5.dp, CaptureMuted),
        modifier = Modifier
            .height(56.dp)
            .semantics { contentDescription = "Review $count pages" }
    ) {
        Row(
            modifier = Modifier.padding(start = BubtSpacing.Md, end = BubtSpacing.Sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "$count pg", style = MaterialTheme.typography.labelLarge)
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(20.dp))
        }
    }
}

private fun android.content.Context.hasCameraPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
