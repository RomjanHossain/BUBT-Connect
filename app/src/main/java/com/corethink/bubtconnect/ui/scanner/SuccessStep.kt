package com.corethink.bubtconnect.ui.scanner

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.corethink.bubtconnect.ui.design.BUBTConnectMaterial3Theme
import com.corethink.bubtconnect.ui.design.BubtOutlinedButton
import com.corethink.bubtconnect.ui.design.BubtPrimaryButton
import com.corethink.bubtconnect.ui.design.BubtShapes
import com.corethink.bubtconnect.ui.design.BubtSpacing
import java.io.File
import java.util.Locale

@Composable
fun SuccessRoute(state: ScannerUiState, viewModel: ScannerViewModel, onFinished: () -> Unit) {
    val context = LocalContext.current
    val saveLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        if (uri != null) {
            viewModel.saveCopy(uri) { saved ->
                Toast.makeText(context, if (saved) "Saved to device" else "Couldn't save the PDF", Toast.LENGTH_SHORT).show()
            }
        }
    }
    SuccessStep(
        state = state,
        onSave = { state.result?.let { saveLauncher.launch(it.file.name) } },
        onShare = { state.result?.let { sharePdf(context, it.file) } },
        onPublishChange = viewModel::setPublishToCommunity,
        onAddTag = viewModel::addTag,
        onRemoveTag = viewModel::removeTag,
        onAnonymousChange = viewModel::setPostAnonymously,
        onDone = {
            viewModel.finish()
            onFinished()
        }
    )
}

/** 1s · PDF ready. Turning "Publish" off hides tags + anonymous row and the primary becomes "Done". */
@Composable
fun SuccessStep(
    state: ScannerUiState,
    onSave: () -> Unit,
    onShare: () -> Unit,
    onPublishChange: (Boolean) -> Unit,
    onAddTag: (String) -> Unit,
    onRemoveTag: (String) -> Unit,
    onAnonymousChange: (Boolean) -> Unit,
    onDone: () -> Unit
) {
    WizardFrame(
        header = { WizardTopBar("", step = 4, leading = WizardLeading.Close, onLeading = onDone) },
        bottom = {
            BubtPrimaryButton(
                text = if (state.publishToCommunity) "Publish & finish" else "Done",
                onClick = onDone,
                modifier = Modifier.fillMaxWidth()
            )
        },
        // Design 1s: confetti falls once behind the celebration.
        overlay = { ConfettiFall(Modifier.align(Alignment.TopCenter).fillMaxWidth().height(340.dp)) }
    ) {
        // 1s: the success check pops in with a spring and a soft green ring.
        val okGreen = Color(0xFF10B981)
        val pop = remember { Animatable(0.3f) }
        LaunchedEffect(Unit) {
            pop.animateTo(
                1f,
                animationSpec = spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMedium)
            )
        }
        Box(
            modifier = Modifier
                .padding(vertical = BubtSpacing.Md)
                .size(96.dp)
                .graphicsLayer {
                    scaleX = pop.value
                    scaleY = pop.value
                }
                .clip(CircleShape)
                .background(okGreen.copy(alpha = 0.18f))
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(okGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = "PDF created",
                    tint = Color.White,
                    modifier = Modifier.size(42.dp)
                )
            }
        }
        Text(
            text = "Your PDF is ready",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(BubtSpacing.Md))

        state.result?.let { result ->
            Surface(
                shape = BubtShapes.Card,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(BubtSpacing.CardPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 40.dp, height = 48.dp)
                            .clip(BubtShapes.Skeleton)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.PictureAsPdf, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(BubtSpacing.Md))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = result.file.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${result.pageCount} page${if (result.pageCount == 1) "" else "s"} · ${formatFileSize(result.sizeBytes)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(BubtSpacing.Md))
        Row(horizontalArrangement = Arrangement.spacedBy(BubtSpacing.Md)) {
            BubtOutlinedButton(
                text = "Save",
                onClick = onSave,
                leadingIcon = Icons.Outlined.FileDownload,
                modifier = Modifier.weight(1f)
            )
            BubtOutlinedButton(
                text = "Share",
                onClick = onShare,
                leadingIcon = Icons.Outlined.Share,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(BubtSpacing.Lg))

        PublishCard(
            state = state,
            onPublishChange = onPublishChange,
            onAddTag = onAddTag,
            onRemoveTag = onRemoveTag,
            onAnonymousChange = onAnonymousChange
        )
    }
}

@Composable
private fun PublishCard(
    state: ScannerUiState,
    onPublishChange: (Boolean) -> Unit,
    onAddTag: (String) -> Unit,
    onRemoveTag: (String) -> Unit,
    onAnonymousChange: (Boolean) -> Unit
) {
    Surface(
        shape = BubtShapes.Card,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(BubtSpacing.CardPadding)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Publish to Community?",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Switch(checked = state.publishToCommunity, onCheckedChange = onPublishChange)
            }
            if (state.publishToCommunity) {
                Text(
                    text = "Help classmates find good references",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = BubtSpacing.Xs, bottom = BubtSpacing.Sm)
                )
                TagInput(tags = state.tags, onAddTag = onAddTag, onRemoveTag = onRemoveTag)
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(vertical = BubtSpacing.Md)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Post anonymously",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(checked = state.postAnonymously, onCheckedChange = onAnonymousChange)
                }
            }
        }
    }
}

