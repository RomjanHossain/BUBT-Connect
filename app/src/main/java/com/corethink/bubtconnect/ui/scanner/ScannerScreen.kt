package com.corethink.bubtconnect.ui.scanner

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.OpenInFull
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.corethink.bubtconnect.data.model.CoverDetails
import com.corethink.bubtconnect.data.model.CoverTemplate
import com.corethink.bubtconnect.data.model.SimpleDate
import com.corethink.bubtconnect.ui.components.dashedBorder
import com.corethink.bubtconnect.ui.design.BUBTConnectMaterial3Theme
import com.corethink.bubtconnect.ui.design.BubtPrimaryButton
import com.corethink.bubtconnect.ui.design.BubtShapes
import com.corethink.bubtconnect.ui.design.BubtSpacing
import com.corethink.bubtconnect.ui.design.BubtSuccess
import com.corethink.bubtconnect.ui.design.BubtTextField
import java.util.Calendar
import java.util.TimeZone

/** 04 · Scanner & Cover Page Generator — full-screen wizard, no bottom nav. */
@Composable
fun ScannerRoute(onClose: () -> Unit, viewModel: ScannerViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val liveDetection by viewModel.liveDetection.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var confirmDiscard by rememberSaveable { mutableStateOf(false) }

    // Leaving with pages captured would silently throw them away, so ask first.
    val requestClose = {
        if (state.pages.isNotEmpty() && state.step != ScannerStep.Success) confirmDiscard = true else onClose()
    }
    BackHandler { if (!viewModel.back()) requestClose() }

    when (state.step) {
        ScannerStep.Template -> TemplateStep(
            state = state,
            onClose = requestClose,
            onSelect = viewModel::selectTemplate,
            onNext = viewModel::next
        )
        ScannerStep.Details -> DetailsStep(
            state = state,
            onBack = { viewModel.back() },
            onCourseCodeChange = viewModel::onCourseCodeChange,
            onDateChange = { date -> viewModel.updateDetails { it.copy(date = date) } },
            onTitleChange = { title -> viewModel.updateDetails { it.copy(title = title) } },
            onSubmittedToChange = { value -> viewModel.updateDetails { it.copy(submittedTo = value) } },
            onSubmittedByChange = viewModel::onSubmittedByChange,
            onNext = viewModel::next
        )
        ScannerStep.Capture -> CaptureStep(
            state = state,
            onClose = requestClose,
            newCaptureFile = viewModel::newCaptureFile,
            onCameraPageCaptured = viewModel::addCameraPage,
            onPagesAdded = viewModel::addPages,
            onReview = viewModel::next,
            liveDetection = liveDetection,
            onFrameAnalyzed = viewModel::analyzeFrame
        )
        ScannerStep.Review -> ReviewStep(
            state = state,
            onBack = { viewModel.back() },
            onEditCover = viewModel::editCover,
            onSelectPage = viewModel::selectPage,
            onMovePage = viewModel::movePage,
            onAddPages = viewModel::addMorePages,
            onCrop = viewModel::applyCrop,
            onRotate = viewModel::rotateSelected,
            onColorMode = viewModel::setPageColorMode,
            onDelete = viewModel::deleteSelected,
            onFileNameChange = viewModel::onFileNameChange,
            onSearchableChange = viewModel::onSearchablePdfChange,
            onMerge = viewModel::next
        )
        ScannerStep.Success -> SuccessRoute(
            state = state,
            viewModel = viewModel,
            onFinished = onClose
        )
    }

    if (confirmDiscard) {
        AlertDialog(
            onDismissRequest = { confirmDiscard = false },
            title = { Text("Discard this scan?") },
            text = { Text("The ${state.pages.size} page(s) you've added will be lost.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDiscard = false
                    onClose()
                }) { Text("Discard") }
            },
            dismissButton = { TextButton(onClick = { confirmDiscard = false }) { Text("Keep editing") } }
        )
    }
}

/** Light wizard page: header, scrolling body, pinned primary action. [overlay] draws above everything. */
@Composable
internal fun WizardFrame(
    header: @Composable () -> Unit,
    bottom: @Composable () -> Unit,
    overlay: (@Composable BoxScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            Box(modifier = Modifier.padding(horizontal = BubtSpacing.Screen)) { header() }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = BubtSpacing.Screen, vertical = BubtSpacing.Lg),
                content = content
            )
            Box(modifier = Modifier.padding(horizontal = BubtSpacing.Screen, vertical = BubtSpacing.Md)) { bottom() }
            }
            overlay?.invoke(this)
        }
    }
}

