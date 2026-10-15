package com.corethink.bubtconnect.ui.courses

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.corethink.bubtconnect.data.model.CourseStatus
import com.corethink.bubtconnect.data.model.CurriculumCourse
import com.corethink.bubtconnect.data.repository.SampleAcademicsRepository
import com.corethink.bubtconnect.ui.components.BubtSegmentedControl
import com.corethink.bubtconnect.ui.components.formatCredits
import com.corethink.bubtconnect.ui.design.BUBTConnectMaterial3Theme
import com.corethink.bubtconnect.ui.design.BubtShapes
import com.corethink.bubtconnect.ui.design.BubtSpacing
import com.corethink.bubtconnect.ui.design.BubtTag
import com.corethink.bubtconnect.ui.design.BubtTagStyle

@Composable
fun CoursesTab(viewModel: CoursesViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CoursesContent(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onSelectFilter = viewModel::selectFilter
    )
}

/** 1m · All courses. Segmented filter, completion progress, Done badges on completed rows. */
@Composable
fun CoursesContent(
    state: CoursesUiState,
    onQueryChange: (String) -> Unit,
    onSelectFilter: (CourseFilter) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = BubtSpacing.Screen, vertical = BubtSpacing.Md),
        verticalArrangement = Arrangement.spacedBy(BubtSpacing.Md)
    ) {
        item {
            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search code or title", style = MaterialTheme.typography.bodyMedium) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                trailingIcon = if (state.query.isNotEmpty()) {
                    {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear search")
                        }
                    }
                } else null,
                singleLine = true,
                shape = BubtShapes.Field,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
        item {
            BubtSegmentedControl(
                options = CourseFilter.entries,
                selected = state.filter,
                label = CourseFilter::label,
                onSelect = onSelectFilter
            )
        }
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${state.completedCount} of ${state.totalCount} courses completed",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${state.progressPercent}%",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                LinearProgressIndicator(
                    progress = { state.progressPercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = BubtSpacing.Sm)
                        .height(4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            }
        }
        if (state.courses.isEmpty()) {
            item {
                Text(
                    text = "No courses match \"${state.query}\".",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        items(state.courses, key = { it.course.code }) { item ->
            CourseCard(item = item)
        }
    }
}

@Composable
private fun CourseCard(item: CurriculumCourse) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = BubtShapes.Card,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(BubtSpacing.CardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NumberTile(number = item.course.code.takeLast(3))
            Spacer(modifier = Modifier.width(BubtSpacing.Md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.course.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${item.course.code} · ${formatCredits(item.credits)} credits",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            when (item.status) {
                CourseStatus.Completed -> BubtTag(
                    text = "Done",
                    style = BubtTagStyle.Success,
                    leadingIcon = Icons.Filled.Check
                )
                CourseStatus.InProgress -> BubtTag(
                    text = "Ongoing",
                    style = BubtTagStyle.Primary
                )
                CourseStatus.Remaining -> Unit
            }
        }
    }
}

/** Leading rounded tile showing the 3-digit course number (e.g. "101"). */
@Composable
private fun NumberTile(number: String) {
    Surface(
        shape = BubtShapes.Field,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.size(48.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = number,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 750)
@Composable
private fun CoursesContentPreview() {
    val courses = SampleAcademicsRepository.curriculum()
    BUBTConnectMaterial3Theme {
        Surface(color = MaterialTheme.colorScheme.background) {
            CoursesContent(
                state = CoursesUiState(
                    courses = courses,
                    completedCount = courses.count { it.status == CourseStatus.Completed },
                    totalCount = courses.size
                ),
                onQueryChange = {},
                onSelectFilter = {}
            )
        }
    }
}