/** Removable #tag chips plus a free-text "add tag…" field; space, comma or Done commits a tag. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagInput(tags: List<String>, onAddTag: (String) -> Unit, onRemoveTag: (String) -> Unit) {
    var draft by rememberSaveable { mutableStateOf("") }
    fun commit() {
        if (draft.isNotBlank()) onAddTag(draft)
        draft = ""
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(BubtShapes.Field)
            .border(1.dp, MaterialTheme.colorScheme.outline, BubtShapes.Field)
            .padding(BubtSpacing.Md)
    ) {
        if (tags.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(BubtSpacing.Sm),
                verticalArrangement = Arrangement.spacedBy(BubtSpacing.Sm),
                modifier = Modifier.padding(bottom = BubtSpacing.Sm)
            ) {
                tags.forEach { tag ->
                    Surface(
                        shape = BubtShapes.Chip,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            modifier = Modifier.padding(start = BubtSpacing.Md, end = BubtSpacing.Xs),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "#$tag", style = MaterialTheme.typography.labelMedium)
                            Surface(
                                onClick = { onRemoveTag(tag) },
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.Close, contentDescription = "Remove #$tag", modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
        Box {
            if (draft.isEmpty()) {
                Text(
                    text = "Add tag…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            BasicTextField(
                value = draft,
                onValueChange = { value ->
                    if (value.endsWith(" ") || value.endsWith(",")) {
                        draft = value.dropLast(1)
                        commit()
                    } else {
                        draft = value
                    }
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { commit() }),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Design 1s: one round of confetti strips falling from the top (design "falls once").
 * Each strip has its own x position, color and start delay so they layer like the mock.
 */
@Composable
private fun ConfettiFall(modifier: Modifier = Modifier) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, animationSpec = tween(durationMillis = 3200, easing = LinearEasing))
    }
    val strips = remember {
        listOf(
            Triple(0.05f, 0.00f, Color(0xFF2563EB)),
            Triple(0.14f, 0.25f, Color(0xFF10B981)),
            Triple(0.23f, 0.50f, Color(0xFFF59E0B)),
            Triple(0.32f, 0.12f, Color(0xFFEF4444)),
            Triple(0.41f, 0.62f, Color(0xFF60A5FA)),
            Triple(0.50f, 0.34f, Color(0xFF10B981)),
            Triple(0.59f, 0.78f, Color(0xFFF59E0B)),
            Triple(0.68f, 0.06f, Color(0xFF2563EB)),
            Triple(0.77f, 0.55f, Color(0xFFA855F7)),
            Triple(0.86f, 0.30f, Color(0xFFEF4444)),
            Triple(0.94f, 0.68f, Color(0xFFF59E0B))
        )
    }
    Canvas(modifier) {
        val stripW = 8.dp.toPx()
        val stripH = 14.dp.toPx()
        strips.forEach { (xFraction, delay, color) ->
            val p = (progress.value - delay) / (1f - delay)
            if (p < 0f || p > 1f) return@forEach
            val x = xFraction * size.width
            val y = -stripH + p * (size.height + stripH)
            val alpha = if (p > 0.7f) ((1f - p) / 0.3f).coerceIn(0f, 1f) else 1f
            rotate(degrees = p * 540f, pivot = Offset(x, y)) {
                drawRoundRect(
                    color = color.copy(alpha = alpha),
                    topLeft = Offset(x - stripW / 2f, y - stripH / 2f),
                    size = Size(stripW, stripH),
                    cornerRadius = CornerRadius(3f, 3f)
                )
            }
        }
    }
}

private fun sharePdf(context: Context, file: File) {    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    try {
        context.startActivity(Intent.createChooser(send, "Share PDF"))
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, "No app available to share PDFs", Toast.LENGTH_SHORT).show()
    }
}

/** 1_468_006 → "1.4 MB", 86_000 → "84 KB" */
internal fun formatFileSize(bytes: Long): String = when {
    bytes >= 1024 * 1024 -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
    bytes >= 1024 -> "${(bytes + 512) / 1024} KB"
    else -> "$bytes B"
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun SuccessStepPreview() {
    BUBTConnectMaterial3Theme {
        SuccessStep(
            state = ScannerUiState(
                step = ScannerStep.Success,
                result = ScanResult(File("CSE231_Assignment_Rafi.pdf"), pageCount = 4, sizeBytes = 1_468_006),
                tags = listOf("CSE231", "DataStructures")
            ),
            onSave = {},
            onShare = {},
            onPublishChange = {},
            onAddTag = {},
            onRemoveTag = {},
            onAnonymousChange = {},
            onDone = {}
        )
    }
}
