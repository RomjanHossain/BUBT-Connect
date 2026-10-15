package com.corethink.bubtconnect.ui.courses

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.corethink.bubtconnect.data.model.CourseStatus
import com.corethink.bubtconnect.data.model.CurriculumCourse
import com.corethink.bubtconnect.data.repository.SampleAcademicsRepository
import com.corethink.bubtconnect.ui.components.FilterPill
import com.corethink.bubtconnect.ui.components.formatCredits
import com.corethink.bubtconnect.ui.design.BUBTConnectMaterial3Theme
import com.corethink.bubtconnect.ui.design.BubtOnSuccessContainerLight
import com.corethink.bubtconnect.ui.design.BubtShapes
import com.corethink.bubtconnect.ui.design.BubtSpacing
import com.corethink.bubtconnect.ui.design.BubtSuccess
import com.corethink.bubtconnect.ui.design.BubtSuccessContainerLight
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

/** 1m · All courses. Matches HTML 1m exactly. */
@Composable
fun CoursesContent(
    state: CoursesUiState,
    onQueryChange: (String) -> Unit,
    onSelectFilter: (CourseFilter) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                shape = RoundedCornerShape(26.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(CourseFilter.entries) { filter ->
                    FilterPill(
                        text = filter.label,
                        selected = filter == state.filter,
                        onClick = { onSelectFilter(filter) }
                    )
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${state.completedCount} of ${state.totalCount} courses completed",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${state.progressPercent}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(state.progressPercent / 100f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(BubtSuccess)
                    )
                }
            }
        }
        if (state.courses.isEmpty()) {
            item {
                Text(
                    text = "No courses match \"${state.query}\".",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp)
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
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            NumberTile(
                number = item.course.code.takeLast(3),
                status = item.status
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = item.course.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
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
                CourseStatus.Remaining -> {}
            }
        }
    }
}

@Composable
private fun NumberTile(number: String, status: CourseStatus) {
    val container = when (status) {
        CourseStatus.Completed -> BubtSuccessContainerLight
        CourseStatus.InProgress -> MaterialTheme.colorScheme.primaryContainer
        CourseStatus.Remaining -> MaterialTheme.colorScheme.surfaceContainer
    }
    val content = when (status) {
        CourseStatus.Completed -> BubtOnSuccessContainerLight
        CourseStatus.InProgress -> MaterialTheme.colorScheme.onPrimaryContainer
        CourseStatus.Remaining -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(container),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = content,
            fontSize = 11.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CoursesTabPreview() {
    BUBTConnectMaterial3Theme {
        CoursesContent(
            state = CoursesUiState(courses = SampleAcademicsRepository.curriculum()),
            onQueryChange = {},
            onSelectFilter = {}
        )
    }
}