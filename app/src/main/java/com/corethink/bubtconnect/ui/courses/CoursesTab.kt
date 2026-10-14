package com.corethink.bubtconnect.ui.courses

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.corethink.bubtconnect.ui.components.FilterPill
import com.corethink.bubtconnect.ui.components.formatCredits
import com.corethink.bubtconnect.ui.design.BUBTConnectMaterial3Theme
import com.corethink.bubtconnect.ui.design.BubtShapes
import com.corethink.bubtconnect.ui.design.BubtSpacing

@Composable
fun CoursesTab(viewModel: CoursesViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CoursesContent(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onSelectFilter = viewModel::selectFilter
    )
}

/** 1l · All courses. Completed badge + filter only appear when Annex is connected. */
@Composable
fun CoursesContent(
    state: CoursesUiState,
    onQueryChange: (String) -> Unit,
    onSelectFilter: (CourseFilter) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = BubtSpacing.Screen, vertical = BubtSpacing.Md)
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
        if (state.isConnected) {
            item {
                Row(
                    modifier = Modifier.padding(top = BubtSpacing.Md),
                    horizontalArrangement = Arrangement.spacedBy(BubtSpacing.Sm)
                ) {
                    CourseFilter.entries.forEach { filter ->
                        FilterPill(
                            text = filter.label,
                            selected = filter == state.filter,
                            onClick = { onSelectFilter(filter) }
                        )
                    }
                }
            }
        }
        item {
            Text(
                text = if (state.isConnected) {
                    "${state.department} curriculum · ${state.completedCount} of ${state.totalCount} completed"
                } else {
                    "${state.department} curriculum · ${state.totalCount} courses"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = BubtSpacing.Md)
            )
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
            CourseRow(item = item, showStatus = state.isConnected)
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
private fun CourseRow(item: CurriculumCourse, showStatus: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = BubtSpacing.Md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.course.code,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${item.course.title} · ${formatCredits(item.credits)} cr",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (showStatus) {
            when (item.status) {
                CourseStatus.Completed -> Surface(
                    shape = BubtShapes.Chip,
                    color = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = BubtSpacing.Md, vertical = BubtSpacing.Xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        Text(text = " Completed", style = MaterialTheme.typography.labelMedium)
                    }
                }
                CourseStatus.InProgress -> Text(
                    text = "In progress",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                CourseStatus.Remaining -> Unit
            }
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
                    isConnected = true,
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
