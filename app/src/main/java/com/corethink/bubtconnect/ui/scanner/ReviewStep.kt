package com.corethink.bubtconnect.ui.scanner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Crop
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.RotateRight
import androidx.compose.material.icons.outlined.Tonality
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.corethink.bubtconnect.data.model.CoverDetails
import com.corethink.bubtconnect.data.model.ScanPage
import com.corethink.bubtconnect.ui.components.BubtSegmentedControl
import com.corethink.bubtconnect.ui.components.dashedBorder
import com.corethink.bubtconnect.ui.design.BUBTConnectMaterial3Theme
import com.corethink.bubtconnect.ui.design.BubtPrimaryButton
import com.corethink.bubtconnect.ui.design.BubtShapes
import com.corethink.bubtconnect.ui.design.BubtSpacing
import com.corethink.bubtconnect.ui.design.BubtTextField
import com.corethink.bubtconnect.ui.design.bubtErrorText
import org.fairscan.imageprocessing.ColorMode
import org.fairscan.imageprocessing.Quad
import kotlin.math.roundToInt

private const val GridColumns = 3

/** 1r · Review + merge. The cover (from 1p) is pinned as page 1; long-press and drag the rest to reorder. */
@Composable
fun ReviewStep(
    state: ScannerUiState,
    onBack: () -> Unit,
    onEditCover: () -> Unit,
    onSelectPage: (Long) -> Unit,
    onMovePage: (Int, Int) -> Unit,
    onAddPages: () -> Unit,
    onCrop: (Quad) -> Unit,
    onRotate: () -> Unit,
    onColorMode: (ColorMode?) -> Unit,
    onDelete: () -> Unit,
    onFileNameChange: (String) -> Unit,
    onSearchableChange: (Boolean) -> Unit,
    onMerge: () -> Unit
) {
    val totalPages = state.pages.size + if (state.template.hasCover) 1 else 0
    WizardFrame(
        header = { WizardTopBar("Review pages", step = 3, leading = WizardLeading.Back, onLeading = onBack) },
        bottom = {
            BubtPrimaryButton(
                text = if (state.isMerging) "Merging…" else "Merge $totalPages page${if (totalPages == 1) "" else "s"}",
                onClick = onMerge,
                enabled = state.canMerge,
                loading = state.isMerging,
                leadingIcon = Icons.Outlined.PictureAsPdf,
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) {
        Text(
            text = "Long-press and drag to reorder. Tap a page to edit.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = BubtSpacing.Md)
        )
        PageGrid(
            state = state,
            onEditCover = onEditCover,
            onSelectPage = onSelectPage,
            onMovePage = onMovePage,
            onAddPages = onAddPages
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(vertical = BubtSpacing.Md))
        val selected = state.selectedPage
        val editing = state.processingPageId != null
        var cropping by remember { mutableStateOf(false) }
        var previewing by remember { mutableStateOf(false) }
        var showFilter by remember { mutableStateOf(true) }
        // Design 1r: tools sit in one tonal rounded row, spread evenly.
        Surface(
            shape = BubtShapes.Card,
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(vertical = BubtSpacing.Md - 2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ToolButton(
                    Icons.Outlined.Visibility,
                    "Preview",
                    enabled = state.pages.isNotEmpty() && !editing,
                    onClick = {
                        // Preview the selected page, or page 1 when nothing is selected.
                        (selected ?: state.pages.firstOrNull())?.let { first ->
                            if (selected == null) onSelectPage(first.id)
                            previewing = true
                        }
                    }
                )
                ToolButton(
                    Icons.Outlined.Crop,
                    "Crop",
                    enabled = selected != null && selected.sourceUri != null && !editing,
                    onClick = { cropping = true }
                )
                ToolButton(Icons.Outlined.RotateRight, "Rotate", enabled = selected != null && !editing, onClick = onRotate)
                ToolButton(
                    Icons.Outlined.Tonality,
                    "Filter",
                    enabled = selected != null && !editing,
                    onClick = { showFilter = !showFilter }
                )
                ToolButton(Icons.Outlined.Delete, "Delete", enabled = selected != null && !editing, danger = true, onClick = onDelete)
            }
        }
        if (showFilter && selected != null) {
            Text(
                text = "Color mode",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = BubtSpacing.Md)
            )
            BubtSegmentedControl(
                options = PageMode.entries,
                selected = PageMode.from(selected),
                label = { it.label },
                onSelect = { onColorMode(it.toColorMode()) }
            )
        }
        if (cropping && selected != null) {
            CropPageDialog(
                page = selected,
                onConfirm = { quad ->
                    cropping = false
                    onCrop(quad)
                },
                onDismiss = { cropping = false }
            )
        }
        if (previewing && selected != null) {
            val pageIndex = state.pages.indexOfFirst { it.id == selected.id }
            if (pageIndex >= 0) {
                PagePreviewDialog(
                    page = selected,
                    pageLabel = "Page ${pageIndex + 1 + if (state.template.hasCover) 1 else 0} of $totalPages",
                    onRotate = onRotate,
                    onDismiss = { previewing = false }
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(vertical = BubtSpacing.Md))

        BubtTextField(
            value = state.fileName,
            onValueChange = onFileNameChange,
            label = "File name",
            supportingText = summary(state)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Searchable text (OCR)",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Adds hidden text so you can search and copy from the PDF.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = state.searchablePdf, onCheckedChange = onSearchableChange)
        }
        state.mergeError?.let {
            Text(text = it, style = MaterialTheme.typography.bodySmall, color = bubtErrorText())
        }
    }
}

private sealed interface GridCell {
    data object Cover : GridCell
    data class Page(val index: Int, val page: ScanPage) : GridCell
    data object Add : GridCell
}

@Composable
private fun PageGrid(
    state: ScannerUiState,
    onEditCover: () -> Unit,
    onSelectPage: (Long) -> Unit,
    onMovePage: (Int, Int) -> Unit,
    onAddPages: () -> Unit
) {
    val coverSlots = if (state.template.hasCover) 1 else 0
    val cells = buildList {
        if (coverSlots == 1) add(GridCell.Cover)
        state.pages.forEachIndexed { index, page -> add(GridCell.Page(index, page)) }
        add(GridCell.Add)
    }
    var draggingId by remember { mutableStateOf<Long?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val gap = BubtSpacing.Md
        val labelHeight = 24.dp
        val cellWidth = (maxWidth - gap * (GridColumns - 1)) / GridColumns
        val tileHeight = cellWidth / PageAspectRatio
        val density = LocalDensity.current
        val stepX = with(density) { (cellWidth + gap).toPx() }
        val stepY = with(density) { (tileHeight + labelHeight + gap).toPx() }

        Column(verticalArrangement = Arrangement.spacedBy(gap)) {
            cells.chunked(GridColumns).forEach { row ->
                // The dragged tile must draw above later rows, so lift its whole row.
                val rowLifted = row.any { it is GridCell.Page && it.page.id == draggingId }
                Row(
                    modifier = Modifier.zIndex(if (rowLifted) 1f else 0f),
                    horizontalArrangement = Arrangement.spacedBy(gap)
                ) {
                    row.forEach { cell ->
                        when (cell) {
                            GridCell.Cover -> GridSlot(cellWidth, labelHeight, label = "1 · COVER") {
                                CoverTile(state, tileHeight, onEditCover)
                            }
                            is GridCell.Page -> {
                                val isDragging = cell.page.id == draggingId
                                GridSlot(
                                    width = cellWidth,
                                    labelHeight = labelHeight,
                                    label = "${cell.index + 1 + coverSlots}",
                                    modifier = Modifier
                                        .zIndex(if (isDragging) 1f else 0f)
                                        .graphicsLayer {
                                            if (isDragging) {
                                                translationX = dragOffset.x
                                                translationY = dragOffset.y
                                                scaleX = 1.05f
                                                scaleY = 1.05f
                                                shadowElevation = 8.dp.toPx()
                                            }
                                        }
                                        .pointerInput(cell.page.id, cell.index, stepX, stepY) {
                                            detectDragGesturesAfterLongPress(
                                                onDragStart = {
                                                    draggingId = cell.page.id
                                                    dragOffset = Offset.Zero
                                                },
                                                onDrag = { change, amount ->
                                                    change.consume()
                                                    dragOffset += amount
                                                },
                                                onDragEnd = {
                                                    val target = cell.index +
                                                        (dragOffset.x / stepX).roundToInt() +
                                                        (dragOffset.y / stepY).roundToInt() * GridColumns
                                                    onMovePage(cell.index, target)
                                                    draggingId = null
                                                    dragOffset = Offset.Zero
                                                },
                                                onDragCancel = {
                                                    draggingId = null
                                                    dragOffset = Offset.Zero
                                                }
                                            )
                                        }
                                ) {
                                    PageTile(
                                        page = cell.page,
                                        height = tileHeight,
                                        selected = cell.page.id == state.selectedPageId,
                                        busy = state.processingPageId == cell.page.id,
                                        onClick = { onSelectPage(cell.page.id) }
                                    )
                                }
                            }
                            GridCell.Add -> GridSlot(cellWidth, labelHeight, label = "Add") {
                                AddTile(tileHeight, onAddPages)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GridSlot(
    width: Dp,
    labelHeight: Dp,
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier.width(width), horizontalAlignment = Alignment.CenterHorizontally) {
        content()
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .height(labelHeight)
                .padding(top = BubtSpacing.Xs)
        )
    }
}

@Composable
private fun CoverTile(state: ScannerUiState, height: Dp, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(BubtShapes.Field)
            .border(2.dp, MaterialTheme.colorScheme.onSurface, BubtShapes.Field)
            .clickable(onClickLabel = "Edit cover", onClick = onClick)
            .clipToBounds()
    ) {
        CoverPreview(
            template = state.template,
            details = state.details,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(Alignment.Top, unbounded = true)
        )
    }
}

@Composable
private fun PageTile(page: ScanPage, height: Dp, selected: Boolean, busy: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(BubtShapes.Field)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = BubtShapes.Field
            )
            .clickable(onClickLabel = if (selected) "Deselect page" else "Select page", onClick = onClick)
            .clipToBounds()
    ) {
        PageThumbnail(page = page, modifier = Modifier.fillMaxSize())
        if (busy) {
            // Reprocessing (crop / color mode) redraws the page from the original.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            }
        }
    }
}

@Composable
private fun AddTile(height: Dp, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(BubtShapes.Field)
            .dashedBorder(MaterialTheme.colorScheme.outline, cornerRadius = 12.dp)
            .clickable(onClickLabel = "Add pages", onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Filled.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun ToolButton(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    danger: Boolean = false,
    onClick: () -> Unit
) {
    // Design 1r: Delete is styled with the error color, muted when disabled.
    val content = when {
        !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        danger -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(enabled = enabled, onClickLabel = label, onClick = onClick)
            .padding(horizontal = BubtSpacing.Sm)
    ) {
        Icon(icon, contentDescription = label, tint = content, modifier = Modifier.size(22.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = content,
            modifier = Modifier.padding(top = BubtSpacing.Xs)
        )
    }
}

private fun summary(state: ScannerUiState): String {
    val pages = state.pages.size
    val pageText = "$pages page" + if (pages == 1) "" else "s"
    return if (state.template.hasCover) "Cover + $pageText" else pageText
}

/** Per-page color mode chips; Auto re-detects from the original photo. */
private enum class PageMode(val label: String) {
    Auto("Auto"),
    Color("Color"),
    Grayscale("Gray"),
    BlackAndWhite("B&W");

    fun toColorMode(): ColorMode? = when (this) {
        Auto -> null
        Color -> ColorMode.COLOR
        Grayscale -> ColorMode.GRAYSCALE
        BlackAndWhite -> ColorMode.BLACK_AND_WHITE
    }

    companion object {
        fun from(page: ScanPage): PageMode = when (page.colorMode) {
            null -> Auto
            ColorMode.COLOR -> Color
            ColorMode.GRAYSCALE -> Grayscale
            ColorMode.BLACK_AND_WHITE -> BlackAndWhite
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun ReviewStepPreview() {
    BUBTConnectMaterial3Theme {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            ReviewStep(
                state = ScannerUiState(
                    step = ScannerStep.Review,
                    details = CoverDetails(courseCode = "CSE 231", courseTitle = "Data Structures", title = "Linked Lists"),
                    fileName = "CSE231_Assignment_Rafi.pdf"
                ),
                onBack = {},
                onEditCover = {},
                onSelectPage = {},
                onMovePage = { _, _ -> },
                onAddPages = {},
                onCrop = {},
                onRotate = {},
                onColorMode = {},
                onDelete = {},
                onFileNameChange = {},
                onSearchableChange = {},
                onMerge = {}
            )
        }
    }
}