// ------------------------------------------------------------- 1o · select template

/** 1o · Choose a cover template. Long-press a tile to preview the full cover. */
@Composable
fun TemplateStep(
    state: ScannerUiState,
    onClose: () -> Unit,
    onSelect: (CoverTemplate) -> Unit,
    onNext: () -> Unit
) {
    var previewing by rememberSaveable { mutableStateOf<CoverTemplate?>(null) }
    WizardFrame(
        header = { WizardTopBar("New document", step = 1, leading = WizardLeading.Close, onLeading = onClose) },
        bottom = {
            BubtPrimaryButton(
                text = "Continue",
                onClick = onNext,
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) {
        Text(
            text = "Choose a cover",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = BubtSpacing.Xs)
        )
        Text(
            text = "We'll fill it with your details next.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = BubtSpacing.Md)
        )
        Column(verticalArrangement = Arrangement.spacedBy(BubtSpacing.Md)) {
            CoverTemplate.entries.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(BubtSpacing.Md)) {
                    row.forEach { template ->
                        TemplateTile(
                            template = template,
                            details = state.details.withPlaceholders(template),
                            selected = template == state.template,
                            onClick = { onSelect(template) },
                            onLongClick = { if (template.hasCover) previewing = template },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
        Text(
            text = "Long-press a template to preview the full cover.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = BubtSpacing.Md)
        )
    }
    previewing?.let { template ->
        CoverPreviewDialog(template, state.details.withPlaceholders(template), onDismiss = { previewing = null })
    }
}

@Composable
private fun TemplateTile(
    template: CoverTemplate,
    details: CoverDetails,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val outline = MaterialTheme.colorScheme.outline
    Box(
        modifier = modifier
            .clip(BubtShapes.Card)
            .background(MaterialTheme.colorScheme.surface)
            .then(
                when {
                    selected -> Modifier.border(2.dp, MaterialTheme.colorScheme.primary, BubtShapes.Card)
                    template.hasCover -> Modifier.border(1.dp, outline, BubtShapes.Card)
                    else -> Modifier.dashedBorder(outline, cornerRadius = 16.dp)
                }
            )
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(BubtSpacing.Sm)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp)
                    .clip(BubtShapes.Skeleton)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .clipToBounds()
            ) {
                if (template.hasCover) {
                    CoverPreview(
                        template = template,
                        details = details,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(Alignment.Top, unbounded = true)
                    )
                }
            }
            Text(
                text = template.label,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = BubtSpacing.Sm, start = BubtSpacing.Xs, bottom = BubtSpacing.Xs)
            )
        }
        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(BubtSpacing.Xs)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ------------------------------------------------------------- 1p · details + live preview

/** 1p · Cover details. The preview redraws as you type; tap it to view full-size. */
@Composable
fun DetailsStep(
    state: ScannerUiState,
    onBack: () -> Unit,
    onCourseCodeChange: (String) -> Unit,
    onDateChange: (SimpleDate) -> Unit,
    onTitleChange: (String) -> Unit,
    onSubmittedToChange: (String) -> Unit,
    onSubmittedByChange: (String) -> Unit,
    onNext: () -> Unit
) {
    var showFullPreview by rememberSaveable { mutableStateOf(false) }
    val details = state.details
    val previewDetails = details.withPlaceholders(state.template)

    WizardFrame(
        header = {
            WizardTopBar(detailsTitle(state.template), step = 2, leading = WizardLeading.Back, onLeading = onBack)
        },
        bottom = {
            BubtPrimaryButton(
                text = "Continue to scan",
                onClick = onNext,
                enabled = state.canContinueDetails,
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) {
        // 1p: 212dp preview panel — "Live preview" tag top-left, open_in_full top-right.
        Surface(
            onClick = { showFullPreview = true },
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(212.dp)
                    .clipToBounds(),
                contentAlignment = Alignment.Center
            ) {
                // 1p: the whole cover fits inside the panel (design: 184px page in a 212px panel).
                CoverPreview(
                    template = state.template,
                    details = previewDetails,
                    modifier = Modifier
                        .height(196.dp)
                        .aspectRatio(PageAspectRatio)
                )

                Surface(
                    shape = RoundedCornerShape(11.dp),
                    color = MaterialTheme.colorScheme.background,
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .height(22.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(BubtSuccess)
                        )
                        Text(
                            text = "Live preview",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Surface(
                    onClick = { showFullPreview = true },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.background,
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.OpenInFull,
                            contentDescription = "Preview full screen",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(BubtSpacing.Lg))

        Row(horizontalArrangement = Arrangement.spacedBy(BubtSpacing.Md)) {
            BubtTextField(
                value = details.courseCode,
                onValueChange = onCourseCodeChange,
                label = "Course code",
                placeholder = "CSE 231",
                supportingText = details.courseTitle.ifBlank { null },
                modifier = Modifier.weight(1f)
            )
            DateField(date = details.date, onDateChange = onDateChange, modifier = Modifier.weight(1f))
        }
        if (state.template.hasTitle) {
            BubtTextField(
                value = details.title,
                onValueChange = onTitleChange,
                label = "Title",
                placeholder = titlePlaceholder(state.template)
            )
        }
        BubtTextField(
            value = details.submittedTo,
            onValueChange = onSubmittedToChange,
            label = "Submitted to",
            placeholder = "Teacher name, designation"
        )
        BubtTextField(
            value = details.submittedBy,
            onValueChange = onSubmittedByChange,
            label = "Submitted by",
            placeholder = "Your name · ID",
            supportingText = if (state.submittedByIsAuto) "Auto-filled from your profile" else null,
            trailingIcon = if (state.submittedByIsAuto) {
                { AutoBadge(modifier = Modifier.padding(end = BubtSpacing.Md)) }
            } else null
        )
    }
    if (showFullPreview) {
        CoverPreviewDialog(state.template, previewDetails, onDismiss = { showFullPreview = false })
    }
}

@Composable
private fun AutoBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = BubtShapes.Chip,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Text(
            text = "auto",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = BubtSpacing.Sm, vertical = 2.dp)
        )
    }
}

/** Read-only field that opens the Material date picker. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(date: SimpleDate, onDateChange: (SimpleDate) -> Unit, modifier: Modifier = Modifier) {
    var open by rememberSaveable { mutableStateOf(false) }
    Box(modifier = modifier) {
        BubtTextField(
            value = "${date.day} ${date.monthShort}",
            onValueChange = {},
            label = "Date",
            readOnly = true,
            trailingIcon = { Icon(Icons.Outlined.CalendarMonth, contentDescription = null) }
        )
        // OutlinedTextField swallows taps, so an overlay opens the picker instead.
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(top = 8.dp)
                .clip(BubtShapes.Field)
                .clickable { open = true }
        )
    }
    if (open) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = date.toUtcMillis())
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { onDateChange(simpleDateFromUtcMillis(it)) }
                    open = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { open = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

private fun detailsTitle(template: CoverTemplate): String =
    if (template == CoverTemplate.IndexPage) "Index page" else "${template.label} cover"

private fun titlePlaceholder(template: CoverTemplate): String = when (template) {
    CoverTemplate.LabReport -> "Experiment name"
    CoverTemplate.ProjectReport -> "Project title"
    CoverTemplate.InternshipReport -> "Organization & role"
    else -> "Assignment title"
}

/** Grey hints in the preview so an empty form still shows the layout. */
internal fun CoverDetails.withPlaceholders(template: CoverTemplate): CoverDetails = copy(
    courseCode = courseCode.ifBlank { "CSE 231" },
    courseTitle = if (courseCode.isBlank()) "Data Structures" else courseTitle,
    title = if (template.hasTitle) title.ifBlank { titlePlaceholder(template) } else "",
    submittedTo = submittedTo.ifBlank { "Teacher name, designation" },
    submittedBy = submittedBy.ifBlank { "Your name · ID" }
)

// The date picker speaks UTC midnight millis.
private fun SimpleDate.toUtcMillis(): Long = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
    clear()
    set(year, month - 1, day)
}.timeInMillis

private fun simpleDateFromUtcMillis(millis: Long): SimpleDate {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = millis }
    return SimpleDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH))
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun TemplateStepPreview() {
    BUBTConnectMaterial3Theme {
        TemplateStep(state = ScannerUiState(), onClose = {}, onSelect = {}, onNext = {})
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun DetailsStepPreview() {
    BUBTConnectMaterial3Theme {
        DetailsStep(
            state = ScannerUiState(
                step = ScannerStep.Details,
                details = CoverDetails(courseCode = "CSE 231", courseTitle = "Data Structures", submittedBy = "Rafi Ahmed · 22235103045"),
                submittedByIsAuto = true
            ),
            onBack = {},
            onCourseCodeChange = {},
            onDateChange = {},
            onTitleChange = {},
            onSubmittedToChange = {},
            onSubmittedByChange = {},
            onNext = {}
        )
    }
}
