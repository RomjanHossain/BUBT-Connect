package com.corethink.bubtconnect.ui.results

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.corethink.bubtconnect.data.model.CourseGrade
import com.corethink.bubtconnect.data.model.SemesterResult
import com.corethink.bubtconnect.data.repository.SampleAcademicsRepository
import com.corethink.bubtconnect.ui.components.formatGpa
import com.corethink.bubtconnect.ui.components.formatCredits
import com.corethink.bubtconnect.ui.design.BUBTConnectMaterial3Theme
import com.corethink.bubtconnect.ui.design.BubtShapes
import com.corethink.bubtconnect.ui.design.BubtSpacing
import com.corethink.bubtconnect.ui.design.BubtSuccess
import com.corethink.bubtconnect.ui.onboarding.BubtDropdownField

@Composable
fun ResultsTab(viewModel: ResultsViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ResultsContent(
        state = state,
        onSelectSemester = viewModel::selectSemester,
        onToggleCourse = viewModel::toggleCourse
    )
}

/** 1j · Results. Semester dropdown, SGPA delta, CGPA with credits, history bars, course grades. */
@Composable
fun ResultsContent(
    state: ResultsUiState,
    onSelectSemester: (Int) -> Unit,
    onToggleCourse: (String) -> Unit
) {
    val selected = state.selected ?: return
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = BubtSpacing.Screen, vertical = BubtSpacing.Md),
        verticalArrangement = Arrangement.spacedBy(BubtSpacing.Md)
    ) {
        BubtDropdownField(
            value = selected.semester,
            label = "Semester",
            placeholder = "Select semester",
            options = state.semesters.map { it.semester },
            onSelect = { name -> onSelectSemester(state.semesters.indexOfFirst { it.semester == name }) }
        )

        SgpaCgpaPanel(
            sgpa = selected.sgpa,
            sgpaDelta = state.sgpaDelta,
            cgpa = state.cgpa,
            credits = formatCredits(state.totalCredits)
        )

        SgpaHistory(
            semesters = state.semesters,
            selectedIndex = state.selectedIndex,
            onSelect = onSelectSemester
        )

        Text(
            text = "Course grades",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = BubtSpacing.Sm)
        )
        Column {
            selected.grades.forEach { grade ->
                GradeRow(
                    grade = grade,
                    expanded = state.expandedCourse == grade.course.code,
                    onToggle = { onToggleCourse(grade.course.code) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

/** One solid primary panel splitting SGPA (with delta) and CGPA (with credits). */
@Composable
private fun SgpaCgpaPanel(
    sgpa: Double,
    sgpaDelta: Double?,
    cgpa: Double,
    credits: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = BubtShapes.Card,
        color = MaterialTheme.colorScheme.primary
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = BubtSpacing.Xl),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GpaHalf(
                label = "SGPA",
                value = formatGpa(sgpa),
                content = Color.White,
                modifier = Modifier.weight(1f),
                delta = sgpaDelta
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(56.dp)
                    .background(Color.White.copy(alpha = 0.2f))
            )
            GpaHalf(
                label = "CGPA",
                value = formatGpa(cgpa),
                content = Color.White,
                modifier = Modifier.weight(1f),
                subtitle = credits
            )
        }
    }
}

@Composable
private fun GpaHalf(
    label: String,
    value: String,
    content: Color,
    modifier: Modifier = Modifier,
    delta: Double? = null,
    subtitle: String? = null
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = content.copy(alpha = 0.85f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.displaySmall,
            color = content
        )
        when {
            delta != null -> Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (delta >= 0) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                    contentDescription = null,
                    tint = if (delta >= 0) Color.White else Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = String.format(java.util.Locale.US, "%+.2f", delta),
                    style = MaterialTheme.typography.labelMedium,
                    color = content
                )
            }
            subtitle != null -> Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = content.copy(alpha = 0.85f)
            )
        }
    }
}

/** Bars run from a 2.00 baseline so small SGPA changes stay visible; each bar is labelled. */
@Composable
private fun SgpaHistory(
    semesters: List<SemesterResult>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    val baseline = 2.0
    val ceiling = 4.0
    val ordinals = listOf("1st", "2nd", "3rd", "4th", "5th", "6th")
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = BubtShapes.Card,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(BubtSpacing.CardPadding)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "SGPA history",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "out of 4.00",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(BubtSpacing.Md))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                horizontalArrangement = Arrangement.spacedBy(BubtSpacing.Md),
                verticalAlignment = Alignment.Bottom
            ) {
                semesters.forEachIndexed { index, semester ->
                    val active = index == selectedIndex
                    val barColor by animateColorAsState(
                        if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
                        label = "barColor"
                    )
                    val fraction = ((semester.sgpa - baseline) / (ceiling - baseline)).toFloat().coerceIn(0.05f, 1f)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onSelect(index) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = formatGpa(semester.sgpa),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(BubtSpacing.Xs))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(fraction * 0.8f)
                                .background(barColor, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        )
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = BubtSpacing.Xs),
                horizontalArrangement = Arrangement.spacedBy(BubtSpacing.Md)
            ) {
                semesters.indices.forEach { index ->
                    Text(
                        text = ordinals.getOrElse(index) { "S${index + 1}" },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun GradeRow(grade: CourseGrade, expanded: Boolean, onToggle: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = BubtSpacing.Md)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = grade.course.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${grade.course.code} · ${formatCredits(grade.credits)} cr",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = grade.grade.label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        AnimatedVisibility(visible = expanded) {
            Text(
                text = "Grade point ${formatGpa(grade.grade.point)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = BubtSpacing.Xs)
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun ResultsContentPreview() {
    val semesters = SampleAcademicsRepository.results()
    BUBTConnectMaterial3Theme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ResultsContent(
                state = ResultsUiState(isConnected = true, semesters = semesters, selectedIndex = semesters.lastIndex),
                onSelectSemester = {},
                onToggleCourse = {}
            )
        }
    }
}
